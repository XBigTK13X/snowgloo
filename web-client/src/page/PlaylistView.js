import React, { Component } from 'react'

import Comp from '../comp'

export default class PlaylistView extends Component {
    constructor(props) {
        super(props)

        this.state = {
            playlist: null,
        }
    }

    componentDidMount() {
        this.props.api.getPlaylist(this.props.$stateParams.playlistId).then(result => {
            this.setState({
                playlist: result,
            })
        })
    }

    render() {
        if (!this.state.playlist) {
            return null
        }
        return (
            <div>
                <Comp.SongPicker
                    title={this.state.playlist.name}
                    addToQueue={this.props.addToQueue}
                    songs={this.state.playlist.songs}
                    playMedia={this.props.playMedia} />
            </div>
        )
    }
}
