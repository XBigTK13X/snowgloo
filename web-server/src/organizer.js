const recurse = require('recursive-readdir')
const _ = require('lodash')

const settings = require('./settings')
const util = require('./util')
const fileSystem = require('./file-system')

const MusicFile = require('./music-file')
const MusicAlbum = require('./music-album')
const MusicArtist = require('./music-artist')

class Organizer {
    constructor(durationLookup) {
        this.mediaRoot = settings.mediaRoot
        this.building = false
        this.startTime = null
        this.endTime = null
        this.rebuildCount = 0
        this.totalSongCount = 0
        this.durationLookup = null
        this.emptyThumbnailLookup = null
        this.extractedCovers = {}
        this.coverArts = {
            list: [],
            lookup: {},
        }
        this.files = {
            list: [],
            lookup: {},
        }
        this.songs = {
            list: [],
            lookup: {},
        }
        this.albums = {
            list: [],
            lookup: {},
        }
        this.categories = {
            list: [],
            lookup: {},
        }
    }

    status() {
        return {
            building: this.building,
            rebuildCount: this.rebuildCount,
            startTime: this.startTime,
            endTime: this.endTime,
            totalSongCount: this.totalSongCount,
        }
    }

    organize() {
        this.building = true
        this.rebuildCount = 0
        this.startTime = new Date()
        this.endTime = null
        this.totalSongCount = 0

        util.log(`Reading all files from media root.`)
        return new Promise(async (resolve, reject) => {
            await this.readLookups()
            await this.scanDirectory()
            await this.filter()
            await this.parseFilesToSongs()
            await this.sortSongs()
            await this.inspectFiles()
            await this.assignCoverArt()
            await this.organizeAlbums()
            await this.organizeCategories()

            let result = {
                songs: this.songs,
                albums: this.albums,
                categories: this.categories,
            }
            this.endTime = new Date()
            let timeSpent = (this.endTime.getTime() - this.startTime.getTime()) / 1000
            this.building = false
            util.log(`Finished organizing for catalog build in ${Math.floor(timeSpent / 60)} minutes and ${Math.floor(timeSpent % 60)} seconds`)
            resolve(result)
        })
    }

    async readLookups() {
        this.durationLookup = await fileSystem.readJsonFile(settings.durationLookupPath)
        this.emptyThumbnailLookup = await fileSystem.readJsonFile(settings.emptyThumbnailLookupPath)
    }

    scanDirectory() {
        return new Promise((resolve, reject) => {
            recurse(this.mediaRoot, (err, files) => {
                if (err) {
                    return reject(err)
                }
                this.files.list = files
                util.log(`Found ${this.files.list.length} files in the entire library`)
                resolve()
            })
        })
    }

    filter() {
        return new Promise((resolve) => {
            this.files.list = this.files.list.filter((file) => {
                if (file.includes('.snowgloo/thumbnails/')) {
                    if (file.includes('/embedded/')) {
                        let parts = file.split('/')
                        this.extractedCovers[parts[parts.length - 1].split('.')[0]] = file
                    }
                    return false
                }
                if (file.includes('.jpg') || file.includes('.png') || file.includes('.jpeg')) {
                    if (!file.toLowerCase().includes('small')) {
                        this.coverArts.list.push(file)
                    }
                    return false
                }
                if (!file.includes('.mp3')) {
                    return false
                }
                return true
            })
            util.log(`Filtered down to ${this.files.list.length} songs to process`)
            resolve()
        })
    }

    parseFilesToSongs() {
        return new Promise((resolve) => {
            this.songs.list = this.files.list.map((file) => {
                let song = new MusicFile(file)
                if (_.has(this.songs.lookup, song.Id)) {
                    console.error('Duplicate song ID ' + song.LocalFilePath + ' and ' + this.songs.lookup[song.Id].LocalFilePath)
                }
                this.songs.lookup[song.Id] = song
                return song.Id
            })
            resolve()
        })
    }

    sortSongs() {
        return new Promise((resolve) => {
            this.songs.list = this.songs.list.sort((aId, bId) => {
                let a = this.songs.lookup[aId]
                let b = this.songs.lookup[bId]
                if (a.Artist.toLowerCase() !== b.Artist.toLowerCase()) {
                    return a.Artist.toLowerCase() > b.Artist.toLowerCase() ? 1 : -1
                }
                if (a.Album.toLowerCase() !== b.Album.toLowerCase()) {
                    return a.Album.toLowerCase() > b.Album.toLowerCase() ? 1 : -1
                }
                if (a.Disc !== b.Disc) {
                    return a.Disc > b.Disc ? 1 : -1
                }
                return a.Track > b.Track ? 1 : -1
            })
            resolve()
        })
    }

    inspectFiles() {
        return new Promise(async (resolve) => {
            this.rebuildCount = 0
            this.totalSongCount = this.songs.list.length
            const notify = 1000
            for (let ii = 0; ii < this.totalSongCount; ii++) {
                this.rebuildCount++
                if (this.rebuildCount === 1 || this.rebuildCount % notify === 0 || this.rebuildCount === this.totalSongCount) {
                    util.log(`Reading file ${this.rebuildCount} out of ${this.totalSongCount}`)
                }
                let songId = this.songs.list[ii]
                let embeddedCover = this.extractedCovers[songId]
                let duration = this.durationLookup[songId]
                let emptyCover = this.emptyThumbnailLookup[songId]
                this.songs.lookup[songId].populateMetadata(emptyCover ? null : embeddedCover, duration)
            }
            return resolve()
        })
    }

    assignCoverArt() {
        return new Promise((resolve) => {
            for (let coverArt of this.coverArts.list) {
                let artFile = new MusicFile(coverArt)
                if (!_.has(this.coverArts.lookup, artFile.AlbumSlug)) {
                    this.coverArts.lookup[artFile.AlbumSlug] = `${settings.mediaServer}${coverArt}`
                }
            }
            for (let songId of this.songs.list) {
                let song = this.songs.lookup[songId]
                if (_.has(this.coverArts.lookup, song.AlbumSlug)) {
                    song.AlbumCoverArt = this.coverArts.lookup[song.AlbumSlug]
                }
                song.CoverArt = song.EmbeddedCoverArt ? song.EmbeddedCoverArt : song.AlbumCoverArt
                this.songs.lookup[songId] = song
            }
            resolve()
        })
    }

    organizeAlbums() {
        return new Promise((resolve) => {
            for (let songId of this.songs.list) {
                let song = this.songs.lookup[songId]
                if (!_.has(this.albums.lookup, song.AlbumSlug)) {
                    const album = new MusicAlbum(song, this.coverArts.lookup[song.AlbumSlug])
                    if (album.ReleaseYear === 9999) {
                        throw new Error(`Album has no defined year ${JSON.stringify(song)}`)
                    }
                    this.albums.lookup[song.AlbumSlug] = album
                    this.albums.list.push(song.AlbumSlug)
                }
                this.albums.lookup[song.AlbumSlug].Songs.push(song.Id)
            }
            this.albums.list = util.alphabetize(this.albums.list)
            resolve()
        })
    }

    organizeCategories() {
        return new Promise((resolve) => {
            for (let songId of this.songs.list) {
                let song = this.songs.lookup[songId]
                if (!_.has(this.categories.lookup, song.Kind)) {
                    this.categories.lookup[song.Kind] = {
                        artists: {
                            list: [],
                            lookup: {},
                        },
                    }
                    this.categories.list.push(song.Kind)
                }
                if (!_.has(this.categories.lookup[song.Kind].artists.lookup, song.Artist)) {
                    this.categories.lookup[song.Kind].artists.list.push(song.Artist)
                    this.categories.lookup[song.Kind].artists.lookup[song.Artist] = new MusicArtist(song)
                }
            }
            for (let category of this.categories.list) {
                this.categories.lookup[category].artists.list = util.alphabetize(this.categories.lookup[category].artists.list)
                this.categories.lookup[category].Kind = this.categories.lookup[category].artists.list.length === 1 && this.categories.lookup[category].artists.list[0] === category ? 'ArtistView' : 'ArtistList'
                this.categories.lookup[category].Name = category
            }
            this.categories.list = this.categories.list.sort()
            resolve()
        })
    }
}

module.exports = Organizer
