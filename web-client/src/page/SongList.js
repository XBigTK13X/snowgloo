import React, { Component } from 'react'
import Comp from '../comp'

export default class SongList extends Component {
    constructor(props) {
        super(props)

        this.state = {
            songs: null,
        }
    }

    componentDidMount() {
        this.props.api.getSongs().then(result => {
            this.setState({
                songs: {
                    list: result,
                },
            })
        })
    }

    render() {
        if (!this.state.songs || !this.state.songs.list) {
            return null
        }
        return <Comp.SongPicker songs={this.state.songs.list} playMedia={this.props.playMedia} />
    }
}
