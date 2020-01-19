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

        this.state = {
            audioUrl: null,
            user: service.user.getUser(),
        }

        this.playMedia = this.playMedia.bind(this)
        this.login = this.login.bind(this)
    }

    playMedia(song) {
        this.setState({
            song,
        })
    }

    login(user) {
        service.user.login(user)
        this.setState({
            user,
        })
    }

    logout() {
        service.user.logout()
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
                                return <Component {...props} playMedia={this.playMedia} api={service.api} user={this.state.user} />
                            }}
                        />
                    </div>
                    <Comp.AudioControls song={this.state.song} />
                </UIRouter>
            </div>
        )
    }
}
