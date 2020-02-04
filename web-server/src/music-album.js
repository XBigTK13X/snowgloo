const util = require('./util')

class MusicAlbum {
    constructor(musicFile, coverArtUrl) {
        this.Album = musicFile.Album
        this.DisplayAlbum = musicFile.DisplayAlbum
        this.DisplayArtist = musicFile.DisplayArtist
        this.SearchAlbum = util.searchify(this.Album + this.DisplayAlbum)
        this.AlbumSlug = musicFile.AlbumSlug
        this.Artist = musicFile.Artist
        this.CoverArt = coverArtUrl
        this.Kind = musicFile.Kind
        this.ReleaseYear = musicFile.ReleaseYear
        this.ReleaseYearSort = musicFile.ReleaseYearSort
        this.Songs = musicFile.Songs || []
        this.SubKind = musicFile.SubKind
    }

    matches(query) {
        return this.SearchAlbum.includes(query)
    }
}

module.exports = MusicAlbum
