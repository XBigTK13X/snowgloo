const settings = require('./settings')
const inspect = require('./inspect')

class MusicFile {
    constructor(path) {
        const parts = path.split('/')
        this.LocalFilePath = path
        this.AudioUrl = `${settings.mediaServer}${path}`
        this.Kind = 'Artist'
        this.SubKind = null
        if (path.includes('/Anime/')) {
            this.Kind = 'Anime'
        } else if (path.includes('/Game')) {
            this.Kind = 'Game'
        } else if (path.includes('/Compilation')) {
            this.Kind = 'Compilation'
        }
        this.Album = parts[parts.length - 2]
        this.ReleaseYear = 9999
        if (this.Album.includes('(') && this.Album.includes(')')) {
            let albumParts = this.Album.split('(')
            this.Album = albumParts[0]
            this.ReleaseYear = parseInt(albumParts[1].split(')')[0], 10)
        }
        this.Artist = parts[parts.length - 3]
        if (this.Kind === 'Artist') {
            if (this.Artist === 'Single' || this.Artist === 'Collab' || this.Artist === 'Special') {
                this.SubKind = parts[parts.length - 3]
                this.Artist = parts[parts.length - 4]
            }
        }
        this.AudioUrl = `${settings.mediaServer}${path}`
        let trackAndTitle = parts[parts.length - 1]
        if (!trackAndTitle.includes(' - ')) {
            console.log(path)
            this.Title = trackAndTitle
        } else {
            let titleParts = trackAndTitle.split(' - ')
            if (titleParts[0].includes('D')) {
                let discAndTrackParts = titleParts[0].split('D')[1].split('T')
                this.Disc = parseInt(discAndTrackParts[0], 10)
                this.Track = parseInt(discAndTrackParts[1], 10)
                this.Title = titleParts[1]
            } else {
                this.Disc = 1
                this.Track = parseInt(titleParts[0], 10)
                this.Title = titleParts[1]
            }
        }
        this.Title = this.Title.split('.')
            .slice(0, -1)
            .join('.')
        this.AlbumSlug = `${this.Album}-${this.Artist}`
    }

    readInfo() {
        return inspect.audio(this.LocalFilePath).then(data => {
            if (data.error) {
                console.error(this.LocalFilePath, data.error)
                throw data.error
            }
            this.Info = data
        })
    }
}

module.exports = MusicFile
