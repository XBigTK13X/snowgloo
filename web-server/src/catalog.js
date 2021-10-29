const _ = require('lodash')

const settings = require('./settings')
const database = require('./database')
const util = require('./util')

const Organizer = require('./organizer')
const MusicFile = require('./music-file')
const MusicAlbum = require('./music-album')
const MusicArtist = require('./music-artist')

class Catalog {
    constructor() {
        this.organizer = new Organizer(settings.mediaRoot)
        this.media = {}
        this.database = database.getInstance('catalog')
    }

    status() {
        return this.organizer.status()
    }

    build(force) {
        return new Promise((resolve) => {
            util.log('Reading catalog into memory')
            return this.database.read().then((persistedMedia) => {
                if (!force && !this.database.isEmpty() && !settings.ignoreDatabaseCache) {
                    util.log(`Using ${persistedMedia.songs.list.length} ingested songs from the database`)
                    this.media = persistedMedia
                    //Need to rehydrate class instances from JSON, otherwise instance methods won't work (i.e. search)
                    let songsLookup = {}
                    for (let songId of Object.keys(this.media.songs.lookup)) {
                        songsLookup[songId] = new MusicFile().rehydrate(this.media.songs.lookup[songId])
                    }
                    this.media.songs.lookup = songsLookup
                    for (let albumName of this.media.albums.list) {
                        const album = new MusicAlbum().rehydrate(this.media.albums.lookup[albumName])
                        this.media.albums.lookup[albumName] = album
                    }
                    for (let category of this.media.categories.list) {
                        for (let artistName of this.media.categories.lookup[category].artists.list) {
                            const artist = new MusicArtist().rehydrate(this.media.categories.lookup[category].artists.lookup[artistName])
                            this.media.categories.lookup[category].artists.lookup[artistName] = artist
                        }
                    }
                    resolve(this.media)
                } else {
                    util.log('Rebuilding the catalog from scratch')
                    let songLookup = this.media.songs ? _.cloneDeep(this.media.songs.lookup) : null
                    this.organizer = new Organizer(settings.mediaRoot, songLookup)
                    return this.organizer
                        .shallow()
                        .then((media) => {
                            this.media = _.cloneDeep(media)
                            return this.organizer.deep()
                        })
                        .then((media) => {
                            this.media = _.cloneDeep(media)
                            return this.database.write(this.media)
                        })
                        .then(() => {
                            util.log('Organized catalog persisted to disk')
                            resolve(this.media)
                        })
                }
            })
        })
    }

    search(query) {
        query = util.searchify(query)
        return new Promise((resolve) => {
            let results = {
                Songs: [],
                Artists: [],
                Albums: [],
                ItemCount: 0,
            }
            for (let songId of this.media.songs.list) {
                if (this.media.songs.lookup[songId].matches(query)) {
                    results.Songs.push(this.media.songs.lookup[songId])
                    results.ItemCount++
                }
            }
            for (let albumSlug of this.media.albums.list) {
                const album = this.media.albums.lookup[albumSlug]
                if (album.matches(query)) {
                    results.Albums.push(album)
                    results.ItemCount++
                }
            }
            for (let category of this.media.categories.list) {
                for (let artistName of this.media.categories.lookup[category].artists.list) {
                    const artist = this.media.categories.lookup[category].artists.lookup[artistName]
                    if (artist.matches(query)) {
                        results.Artists.push(artist)
                        results.ItemCount++
                    }
                }
            }
            resolve(results)
        })
    }

    getCategories() {
        return new Promise((resolve) => {
            resolve(this.media.categories)
        })
    }

    getSongs(songIds) {
        return new Promise((resolve) => {
            if (!songIds) {
                return resolve(
                    this.media.songs.list.map((songId) => {
                        return this.media.songs.lookup[songId]
                    })
                )
            }

            return resolve(
                songIds.map((songId) => {
                    return this.media.songs.lookup[songId]
                })
            )
        })
    }

    getArtists(category) {
        return new Promise((resolve) => {
            if (!_.has(this.media.categories.lookup, category)) {
                return null
            }
            return resolve(this.media.categories.lookup[category].artists)
        })
    }

    getAlbum(albumSlug) {
        return new Promise((resolve) => {
            let album = _.cloneDeep(this.media.albums.lookup[albumSlug])
            album.Songs = album.Songs.map((songId) => {
                return this.media.songs.lookup[songId]
            })
            return resolve(album)
        })
    }

    getAlbums(artist) {
        return new Promise((resolve) => {
            if (!artist) {
                return resolve(this.media.albums)
            }
            let albums = { ...this.media.albums }
            let albumList = albums.list
                .filter((x) => {
                    return albums.lookup[x].Artist === artist
                })
                .sort((a, b) => {
                    if (albums.lookup[a].ReleaseYear === albums.lookup[b].ReleaseYear) {
                        if (albums.lookup[a].ReleaseYearSort === albums.lookup[b].ReleaseYearSort) {
                            return albums.lookup[a].Album > albums.lookup[b].Album ? 1 : -1
                        }
                        return albums.lookup[a].ReleaseYearSort > albums.lookup[b].ReleaseYearSort ? 1 : -1
                    }
                    return albums.lookup[a].ReleaseYear > albums.lookup[b].ReleaseYear ? 1 : -1
                })
            let lists = {
                Album: [],
                Special: [],
                Single: [],
                Collab: [],
            }
            let albumLookup = albumList.reduce((result, next) => {
                if (_.has(lists, albums.lookup[next].SubKind)) {
                    lists[albums.lookup[next].SubKind].push(next)
                } else {
                    lists.Album.push(next)
                }
                result[next] = albums.lookup[next]
                return result
            }, {})

            return resolve({
                lists: lists,
                lookup: albumLookup,
                listKinds: ['Album', 'Single', 'Special', 'Collab'],
            })
        })
    }

    getRandomList() {
        return new Promise((resolve) => {
            let randomCount = settings.randomListSize
            let maxAttempts = settings.randomListSize * 20
            let songs = []
            let artistDedupe = {}
            let albumDedupe = {}
            while (randomCount > 0 && maxAttempts > 0) {
                let album = this.media.albums.lookup[_.sample(this.media.albums.list)]
                let songId = _.sample(album.Songs)
                let song = this.media.songs.lookup[songId]
                if (!_.has(artistDedupe, song.DisplayArtist) && !_.has(albumDedupe, song.DisplayAlbum)) {
                    artistDedupe[song.DisplayArtist] = 1
                    albumDedupe[song.DisplayAlbum] = 1
                    songs.push(song)
                    randomCount--
                }
                maxAttempts--
            }
            resolve(songs)
        })
    }
}

let instance

if (!instance) {
    instance = new Catalog()
}

module.exports = instance
