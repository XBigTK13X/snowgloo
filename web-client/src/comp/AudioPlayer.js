// Modified from https://github.com/binodswain/react-howler-player

import React, { Component } from 'react'
import PropTypes from 'prop-types'
import { Howl } from 'howler'
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faPlay, faPause, faVolumeUp, faVolumeMute } from '@fortawesome/free-solid-svg-icons'

import Comp from './'
import service from '../service'

const STATE = {
    PREPARE: 'PREPARE',
    READY: 'READY',
    ENDED: 'ENDED',
    PAUSE: 'PAUSE',
    PLAYING: 'PLAYING',
}

const STEP_MILLISECONDS = 400 //Originally 15

class Prepare extends Component {
    static propTypes = {
        loadingText: PropTypes.string,
        isDark: PropTypes.bool,
    }

    render() {
        return null
    }
}

export default class ReactHowlerPlayer extends Component {
    constructor(props) {
        super(props)
        this.state = {
            sound: null,
            playerState: STATE.PREPARE,
            src: [],
            progressValue: 0,
            currentPos: '0:00',
            // TODO Save this in localStorage
            volume: 100,
            isMute: false,
        }
        this.stepInterval = null
    }

    componentDidMount() {
        this.setupPlayer()
    }

    componentWillUnmount() {
        this.destroySound()
    }

    componentDidUpdate(prevProps, prevState) {
        if (prevProps.src !== this.props.src) {
            this.setupPlayer()
        }
    }

    toggleMute = () => {
        this.setState(prevState => {
            const { volume, sound } = prevState

            if (volume === 0 || !prevState.isMute) {
                sound.mute(true)
                return { isMute: true }
            }
            sound.mute(false)
            return { isMute: !prevState.isMute }
        })
    }

    readyToPlay = () => {
        const { playerState, sound } = this.state
        if (playerState === STATE.PLAYING) {
            return
        }
        this.setState({
            playerState: STATE.READY,
            duration: this.formatTime(Math.round(sound.duration())),
        })
    }

    setupPlayer = () => {
        this.destroySound()
        const { src, format = ['wav', 'mp3', 'flac', 'aac', 'm4a'] } = this.props

        if (!src) {
            return
        }
        let sound = new Howl({
            src,
            format,
            autoplay: true,
            html5: true,
        })

        sound.volume(this.props.isCasting ? 0 : this.state.volume)

        sound.once('load', this.readyToPlay)

        sound.on('end', () => {
            this.playbackEnded()
            this.props.songFinished()
        })

        sound.on('play', () => {
            this.stepInterval = setInterval(this.step, STEP_MILLISECONDS)
        })

        this.setState({
            sound,
            playerState: STATE.PREPARE,
            progressValue: 0,
            currentPos: '0:00',
            src,
        })
    }

    playbackEnded = () => {
        const { onTimeUpdate } = this.props
        const { duration } = this.state
        if (onTimeUpdate) {
            let playerState = {
                currentTime: this.state.sound.duration(),
                progressPercent: 100,
            }
            onTimeUpdate(playerState)
        }
        clearInterval(this.stepInterval)
        this.setState({
            playerState: STATE.ENDED,
            progressValue: 100,
            currentPos: duration,
        })
    }

    playbackPlay = () => {
        const { sound } = this.state
        sound.play()
        service.googleCast.playOrPause()
        this.setState({
            playerState: STATE.PLAYING,
        })
    }

    playbackPause = () => {
        const { sound } = this.state
        sound.pause()
        clearInterval(this.stepInterval)
        service.googleCast.playOrPause()
        this.setState({
            playerState: STATE.PAUSE,
        })
    }

    formatTime = secs => {
        var minutes = Math.floor(secs / 60) || 0
        var seconds = secs - minutes * 60 || 0

        return minutes + ':' + (seconds < 10 ? '0' : '') + seconds
    }

    seek = value => {
        //Prevent scrubbing to end, triggering next song start
        if (value === 100) {
            value = this.state.progressValue
        }
        const { sound } = this.state
        let percent = value / 100
        let timeLocation = sound.duration() * percent
        sound.seek(timeLocation)
        service.googleCast.seek(timeLocation)
        let currentSeek = sound.seek() || 0
        this.setState({
            progressValue: value,
            currentPos: this.formatTime(Math.round(currentSeek)),
        })
    }

    step = () => {
        let { sound } = this.state
        // If the sound is still playing, continue stepping. Unless a user is seeking.
        if (sound.playing() && !window.isMouseDown) {
            const { onTimeUpdate } = this.props

            var seek = sound.seek() || 0

            let percentage = (seek / sound.duration()) * 100 || 0
            this.setState({
                progressValue: percentage.toFixed(3),
                currentPos: this.formatTime(Math.round(seek)),
                playerState: STATE.PLAYING,
            })
            if (onTimeUpdate) {
                let playerState = {
                    currentTime: seek,
                    progressPercent: Number(percentage.toFixed(3)),
                }
                onTimeUpdate(playerState)
            }
        }
    }

    changeVolume = volume => {
        this.state.sound.volume(Math.round(volume) / 100)

        this.setState({
            volume,
            isMute: Number(volume) === 0,
        })
    }

    destroySound = () => {
        const { sound } = this.state
        clearInterval(this.stepInterval)
        if (sound) {
            sound.off()
            sound.stop()
        }
    }

    render() {
        const { playerState, duration, currentPos, isMute } = this.state

        const { loadingText } = this.props

        if (playerState === STATE.PREPARE) {
            return <Prepare loadingText={loadingText} />
        }

        let playPauseAction
        let playPauseIcon

        if (playerState === STATE.READY || playerState === STATE.PAUSE || playerState === STATE.ENDED) {
            playPauseAction = this.playbackPlay
            playPauseIcon = <FontAwesomeIcon icon={faPlay} />
        } else if (playerState === STATE.PLAYING) {
            playPauseAction = this.playbackPause
            playPauseIcon = <FontAwesomeIcon icon={faPause} />
        }

        let volumeIcon = isMute ? <FontAwesomeIcon icon={faVolumeMute} /> : <FontAwesomeIcon icon={faVolumeUp} />

        return (
            <div>
                <div className="seek-range">
                    <Comp.RangeInput value={this.state.progressValue} onChange={this.seek} />
                </div>
                <div className="audio-duration">
                    {currentPos} <span className="duration">/ {duration || '...'}</span>
                </div>
                <button className="audio-button no-focus" onClick={playPauseAction}>
                    {playPauseIcon}
                </button>
                <div className="volume-range">
                    <Comp.RangeInput value={isMute ? 0 : this.state.volume} onChange={this.changeVolume} />
                </div>
                <button className="audio-button no-focus" onClick={this.toggleMute}>
                    {volumeIcon}
                </button>
            </div>
        )
    }
}
