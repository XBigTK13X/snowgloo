import axios from 'axios'
import settings from '../settings'

class ApiClient {
  constructor(){
    this.httpClient = axios.create({
      baseURL: settings.webApiUrl
    })
  }

  getSongs(){
    return this.httpClient.get('song/list')
    .then((response)=>{
      return response.data
    })
  }

  getAlbums(){
    return this.httpClient.get('album/list')
    .then((response)=>{
      return response.data
    })
  }

  getArtists(){
    return this.httpClient.get('artist/list')
    .then((response)=>{
      return response.data
    })
  }

  playMedia(path){
    return this.httpClient.post('media/play',{path})
    .then((response)=>{
      return response.data
    })
  }
}

let instance;

if(!instance){
  instance = new ApiClient()
}

export default instance
