import React, { Component } from 'react'
import Comp from '../comp'

export default class ArtistList extends Component {
    constructor(props) {
        super(props)

        this.state = {
            albums: {},
        }
    }

    componentDidMount() {
        this.props.api.getAlbums().then(result => {
            this.setState({
                albums: result,
            })
        })
    }

    render() {
        if (!this.state.albums.list) {
            return null
        }
        return <Comp.AlbumPicker albums={this.state.albums} />
    }
}
