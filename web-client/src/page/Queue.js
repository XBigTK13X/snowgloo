import React, { Component } from 'react'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faRandom, faTrashAlt } from '@fortawesome/free-solid-svg-icons'

import Comp from '../comp'

export default class Home extends Component {
    render() {
        if (!this.props.queuedSongs || !this.props.queuedSongs.length) {
            return <p>{this.props.user} has no music queued up.</p>
        }
        return (
            <div>
                <button className="icon-button" onClick={this.props.shuffleQueue} title="Shuffle the queue">
                  <FontAwesomeIcon icon={faRandom} />
                </button>
                <button className="icon-button" onClick={this.props.emptyQueue} title="Empty the queue">
                  <FontAwesomeIcon icon={faTrashAlt} />
                </button>
                <Comp.SongPicker
                  hideQueueButton
                  songs={this.props.queuedSongs}
                  playMedia={this.props.playMedia}
                  playingIndex={this.props.playingIndex}
                  showNowPlaying={true}
                  />
            </div>
        )
    }
}
