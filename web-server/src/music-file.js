const settings = require('./settings')
const inspect = require('./inspect')

class MusicFile {
  constructor(path){
    const parts = path.split("/")
    this.Path = `${settings.mediaServer}${path}`
    this.Album = parts[parts.length-2]
    this.Artist = parts[parts.length-3]
    this.AudioUrl = `${settings.mediaServer}${path}`
    let trackAndTitle = parts[parts.length-1]
    if(!trackAndTitle.includes(' - ')){
      this.Title = trackAndTitle
    }
    else {
      let titleParts = trackAndTitle.split(' - ')
      if(titleParts[0].includes('D')){
        let discAndTrackParts  = titleParts[0].split('D')[1].split('T')
        this.Disc = parseInt(discAndTrackParts[0],10)
        this.Track = parseInt(discAndTrackParts[1],10)
        this.Title = titleParts[1]
      }
      else {
        this.Disc = 1
        this.Track = parseInt(titleParts[0],10)
        this.Title = titleParts[1]
      }
    }
    this.Title = this.Title.split('.').slice(0, -1).join('.')
    this.AlbumSlug = `${this.Album}-${this.Artist}`
  }

  readInfo(){
    return inspect.audio(this.Path)
    .then(data=>{
      this.Info = data
    })
  }
}

module.exports = MusicFile
