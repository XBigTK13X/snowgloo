import React, { Component } from 'react'

import Comp from './'

export default class SongPicker extends Component {
    render() {
        return (
            <div>
                <table>
                    <thead>
                        <tr>
                            <th>Track</th>
                            <th>Title</th>
                            <th>Album</th>
                            <th>Artist</th>
                            <th>Duration</th>
                        </tr>
                    </thead>
                    <tbody>
                        {this.props.songs.map((song, songIndex) => {
                            return <Comp.SongPickerItem key={songIndex} song={song} playMedia={this.props.playMedia} alternate={songIndex % 2 === 0} />
                        })}
                    </tbody>
                </table>
            </div>
        )
    }
}
