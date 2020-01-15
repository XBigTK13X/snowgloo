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
        resolve(files.map(file=>{
          return {
            path: file
          }
        }))
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
