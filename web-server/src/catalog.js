const recurse = require('recursive-readdir')
const _ = require('lodash')
const path = require('path')

const settings = require('./settings')
const database = require('./database')

const MusicFile = require('./music-file')

const alphabetize = (items, property) => {
    if (property) {
        return items.sort((a, b) => {
            return a[property].toLowerCase() > b[property].toLowerCase() ? 1 : -1
        })
    }
    return items.sort((a, b) => {
        return a.toLowerCase() > b.toLowerCase() ? 1 : -1
    })
}

class Catalog {
    constructor() {
        this.mediaRoot = settings.mediaRoot
        this.workingSet = {}
        this.database = database
    }

    build() {
        return new Promise((resolve, reject) => {
            this.database.read().then(workingSet => {
                if (!this.database.isEmpty() && !settings.ignoreDatabaseCache) {
                    console.log(`Using ${workingSet.files.length} cached database results`)
                    this.workingSet = workingSet
                    return resolve(workingSet.files)
                }
                console.log('Rebuilding cache from files')
                let coverArts = []
                let albumCoverArts = {}
                recurse(this.mediaRoot, (err, files) => {
                    if (err) {
                        return reject(err)
                    }
                    files = files
                        .filter(x => {
                            if (x.includes('Anime/') || x.includes('Compilation/') || x.includes('Game/')) {
                                return false
                            }
                            if (x.includes('.jpg') || x.includes('.png') || x.includes('.jpeg')) {
                                coverArts.push(x)
                                return false
                            }
                            return true
                        })
                        .map(file => {
                            return new MusicFile(file)
                        })
                        .sort((a, b) => {
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
                    Promise.all(
                        files.map(x => {
                            return x.readInfo()
                        })
                    )
                        .then(() => {
                            return new Promise(resolve => {
                                coverArts.forEach(coverArt => {
                                    let artDir = path.dirname(coverArt)
                                    files.forEach(file => {
                                        if (_.has(albumCoverArts, file.AlbumSlug)) {
                                            file.CoverArt = albumCoverArts[file.AlbumSlug]
                                        }
                                        if (file.LocalFilePath.includes(artDir)) {
                                            file.CoverArt = `${settings.mediaServer}${coverArt}`
                                            albumCoverArts[file.AlbumSlug] = `${settings.mediaServer}${coverArt}`
                                        }
                                    })
                                    resolve()
                                })
                            })
                        })
                        .then(() => {
                            this.workingSet = {
                                files,
                                albumCoverArts,
                            }
                        })
                        .then(() => {
                            let albums = {
                                list: [],
                                lookup: {},
                            }
                            this.workingSet.files.forEach(file => {
                                if (!_.has(albums.lookup, file.AlbumSlug)) {
                                    albums.lookup[file.AlbumSlug] = {
                                        Album: file.Album,
                                        Artist: file.Artist,
                                        ReleaseYear: file.ReleaseYear,
                                        Songs: [],
                                        AlbumSlug: file.AlbumSlug,
                                        CoverArt: this.workingSet.albumCoverArts[file.AlbumSlug],
                                    }
                                    albums.list.push(file.AlbumSlug)
                                }
                                albums.lookup[file.AlbumSlug].Songs.push(file)
                            })
                            albums.list = alphabetize(albums.list)
                            this.workingSet.albums = albums
                        })
                        .then(() => {
                            let artists = {
                                list: [],
                                lookup: {},
                            }
                            this.workingSet.files.forEach(file => {
                                if (!_.has(artists.lookup, file.Artist)) {
                                    artists.lookup[file.Artist] = {
                                        Artist: file.Artist,
                                    }
                                    artists.list.push(file.Artist)
                                }
                            })
                            artists.list = alphabetize(artists.list)
                            this.workingSet.artists = artists
                        })
                        .then(() => {
                            return this.database.write(this.workingSet)
                        })
                        .then(() => {
                            resolve(this.workingSet)
                        })
                })
            })
        })
    }

    getSongs() {
        return new Promise(resolve => {
            resolve(this.workingSet.files)
        })
    }

    getArtists() {
        return new Promise(resolve => {
            return resolve(this.workingSet.artists)
        })
    }

    getAlbum(albumSlug) {
        return new Promise(resolve => {
            let album = this.workingSet.albums.lookup[albumSlug]
            return resolve(album)
        })
    }

    getAlbums(artist) {
        return new Promise(resolve => {
            if (!artist) {
                return resolve(this.workingSet.albums)
            }
            let albums = this.workingSet.albums
            let albumList = albums.list
                .filter(x => {
                    return albums.lookup[x].Artist === artist
                })
                .sort((a, b) => {
                    return albums.lookup[a].ReleaseYear > albums.lookup[b].ReleaseYear ? 1 : -1
                })
            let albumLookup = albumList.reduce((result, next) => {
                result[next] = albums.lookup[next]
                return result
            }, {})
            return resolve({
                list: albumList,
                lookup: albumLookup,
            })
        })
    }
}

let instance

if (!instance) {
    instance = new Catalog()
}

module.exports = instance
