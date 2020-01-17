const recurse = require('recursive-readdir')
const _ = require('lodash')
const path = require('path')

const settings = require('./settings')
const database = require('./database')

const MusicFile = require('./music-file')

class Catalog {
  constructor(){
    this.mediaRoot = settings.mediaRoot
    this.workingSet = {}
    this.database = database
  }

  readFiles(){
    return new Promise((resolve,reject)=>{
      this.database.read()
      .then(workingSet=>{
        if(!this.database.isEmpty()){
          console.log(`Using ${workingSet.files.length} cached database results`)
          this.workingSet = workingSet
          return resolve(workingSet.files)
        }
        console.log("Rebuilding cache from files")
        let coverArts = []
        let albumCoverArts = {

        }
        recurse(this.mediaRoot,(err,files)=>{
          if(err){
            return reject(err)
          }
          files = files
            .filter(x=>{
              if(x.includes('Anime/') || x.includes('Custom/') || x.includes('Game/')){
                return false
              }
              if(x.includes('.jpg') || x.includes('.png') || x.includes('.jpeg')){
                coverArts.push(x)
                return false
              }
              return true
            })
            .map(file=>{
              return new MusicFile(file)
            })
            .sort((a,b)=>{
              if(a.Artist !== b.Artist){
                return a.Artist > b.Artist ? 1 : -1
              }
              if(a.Album !== b.Album){
                return a.Album > b.Album ? 1 : -1
              }
              return a.Title > b.Title ? 1 : -1
            })
          Promise.all(files.map(x=>{return x.readInfo()}))
          .then(()=>{
            return new Promise(resolve=>{
              coverArts.forEach(coverArt=>{
                let artDir = path.dirname(coverArt)
                files.forEach(file=>{
                  if(_.has(albumCoverArts,file.AlbumSlug)){
                    file.CoverArt = albumCoverArts[file.AlbumSlug]
                  }
                  if(file.Path.includes(artDir)){
                    file.CoverArt = `${settings.mediaServer}${coverArt}`
                    albumCoverArts[file.AlbumSlug] = `${settings.mediaServer}${coverArt}`
                  }
                })
                resolve()
              })
            })
          })
          .then(()=>{
            this.workingSet = {
              files,
              albumCoverArts
            }
            return this.database.write(this.workingSet)
          })
          .then(()=>{
            resolve(this.workingSet)
          })
        })
      })
    })
  }

  getSongs(){
    return new Promise(resolve=>{
      resolve(this.workingSet.files)
    })
  }

  getArtists(){

  }

  getAlbums(){
    return new Promise(resolve=>{
      if(this.workingSet.albums){
          return resolve(this.workingSet.albums)
      }
      let albums = {
        list: [],
        lookup: {}
      }
      this.workingSet.files.forEach(file=>{
        if(!_.has(albums.lookup,file.AlbumSlug)){
          albums.lookup[file.AlbumSlug] = {
            Album: file.Album,
            Artist: file.Artist,
            Songs: [],
            AlbumSlug: file.AlbumSlug,
            CoverArt: this.workingSet.albumCoverArts[file.AlbumSlug]
          }
          albums.list.push(file.AlbumSlug)
        }
        albums.lookup[file.AlbumSlug].Songs.push(file)
      })
      albums.list = albums.list.sort((a,b)=>{
        return a.AlbumSlug > b.AlbumSlug? 1: - 1
      })
      this.workingSet.albums = albums
      this.database.write(this.workingSet)
      resolve(albums)
    })
  }
}

let instance;

if(!instance){
  instance = new Catalog()
}

module.exports = instance
