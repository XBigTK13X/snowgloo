import React, { Component } from 'react'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faFolderPlus } from '@fortawesome/free-solid-svg-icons'

import Comp from './'

export default class SongPicker extends Component {
    render() {
      let queueButton = !this.props.hideQueueButton ? (
        <button className="icon-button" onClick={()=>{this.props.addToQueue(this.props.songs)}} title="Add to queue">
          <FontAwesomeIcon icon={faFolderPlus} />
        </button>
      ) : null
        return (
            <div>
                {queueButton}
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
                            return <Comp.SongPickerItem key={songIndex} song={song} playMedia={this.props.playMedia} alternate={songIndex % 2 === 0} nowPlaying={this.props.showNowPlaying ? songIndex === this.props.playingIndex : null} />
                        })}
                    </tbody>
                </table>
            </div>
        )
    }
}
