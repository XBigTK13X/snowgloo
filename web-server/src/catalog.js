const recurse = require('recursive-readdir')
const _ = require('lodash')
const path = require('path')

const settings = require('./settings')
const database = require('./database')

const MusicFile = require('./music-file')

const BUILD_PASSES = 2

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

    build(force, pass) {
        if (!pass) {
            pass = 1
            this.buildingSet = {}
        }
        console.log('Reading catalog into memory')
        return new Promise((resolve, reject) => {
            this.database.read().then(databaseWorkingSet => {
                if (!force && pass === 1 && !this.database.isEmpty() && !settings.ignoreDatabaseCache) {
                    console.log(`Using ${databaseWorkingSet.files.length} cached database results`)
                    this.workingSet = databaseWorkingSet
                    return resolve(this.workingSet.files)
                }
                let workingSet = this.buildingSet
                let startTime = new Date().getTime()
                this.building = true
                this.rebuildCount = 0
                this.totalCount = 0
                console.log(`Rebuilding cache from files - Pass #${pass} out of ${BUILD_PASSES}`)
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
                                if (!x.toLowerCase().includes('small')) {
                                    coverArts.push(x)
                                }
                                return false
                            }
                            if (!x.includes('.mp3')) {
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

                    let serialReads = Promise.resolve()
                    if (pass === 2) {
                        const batchSize = 300
                        let promiseBatches = []
                        for (let ii = 0; ii < files.length; ii += batchSize) {
                            promiseBatches.push(() => {
                                if (ii % batchSize === 0 || ii >= files.length - 2) {
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
                        serialReads = promiseBatches.reduce((m, p) => m.then(v => Promise.all([...v, p()])), Promise.resolve([]))
                    }

                    serialReads
                        .then(() => {
                            return new Promise(innerResolve => {
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
                                    innerResolve()
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
                            workingSet.filesLookup = {}
                            workingSet.files.forEach(file => {
                                workingSet.filesLookup[file.Id] = file
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
                            console.log(`Finished pass #${pass} out of ${BUILD_PASSES} for catalog build in ${Math.floor(timeSpent / 60)} minutes and ${Math.floor(timeSpent % 60)} seconds`)
                            this.workingSet = workingSet
                            resolve(this.workingSet)
                            if (pass < BUILD_PASSES) {
                                this.build(force, pass + 1)
                            }
                        })
                })
            })
        })
    }

    search(query) {
        query = query.toLowerCase().replace(/\s/g, '')
        return new Promise(resolve => {
            let results = {
                Songs: [],
                Artists: [],
                Albums: [],
                ItemCount: 0,
            }
            this.workingSet.files.forEach(file => {
                if (
                    file.Title.toLowerCase()
                        .replace(/\s/g, '')
                        .includes(query)
                ) {
                    results.Songs.push(file)
                    results.ItemCount++
                }
            })
            this.workingSet.albums.list.forEach(albumSlug => {
                if (
                    this.workingSet.albums.lookup[albumSlug].Album.toLowerCase()
                        .replace(/\s/g, '')
                        .includes(query)
                ) {
                    results.Albums.push(this.workingSet.albums.lookup[albumSlug])
                    results.ItemCount++
                }
            })
            this.workingSet.artists.list.forEach(artist => {
                if (
                    artist
                        .toLowerCase()
                        .replace(/\s/g, '')
                        .includes(query)
                ) {
                    results.Artists.push(this.workingSet.artists.lookup[artist])
                    results.ItemCount++
                }
            })
            resolve(results)
        })
    }

    getSongs(songIds) {
        return new Promise(resolve => {
            if (!songIds) {
                return resolve(this.workingSet.files)
            }

            return resolve(
                songIds.map(songId => {
                    return this.workingSet.filesLookup[songId]
                })
            )
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
