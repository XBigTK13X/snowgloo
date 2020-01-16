const settings = require('./settings')
const jsmediatags = require('jsmediatags')
const {getAudioDurationInSeconds} = require('get-audio-duration')
class MusicFile {
  constructor(path){
    const parts = path.split("/")
    //Use ffprobe for this this.Tags = NODEID3.read(path)
    this.Path = path
    this.Album = parts[parts.length-2]
    this.Artist = parts[parts.length-3]
    this.AudioUrl = `${settings.mediaServer}${path}`
    this.Title = parts[parts.length-1]
  }

  readTags(){
    return new Promise(resolve=>{
      return jsmediatags.read(this.Path,{
        onSuccess: (tags) =>{
          console.log(tags)
          this.Tags = tags
          resolve(tags)
        }
      })
    })
  }

  readDuration(){
    return getAudioDurationInSeconds(this.Path)
    .then((duration)=>{
      this.Duration = duration
    })
  }
}

module.exports = MusicFile
