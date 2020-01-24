import { Component } from 'react'

import util from '../util'

export default class SongDuration extends Component {
    render() {
        if (!this.props.song || !this.props.song.Info || !this.props.song.Info.format) {
            return null
        }
        return `${util.secondsToTimeStamp(this.props.song.Info.format.duration)}`
    }
}
