import React, { Component } from 'react'

import Comp from './'

export default class SongPickerItem extends Component {
    render() {
        if (!this.props.song) {
            return null
        }
        let style = 'list-item-text'
        if (this.props.alternate && !this.props.nowPlaying) {
            style += ' alternate'
        }
        if (this.props.nowPlaying) {
            style += ' highlighted-row'
        }
        return (
            <tr
                className={style}
                onClick={() => {
                    this.props.playMedia(this.props.song)
                }}
            >
                <td>
                    Disc {this.props.song.Disc} Track {this.props.song.Track}
                </td>
                <td>{this.props.song.Title}</td>
                <td>{this.props.song.DisplayAlbum}</td>
                <td>{this.props.song.DisplayArtist}</td>
                <td>
                    <Comp.SongDuration song={this.props.song} />
                </td>
            </tr>
        )
    }
}
