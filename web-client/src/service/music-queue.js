import _ from 'lodash'

class MusicQueue {
    constructor(queue) {
        this.queue = {
            songs: [],
            currentIndex: null,
        }
    }

    empty() {
        this.queue = {
            songs: [],
            currentIndex: null,
        }
        return this.serverWrite()
    }

    setApi(api) {
        return new Promise(resolve => {
            this.api = api
            resolve()
        })
    }

    add(song) {
        if (!song) {
            return Promise.resolve()
        }
        return new Promise(resolve => {
            let found = false
            this.queue.songs.forEach((entry, entryIndex) => {
                if (entry === song) {
                    this.queue.currentIndex = entryIndex
                    found = true
                }
            })
            if (!found) {
                this.queue.songs.push(song)
            }
            resolve()
        })
    }

    getNext() {
        if (!this.queue.currentIndex) {
            this.queue.currentIndex = 0
        }
        this.queue.currentIndex++
        return this.getCurrent()
    }

    getCurrent() {
        if (this.queue.currentIndex === null) {
            return null
        }
        if (this.queue.currentIndex >= this.queue.songs.length) {
            this.currentIndex = null
            return null
        }
        return this.queue.songs[this.queue.currentIndex]
    }

    setCurrent(index) {
        this.queue.currentIndex = index
    }

    serverRead() {
        if (!this.api) {
            throw new Error('Unable to read music queue, no api has been set')
        }
        return this.api.getQueue().then(result => {
            if (!_.isEmpty(result)) {
                this.queue = {
                  songs: result.songs,
                  currentIndex: result.currentIndex
                }
            }
            return this.queue
        })
    }

    serverWrite() {
        if (!this.api) {
            throw new Error('Unable to persist music queue, no api has been set')
        }
        return this.api.setQueue(this.queue).then(result => {
            return this.queue
        })
    }

    getQueue() {
        return this.queue
    }
}

let instance
if (!instance) {
    instance = new MusicQueue()
}

export default instance
