const settings = require('./settings')
const recurse = require('recursive-readdir')

class Catalog {
  constructor(){
    this.mediaRoot = settings.mediaRoot
  }

  readFiles(){
    return new Promise((resolve,reject)=>{
      recurse(this.mediaRoot,(err,files)=>{
        if(err){
          return reject(err)
        }
        resolve(files
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
            if(a.Album === b.Album){
              return a.Title > b.Title?1:-1
            }
            return a.Album > b.Album ? 1:-1
          })
        )
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
