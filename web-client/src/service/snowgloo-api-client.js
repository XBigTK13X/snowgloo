import axios from 'axios'
import settings from '../settings'

class ApiClient {
    constructor() {
        this.httpClient = axios.create({
            baseURL: settings.webApiUrl,
        })
    }

    get(url) {
        return this.httpClient.get(url).then(response => {
            return response.data
        })
    }

    getSongs() {
        return this.get('song/list')
    }

    getAlbums() {
        return this.get('album/list')
    }

    getArtists() {
        return this.get('artist/list')
    }

    getArtist(artist) {
        return this.httpClient.get('artist/view', { params: { artist } }).then(response => {
            return response.data
        })
    }

    getAlbum(albumSlug) {
        return this.httpClient.get('album/view', { params: { albumSlug } }).then(response => {
            return response.data
        })
    }

    catalogRebuild() {
        return this.httpClient.post('catalog/build').then(response => {
            return response.data
        })
    }

    catalogStatus() {
        return this.get('catalog/build/status')
    }

    userList() {
        return this.get('user/list')
    }
}

let instance

if (!instance) {
    instance = new ApiClient()
}

export default instance
