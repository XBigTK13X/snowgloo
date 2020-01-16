const settings = require('./settings')
const recurse = require('recursive-readdir')
const MusicFile = require('./music-file')

class Catalog {
  constructor(){
    this.mediaRoot = settings.mediaRoot
    this.fileCache = null
  }

  readFiles(){
    return new Promise((resolve,reject)=>{
      if(this.fileCache){
          return resolve(this.fileCache)
      }
      recurse(this.mediaRoot,(err,files)=>{
        if(err){
          return reject(err)
        }
        this.fileCache = files
          .filter(x=>{
            if(x.includes('Anime/') || x.includes('Custom/') || x.includes('Game/')){
              return false
            }
            if(x.includes('.jpg') || x.includes('.png')){
              return false
            }
            return true
          })
          .map(file=>{
            return new MusicFile(file)
          })
          .sort((a,b)=>{
            if(a.Artist !== b.Artist){
              return a.Artist < b.Artist ? 1 : -1
            }
            if(a.Album !== b.Album){
              return a.Album < b.Album ? 1 : -1
            }
            return a.Title < b.Title ? 1 : -1
          })
        Promise.all(this.fileCache.map(x=>{return x.readDuration()}))
        .then(()=>{
          resolve(this.fileCache)
        })
      })
    })
  }

  playMedia(entry){
    return new Promise((resolve=>{
      //let relativePath = entry.replace(settings.mediaRoot,'')
      resolve(`${settings.mediaServer}${entry}`)
    }))
  }
}

let instance;

if(!instance){
  instance = new Catalog()
}

module.exports = instance
