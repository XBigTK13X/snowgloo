import React, { Component } from 'react'

import Comp from '../comp'

export default class Home extends Component {
    constructor(props) {
        super(props)
        this.state = {
            queue: this.props.musicQueue.getQueue(),
        }
    }
    componentDidUpdate() {
        if (this.state.queue !== this.props.musicQueue.getQueue()) {
            this.setState({
                queue: this.props.musicQueue.getQueue(),
            })
        }
    }
    render() {
        if (!this.state.queue || !this.state.queue.songs || !this.state.queue.songs.length) {
            return <p>{this.props.user} has no music queued up.</p>
        }
        return (
            <div>
                <button onClick={this.props.emptyQueue}>Empty Queue</button>
                <Comp.SongPicker songs={this.state.queue.songs} playMedia={this.props.playMedia} />
            </div>
        )
    }
}
