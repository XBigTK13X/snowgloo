const settings = require('./settings')
const recurse = require('recursive-readdir')

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
          .filter(x=>{return !(x.includes('.jpg') || x.includes('.png'))})
          .map(file=>{
            const parts = file.split("/")
            return {
              Album: parts[parts.length-2],
              Artist: parts[parts.length-3],
              AudioUrl:`${settings.mediaServer}${file}`,
              Duration:1000,
              Path: file,
              Title: parts[parts.length-1]
            }
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
        resolve(this.fileCache)
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
