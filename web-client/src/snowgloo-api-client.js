import axios from 'axios'
import settings from './settings'

class ApiClient {
  constructor(){
    this.httpClient = axios.create({
      baseURL: settings.webApiUrl
    })
  }

  getFiles(){
    return this.httpClient.get('files')
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
