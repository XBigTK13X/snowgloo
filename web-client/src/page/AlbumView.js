import React, { Component } from 'react'

import Comp from '../comp'

export default class AlbumView extends Component {
    constructor(props) {
        super(props)

        this.state = {
            songs: null,
        }
    }

    componentDidMount() {
        this.props.api.getAlbum(this.props.$stateParams.albumSlug).then(result => {
            this.setState({
                album: result.album,
            })
        })
    }

    render() {
        if (!this.state.album) {
            return null
        }
        return (
            <div>
                <Comp.SongPicker songs={this.state.album.Songs} playMedia={this.props.playMedia} />
            </div>
        )
    }
}
