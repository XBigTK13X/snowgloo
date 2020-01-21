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
        this.database = database.getInstance('catalog')
        this.building = false
        this.rebuildCount = 0
        this.totalCount = 0
    }

    status() {
        return {
            building: this.building,
            rebuildCount: this.rebuildCount,
            totalCount: this.totalCount,
        }
    }

    build(force) {
        console.log('Reading catalog into memory')
        return new Promise((resolve, reject) => {
            this.database.read().then(workingSet => {
                if (!force && !this.database.isEmpty() && !settings.ignoreDatabaseCache) {
                    console.log(`Using ${workingSet.files.length} cached database results`)
                    this.workingSet = workingSet
                    return resolve(this.workingSet.files)
                }
                workingSet = {}
                let startTime = new Date().getTime()
                this.building = true
                this.rebuildCount = 0
                this.totalCount = 0
                console.log(`Rebuilding cache from files`)
                let coverArts = []
                let albumCoverArts = {}
                recurse(this.mediaRoot, (err, files) => {
                    if (err) {
                        return reject(err)
                    }
                    console.log(`Found ${files.length} files in the entire library`)
                    files = files
                        .filter(x => {
                            if (x.includes('.jpg') || x.includes('.png') || x.includes('.jpeg')) {
                                coverArts.push(x)
                                return false
                            }
                            if (x.includes('.txt')) {
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
                    const batchSize = 300
                    let promiseBatches = []
                    for (let ii = 0; ii < files.length; ii += batchSize) {
                        promiseBatches.push(() => {
                            if (ii % batchSize === 0 || ii >= files.length - 1) {
                                console.log(`Reading file ${ii} of ${files.length} [${files[ii].LocalFilePath}]`)
                                this.rebuildCount = ii
                                this.totalCount = files.length
                            }
                            let internalPromises = []
                            for (let jj = 0; jj < batchSize; jj++) {
                                if (ii + jj < files.length) {
                                    internalPromises.push(files[ii + jj].readInfo())
                                }
                            }
                            return Promise.all(internalPromises)
                        })
                    }
                    const serialReads = promiseBatches.reduce((m, p) => m.then(v => Promise.all([...v, p()])), Promise.resolve([]))
                    serialReads
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
                            workingSet = {
                                files,
                                albumCoverArts,
                            }
                        })
                        .then(() => {
                            let albums = {
                                list: [],
                                lookup: {},
                            }
                            workingSet.files.forEach(file => {
                                if (!_.has(albums.lookup, file.AlbumSlug)) {
                                    albums.lookup[file.AlbumSlug] = {
                                        Album: file.Album,
                                        Artist: file.Artist,
                                        ReleaseYear: file.ReleaseYear,
                                        ReleaseYearSort: file.ReleaseYearSort,
                                        Songs: [],
                                        AlbumSlug: file.AlbumSlug,
                                        CoverArt: workingSet.albumCoverArts[file.AlbumSlug],
                                        Kind: file.Kind,
                                        SubKind: file.SubKind,
                                    }
                                    albums.list.push(file.AlbumSlug)
                                }
                                albums.lookup[file.AlbumSlug].Songs.push(file)
                            })
                            albums.list = alphabetize(albums.list)
                            workingSet.albums = albums
                        })
                        .then(() => {
                            let artists = {
                                list: [],
                                lookup: {},
                            }
                            workingSet.files.forEach(file => {
                                if (!_.has(artists.lookup, file.Artist)) {
                                    artists.lookup[file.Artist] = {
                                        Artist: file.Artist,
                                    }
                                    artists.list.push(file.Artist)
                                }
                            })
                            artists.list = alphabetize(artists.list)
                            workingSet.artists = artists
                        })
                        .then(() => {
                            return this.database.write(workingSet)
                        })
                        .then(() => {
                            let timeSpent = (new Date().getTime() - startTime) / 1000
                            this.building = false
                            console.log(`Finished building catalog in ${Math.floor(timeSpent / 60)} minutes and ${Math.floor(timeSpent % 60)} seconds`)
                            this.workingSet = workingSet
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
                    if (albums.lookup[a].ReleaseYear === albums.lookup[b].ReleaseYear) {
                        return albums.lookup[a].ReleaseYearSort > albums.lookup[b].ReleaseYearSort ? 1 : -1
                    }
                    return albums.lookup[a].ReleaseYear > albums.lookup[b].ReleaseYear ? 1 : -1
                })
            let lists = {
                Main: [],
                Special: [],
                Single: [],
                Collab: [],
            }
            let albumLookup = albumList.reduce((result, next) => {
                if (_.has(lists, albums.lookup[next].SubKind)) {
                    lists[albums.lookup[next].SubKind].push(next)
                } else {
                    lists.Main.push(next)
                }
                result[next] = albums.lookup[next]
                return result
            }, {})

            return resolve({
                lists: lists,
                lookup: albumLookup,
                listKinds: ['Main', 'Single', 'Special', 'Collab'],
            })
        })
    }
}

let instance

if (!instance) {
    instance = new Catalog()
}

module.exports = instance
