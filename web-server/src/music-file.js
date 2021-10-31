const settings = require('./settings')
const util = require('./util')

class MusicFile {
    constructor(path) {
        if (!path) {
            return this
        }
        const parts = path.split('/')

        this.LocalFilePath = path
        this.AudioUrl = `${settings.mediaServer}${encodeURI(path).replace(/#/g, '%23')}`
        this.Kind = path.replace(settings.mediaRoot, '').split('/')[1]
        this.SubKind = null
        this.CoverArt = null
        this.AlbumCoverArt = null
        this.EmbeddedCoverArt = null
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
        if (this.Artist === 'Single' || this.Artist === 'Collab' || this.Artist === 'Special') {
            this.SubKind = parts[parts.length - 3]
            this.Artist = parts[parts.length - 4]
            this.DisplayArtist = this.Artist
        }
        let trackAndTitle = parts[parts.length - 1].split('.')
        //Remove the 'adjusted' keyword and file extension
        trackAndTitle.pop()
        trackAndTitle.pop()
        trackAndTitle = trackAndTitle.join('.')
        this.Title = trackAndTitle
        //Everything that has been fingerprinted should have at least one dash
        if (trackAndTitle.includes(' - ')) {
            let titleParts = trackAndTitle.split(' - ')
            this.TitleParts = titleParts
            this.Id = titleParts.pop()
            if (titleParts[0].includes('D')) {
                let discAndTrackParts = titleParts[0].split('D')[1].split('T')
                this.Disc = parseInt(discAndTrackParts[0], 10)
                this.Track = parseInt(discAndTrackParts[1], 10)
                titleParts.shift()
                this.Title = titleParts.join(' - ')
            } else {
                this.Disc = 1
                this.Track = parseInt(titleParts.shift(), 10)
                this.Title = titleParts.join(' - ')
            }
            if (titleParts.length > 1) {
                this.DisplayArtist = titleParts.pop()
                this.Title = titleParts.join(' - ')
            }
            if (this.DisplayAlbum.includes('Vol. ')) {
                let albumParts = this.DisplayAlbum.split(' - ')
                albumParts.shift()
                this.DisplayAlbum = albumParts.join(' - ')
            }
        }

        this.Album = this.Album.trim()
        this.Artist = this.Artist.trim()
        this.Title = this.Title.trim()
        this.DisplayAlbum = this.DisplayAlbum.trim()
        this.DisplayArtist = this.DisplayArtist.trim()
        this.SearchTerms = util.searchify(this.Title + this.DisplayArtist)
        this.AlbumSlug = `${this.Album}-${this.Artist}`
    }

    rehydrate(instance) {
        Object.assign(this, instance)
        return this
    }

    populateMetadata(embeddedCoverArt, durationSeconds) {
        if (embeddedCoverArt) {
            this.EmbeddedCoverArt = `${settings.mediaServer}/${embeddedCoverArt}`
        }
        if (durationSeconds) {
            this.AudioDuration = durationSeconds
        }
    }

    matches(query) {
        return this.SearchTerms.includes(query)
    }
}

module.exports = MusicFile
