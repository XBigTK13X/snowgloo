const settings = require('./settings')
const inspect = require('./inspect')

class MusicFile {
    constructor(path) {
        const parts = path.split('/')
        this.LocalFilePath = path
        this.AudioUrl = `${settings.mediaServer}${path.replace(/#/g, '%23')}`
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
            let year = albumParts.pop().split(')')[0]
            this.ReleaseYear = parseInt(year.split('.')[0], 10)
            this.ReleaseYearSort = parseFloat(year)
            this.Album = albumParts.join('(')
        }
        this.DisplayAlbum = this.Album
        this.Artist = parts[parts.length - 3]
        this.DisplayArtist = this.Artist
        if (this.Kind === 'Artist') {
            if (this.Artist === 'Single' || this.Artist === 'Collab' || this.Artist === 'Special') {
                this.SubKind = parts[parts.length - 3]
                this.Artist = parts[parts.length - 4]
            }
        }
        let trackAndTitle = parts[parts.length - 1]
            .split('.')
            .slice(0, -1)
            .join('.')
        if (!trackAndTitle.includes(' - ')) {
            this.Title = trackAndTitle
        } else {
            let titleParts = trackAndTitle.split(' - ')
            if (titleParts[0].includes('D')) {
                let discAndTrackParts = titleParts[0].split('D')[1].split('T')
                this.Disc = parseInt(discAndTrackParts.shift(), 10)
                this.Track = parseInt(discAndTrackParts.shift(), 10)
                this.Title = discAndTrackParts.join(' - ')
            } else {
                this.Disc = 1
                this.Track = parseInt(titleParts[0], 10)
                let parts2 = trackAndTitle.split(' - ')
                parts2.shift()
                this.Title = parts2.join(' - ')
            }
            if (this.Kind === 'Compilation') {
                this.Title = titleParts[1]
                this.DisplayAlbum = titleParts[2]
                this.DisplayArtist = this.Album
            }
        }
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
