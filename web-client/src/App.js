import React, { Component } from 'react'
import { UIRouter, UIView, pushStateLocationPlugin } from '@uirouter/react'
import routes from './routes'
import Comp from './comp'
import service from './service'

const plugins = [pushStateLocationPlugin]

const configRouter = router => {
    router.urlRouter.otherwise('/')
}

export default class App extends Component {
    constructor(props) {
        super(props)

        let user = service.user.getUser()

        if (user) {
            service.api.setUser(user)
        }

        this.state = {
            audioUrl: null,
            user: user,
            queue: {
                songs: null,
                currentIndex: null,
            },
            castSession: null,
        }

        this.playMedia = this.playMedia.bind(this)
        this.login = this.login.bind(this)
        this.emptyQueue = this.emptyQueue.bind(this)
        this.songFinished = this.songFinished.bind(this)
        this.listenForGoogleCast()
    }

    listenForGoogleCast() {
        let castSessionInterval = setInterval(() => {
            let session = window.cast.framework.CastContext.getInstance().getCurrentSession()
            if (session) {
                this.setState({
                    castSession: session,
                })
            }
            clearInterval(castSessionInterval)
        }, 100)
    }

    componentDidMount() {
        service.musicQueue.setApi(service.api).then(() => {
            service.musicQueue.serverRead().then(queue => {
                queue.currentIndex = null
                this.setState({
                    queue: queue,
                })
            })
        })
    }

    login(user) {
        service.user.login(user)
        service.api.setUser(user)
        service.musicQueue.serverRead().then(queue => {
            this.setState({
                user,
                queue,
            })
        })
    }

    logout() {
        service.user.logout()
        service.api.setUser(null)
        this.setState({
            user: null,
        })
    }

    playMedia(song) {
        service.musicQueue.add(song)
        service.musicQueue.serverWrite()
        var mediaInfo = new window.chrome.cast.media.MediaInfo(song.AudioUrl)
        var request = new window.chrome.cast.media.LoadRequest(mediaInfo)
        this.state.castSession.loadMedia(request).then(
            function() {
                console.log('Load succeed')
            },
            function(errorCode) {
                console.log('Error code: ' + errorCode)
            }
        )
        this.setState({
            song,
            queue: service.musicQueue.getQueue(),
        })
    }

    songFinished() {
        let nextSong = service.musicQueue.getNext()
        this.playMedia(nextSong)
    }

    emptyQueue() {
        service.musicQueue.empty().then(() => {
            this.setState({
                queue: service.musicQueue.getQueue(),
            })
        })
    }

    render() {
        if (!this.state.user) {
            return (
                <div>
                    <Comp.LoginForm login={this.login} />
                </div>
            )
        }
        return (
            <div>
                <UIRouter plugins={plugins} states={routes} config={configRouter}>
                    <div className="page-wrapper">
                        <Comp.NavBar logout={this.logout} />
                        <UIView
                            render={(Component, props) => {
                                return <Component {...props} playMedia={this.playMedia} api={service.api} user={this.state.user} queuedSongs={this.state.queue.songs} emptyQueue={this.emptyQueue} playingIndex={this.state.queue.currentIndex} />
                            }}
                        />
                    </div>
                    <Comp.AudioControls song={this.state.song} songFinished={this.songFinished} />
                </UIRouter>
            </div>
        )
    }
}
