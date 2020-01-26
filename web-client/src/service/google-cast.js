const debounce = require('debounce')
const settings = require('../settings')

class GoogleCast {
    constructor() {
        this.isCasting = false
        this.sessionId = null
        this.castSessionInterval = null
        this.castSession = null
        this.remote = null
        this.player = null
        this.playerController = null
    }

    load(song) {
        return new Promise((resolve, reject) => {
            if (!this.isCasting) {
                return resolve()
            }
            var mediaInfo = new window.chrome.cast.media.MediaInfo(song.AudioUrl)
            var request = new window.chrome.cast.media.LoadRequest(mediaInfo)
            this.castSession.loadMedia(request).then(() => {
                this.setupRemote()
                resolve()
            }, reject)
        })
    }

    onChange(handler) {
        if (this.castSessionInterval) {
            clearInterval(this.castSessionInterval)
        }
        this.castSessionInterval = setInterval(() => {
            if (!window.cast) {
                return
            }
            let session = window.cast.framework.CastContext.getInstance().getCurrentSession()
            if ((!session && this.isCasting) || (session && !this.isCasting)) {
                if (session) {
                    if (!this.isCasting || session.i.sessionId !== this.sessionId) {
                        this.isCasting = true
                        this.sessionId = session.i.sessionId
                        this.castSession = session
                        this.setupRemote()
                        handler({
                            isCasting: true,
                        })
                    }
                } else {
                    this.isCasting = false
                    this.sessionId = null
                    this.castSession = null
                    this.remote = null
                    handler({
                        isCasting: false,
                    })
                }
            }
        }, settings.castPollMilliseconds)
    }

    setupRemote() {
        this.player = new window.cast.framework.RemotePlayer()
        this.playerController = new window.cast.framework.RemotePlayerController(this.player)
        this.playerController.addEventListener(window.cast.framework.RemotePlayerEventType.MEDIA_INFO_CHANGED, () => {
            // Use the current session to get an up to date media status.
            let session = window.cast.framework.CastContext.getInstance().getCurrentSession()

            if (!session) {
                return
            }

            // Contains information about the playing media including currentTime.
            let mediaStatus = session.getMediaSession()
            if (!mediaStatus) {
                return
            }
        })
        this.playerController.addEventListener(window.cast.framework.RemotePlayerEventType.IS_CONNECTED_CHANGED, function() {
            if (!this.player.isConnected) {
                console.log('RemotePlayerController: Player disconnected')
                // Update local player to disconnected state
            }
        })
    }

    playOrPause() {
        if (this.playerController) {
            this.playerController.playOrPause()
        }
    }

    seek = debounce(time => {
        if (this.playerController) {
            this.player.currentTime = time
            this.playerController.seek()
        }
    }, settings.debounceMilliseconds)
}

let instance
if (!instance) {
    instance = new GoogleCast()
}

export default instance
