const recurse = require('recursive-readdir')
const _ = require('lodash')
const path = require('path')

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
        this.building = false
        this.rebuildCount = 0
        this.totalCount = 0
    }

    status() {
        return this.organizer.status()
    }

    build(force) {
        return new Promise((resolve)=>{
            console.log('Reading catalog into memory')
            return this.database.read().then(persistedMedia => {
                if (!force && !this.database.isEmpty() && !settings.ignoreDatabaseCache) {
                    console.log(`Using ${persistedMedia.songs.list.length} ingested songs from the database`)
                    this.media = persistedMedia
                    //Need to rehydrate class instances from JSON, otherwise instance methods won't work (i.e. search)
                    this.media.songs.list = this.media.songs.list.map(song => {
                        let result = new MusicFile(song.LocalFilePath)
                        result.CoverArt = song.CoverArt
                        result.EmbeddedCoverArt = song.EmbeddedCoverArt
                        result.AlbumCoverArt = song.AlbumCoverArt
                        this.media.songs.lookup[song.Id] = result
                        return result
                    })
                    this.media.albums.list.forEach(albumName => {
                        const album = this.media.albums.lookup[albumName]
                        this.media.albums.lookup[albumName] = new MusicAlbum(album, album.CoverArt)
                    })
                    this.media.categories.list.forEach(category=>{
                        this.media.categories.lookup[category].artists.list.forEach(artistName => {
                            const artist = this.media.categories.lookup[category].artists.lookup[artistName]
                            this.media.categories.lookup[category].artists.lookup[artistName] = new MusicArtist(artist)
                        })
                    })
                    resolve(this.media)
                }
                else {
                    console.log('Rebuilding the catalog from scratch')
                    this.organizer = new Organizer(settings.mediaRoot)
                    return this.organizer.shallow()
                    .then((media=>{
                        this.media = {...media}
                        return this.organizer.deep()
                    }))
                    .then((media)=>{
                        this.media = {...media};
                        return this.database.write(this.media)
                    })
                    .then(()=>{
                        console.log("Organized catalog persisted to disk");
                        resolve(this.media)
                    })
                }
            });
        })
    }

    search(query) {
        query = util.searchify(query)
        return new Promise(resolve => {
            let results = {
                Songs: [],
                Artists: [],
                Albums: [],
                ItemCount: 0,
            }
            this.media.songs.list.forEach(song => {
                if (song.matches(query)) {
                    results.Songs.push(song)
                    results.ItemCount++
                }
            })
            this.media.albums.list.forEach(albumSlug => {
                const album = this.media.albums.lookup[albumSlug]
                if (album.matches(query)) {
                    results.Albums.push(album)
                    results.ItemCount++
                }
            })
            this.media.categories.list.forEach(category=>{
                this.media.categories.lookup[category].artists.list.forEach(artistName => {
                    const artist = this.media.categories.lookup[category].artists.lookup[artistName]
                    if (artist.matches(query)) {
                        results.Artists.push(artist)
                        results.ItemCount++
                    }
                })
            })
            resolve(results)
        })
    }

    getSongs(songIds) {
        return new Promise(resolve => {
            if (!songIds) {
                return resolve(this.media.songs.list)
            }

            return resolve(
                songIds.map(songId => {
                    return this.media.songs.lookup[songId]
                })
            )
        })
    }

    getArtists(category) {
        return new Promise(resolve => {
            if(!_.has(this.media.categories.lookup, category)){
                return null
            }
            return resolve(this.media.categories.lookup[category].artists)
        })
    }

    getAlbum(albumSlug) {
        return new Promise(resolve => {
            let album = this.media.albums.lookup[albumSlug]
            return resolve(album)
        })
    }

    getAlbums(artist) {
        return new Promise(resolve => {
            if (!artist) {
                return resolve(this.media.albums)
            }
            let albums = {...this.media.albums}
            let albumList = albums.list
                .filter(x => {
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
}

let instance

if (!instance) {
    instance = new Catalog()
}

module.exports = instance
