import React, { Component } from 'react'

import Comp from '../comp'

export default class ArtistView extends Component {
    constructor(props) {
        super(props)
        this.state = {
            albums: null,
        }
    }

    componentDidMount() {
        this.props.api.getArtist(this.props.$stateParams.artist).then(result => {
            this.setState({
                albums: result.albums,
            })
        })
    }

    render() {
        if (!this.state.albums) {
            return null
        }

        return (
            <div>
                <Comp.AlbumPicker albums={this.state.albums} />
            </div>
        )
    }
}
