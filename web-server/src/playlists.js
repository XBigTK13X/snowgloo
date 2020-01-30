const path = require('path')
const database = require('./database')
const recurse = require('recursive-readdir')
const uuid = require('uuid/v4')
const _ = require('lodash')
const settings = require('./settings')

class Playlists {
    constructor() {
        this.databaseRoot = path.join(settings.databaseDirectory, 'playlist')
        this.catalog = null
        this.playlists = {
            lookup: {},
            list: [],
        }
    }

    getDatabase(playlistId) {
        return database.getInstance(`playlist/${playlistId}`)
    }

    build(catalog) {
        return new Promise(resolve => {
            this.catalog = catalog
            recurse(this.databaseRoot, (err, files) => {
                let readPromises = files.map(file => {
                    let playlistId = file
                        .split('/')
                        .pop()
                        .split('.')[0]
                    let database = this.getDatabase(playlistId)
                    return database.read()
                })
                Promise.all(readPromises).then(playlists => {
                    playlists.forEach(playlist => {
                        this.playlists.lookup[playlist.id] = playlist
                        this.playlists.list.push(playlist)
                    })
                    console.log(`Loaded ${playlists.length} playlists from disk`)
                })
            })
        })
    }

    write(playlist) {
        if (!playlist.id) {
            playlist.id = uuid()
        }

        let songIds = []
        playlist.songs.forEach(song => {
            songIds.push(song.Id)
        })

        playlist.songs = songIds

        if (!_.has(this.playlists.lookup, playlist.id)) {
            this.playlists.list.push(playlist)
        }
        this.playlists.lookup[playlist.id] = playlist
        this.playlists.list.sort((a, b) => {
            return a.name > b.name ? 1 : -1
        })
        return this.getDatabase(playlist.id).write(playlist)
    }

    read(playlistId) {
        return new Promise(resolve => {
            let playlist = { ...this.playlists.lookup[playlistId] }
            this.catalog.getSongs(playlist.songs).then(songs => {
                playlist.songs = songs
                resolve(playlist)
            })
        })
    }

    readAll() {
        return this.playlists
    }
}

let instance
if (!instance) {
    instance = new Playlists()
}

module.exports = instance
