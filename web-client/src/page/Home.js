import React, { Component } from 'react'

import Comp from '../comp'

export default class Home extends Component {
    render() {
        if (!this.props.queuedSongs || !this.props.queuedSongs.length) {
            return <p>{this.props.user} has no music queued up.</p>
        }
        return (
            <div>
                <button onClick={this.props.emptyQueue}>Empty Queue</button>
                <Comp.SongPicker songs={this.props.queuedSongs} playMedia={this.props.playMedia} playingIndex={this.props.playingIndex} showNowPlaying={true} />
            </div>
        )
    }
}
