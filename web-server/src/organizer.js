const recurse = require('recursive-readdir')
const _ = require('lodash')
const path = require('path')

const settings = require('./settings')
const database = require('./database')
const util = require('./util')

const MusicFile = require('./music-file')
const MusicAlbum = require('./music-album')
const MusicArtist = require('./music-artist')

const SHALLOW = "shallow"
const DEEP = "deep"

class Organizer {
    constructor(mediaRoot){
        this.mediaRoot = mediaRoot
        this.depth = "shallow"
        this.building = false
        this.rebuildCount = 0
        this.totalCount = 0
    }

    status(){
        return {
            building: this.building,
            rebuildCount: this.rebuildCount,
            totalCount: this.totalCount,
        }
    }

    shallow(){
        return this.organize(SHALLOW)
    }

    deep(){
        return this.organize(DEEP)
    }

    organize(depth) {
        this.depth = depth
        this.coverArts = {
            list: [],
            lookup: {}
        }
        this.files = {
            list: [],
            lookup: {}
        }
        this.songs = {
            list: [],
            lookup: {}
        }
        this.albums = {
            list: [],
            lookup: {}
        }
        this.artists = {
            list: [],
            lookup: {}
        },
        this.categories = {
            list: [],
            lookup: {}
        }
        this.building = true
        this.rebuildCount = 0
        this.totalCount = 0
        this.startTime = new Date().getTime()

        console.log(`Reading all files from media root. Making a ${depth} pass`)
        return new Promise((resolve, reject) => {
            return this.scanDirectory()
                .then(()=>{
                    return this.filter()
                })
                .then(()=>{
                    return this.parseFilesToSongs()
                })
                .then(()=>{
                    return this.sortSongs()
                })
                .then(()=>{
                    return this.inspectFiles()
                })
                .then(()=>{
                    return this.assignCoverArt()
                })
                .then(()=>{
                    return this.organizeAlbums()
                })
                .then(()=>{
                    return this.organizeArtists()
                })
                .then(()=>{
                    let result = {
                        songs: this.songs,
                        albums: this.albums,
                        artists: this.artists,
                        categories: this.categories
                    }
                    let timeSpent = (new Date().getTime() - this.startTime) / 1000
                    this.building = false
                    console.log(`Finished ${depth} pass for catalog build in ${Math.floor(timeSpent / 60)} minutes and ${Math.floor(timeSpent % 60)} seconds`)
                    resolve(result)
                })
        })
    }

    scanDirectory(){
        return new Promise((resolve,reject) => {
            recurse(this.mediaRoot, (err, files) => {
                if (err) {
                    return reject(err)
                }
                this.files.list = files
                console.log(`Found ${this.files.list.length} files in the entire library`)
                resolve()
            })
        })
    }

    filter(){
        return new Promise(resolve=>{
            this.files.list = this.files.list.filter(file => {
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
            console.log(`Filtered down to ${this.files.list.length} songs to process`)
            resolve()
        })
    }

    parseFilesToSongs(){
        return new Promise(resolve=>{
            this.songs.list = this.files.list.map(file => {
                let song = new MusicFile(file)
                if (this.depth === DEEP) {
                    if (_.has(this.songs.lookup, song.Id)) {
                        console.error('Duplicate song ID ' + song.LocalFilePath + ' and ' + fileLookup[song.Id])
                    } else {
                        this.songs.lookup[song.Id] = song.LocalFilePath
                    }
                }
                return song
            })
            resolve();
        })
    }

    sortSongs(){
        return new Promise(resolve=>{
            this.songs.list = this.songs.list.sort((a, b) => {
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
            resolve();
        })
    }

    inspectFiles(){
        if(this.depth == SHALLOW){
            return Promise.resolve();
        }
        return new Promise(resolve=>{
            const batchSize = 8
            let promiseBatches = []
            for (let ii = 0; ii < this.songs.list.length; ii += batchSize) {
                promiseBatches.push(() => {
                    let internalPromises = []
                    for (let jj = 0; jj < batchSize; jj++) {
                        if (ii + jj < this.songs.list.length) {
                            internalPromises.push(this.songs.list[ii + jj].parseMetadata())
                        }
                    }
                    return Promise.all(internalPromises)
                })
            }
            this.rebuildCount = 0
            this.totalCount = promiseBatches.length
            const notify = 100
            return promiseBatches.reduce((m, p) => {
                return m.then(v => {
                    this.rebuildCount++
                    if (this.rebuildCount === 1 || this.rebuildCount % notify === 0 || this.rebuildCount === this.totalCount - 1) {
                        console.log(`Reading file batch ${this.rebuildCount}/${this.totalCount}`)
                    }
                    return Promise.all([...v, p()])
                })
            }, Promise.resolve([]))
            .then(()=>{
                resolve()
            })
        })
    }

    assignCoverArt(){
        return new Promise(resolve => {
            this.coverArts.list.forEach(coverArt => {
                let artFile = new MusicFile(coverArt)
                if(!_.has(this.coverArts.lookup, artFile.AlbumSlug)){
                    this.coverArts.lookup[artFile.AlbumSlug] = `${settings.mediaServer}${coverArt}`
                }
            })
            this.songs.list.forEach(song => {
                if (_.has(this.coverArts.lookup, song.AlbumSlug)) {
                    song.AlbumCoverArt = this.coverArts.lookup[song.AlbumSlug]
                }
                song.CoverArt = song.EmbeddedCoverArt ? song.EmbeddedCoverArt : song.AlbumCoverArt
                if (this.depth === DEEP && !song.CoverArt) {
                    console.error('No cover art found for ' + song.LocalFilePath)
                }
            })
            resolve()
        })
    }

    organizeAlbums(){
        return new Promise(resolve=>{
            this.songs.list.forEach(song => {
                if (!_.has(this.albums.lookup, song.AlbumSlug)) {
                    const album = new MusicAlbum(song, this.coverArts.lookup[song.AlbumSlug])
                    if (album.ReleaseYear === 9999) {
                        throw new Error(`Album has no defined year ${JSON.stringify(song)}`)
                    }
                    this.albums.lookup[song.AlbumSlug] = album
                    this.albums.list.push(song.AlbumSlug)
                }
                this.albums.lookup[song.AlbumSlug].Songs.push(song)
            })
            this.albums.list = util.alphabetize(this.albums.list)
            resolve()
        })
    }

    organizeArtists(){
        return new Promise(resolve=>{
            this.songs.list.forEach(song => {
                if (!_.has(this.artists.lookup, song.Artist)) {
                    this.artists.lookup[song.Artist] = new MusicArtist(song)
                    this.artists.list.push(song.Artist)
                }
            })
            this.artists.list = util.alphabetize(this.artists.list)
            resolve()
        })
    }
}

module.exports = Organizer
