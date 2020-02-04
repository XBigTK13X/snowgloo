const util = require('./util')

class MusicArtist {
    constructor(musicFile){
        this.Artist = musicFile.Artist
        this.DisplayArtist = musicFile.DisplayArtist
        this.SearchArtist = util.searchify(this.Artist + this.DisplayArtist)
    }

    matches(query){
        return this.SearchArtist.includes(query)
    }
}

module.exports = MusicArtist
