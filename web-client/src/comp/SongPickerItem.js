import React, { Component } from 'react'

import Comp from './'

export default class SongPickerItem extends Component {
    render() {
        return (
            <tr
                className={this.props.alternate ? 'list-item-text alternate' : 'list-item-text'}
                onClick={() => {
                    this.props.playMedia(this.props.song)
                }}
            >
                <td>
                    Disc {this.props.song.Disc} Track {this.props.song.Track}
                </td>
                <td>{this.props.song.Title}</td>
                <td>{this.props.song.Album}</td>
                <td>{this.props.song.Artist}</td>
                <td>
                    <Comp.SongDuration song={this.props.song} />
                </td>
            </tr>
        )
    }
}
