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
        }

        this.playMedia = this.playMedia.bind(this)
        this.login = this.login.bind(this)
        this.emptyQueue = this.emptyQueue.bind(this)
    }

    componentDidMount() {
        service.musicQueue.setApi(service.api).then(() => {
            service.musicQueue.serverRead().then(queue => {
                this.setState({
                    queue: queue,
                })
            })
        })
    }

    emptyQueue() {
        service.musicQueue.empty().then(() => {
            this.setState({
                queue: service.musicQueue.getQueue(),
            })
        })
    }

    playMedia(song) {
        this.setState({
            song,
        })
        service.musicQueue.add(song)
        service.musicQueue.serverWrite()
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
                                return <Component {...props} playMedia={this.playMedia} api={service.api} user={this.state.user} musicQueue={service.musicQueue} emptyQueue={this.emptyQueue} />
                            }}
                        />
                    </div>
                    <Comp.AudioControls song={this.state.song} />
                </UIRouter>
            </div>
        )
    }
}
