import React, { Component } from 'react'

import Comp from './'

export default class AudioControls extends Component {
    constructor(props) {
        super(props)
        this.state = {
            coverArt: null,
            coverSongId: null,
        }
        this.refreshCoverArt = this.refreshCoverArt.bind(this)
    }
    componentDidMount() {
        this.refreshCoverArt()
    }
    componentDidUpdate() {
        this.refreshCoverArt()
    }
    refreshCoverArt() {
        if (this.props.song && this.state.coverSongId !== this.props.song.Id) {
            this.props.api
                .getEmbeddedArt(this.props.song.LocalFilePath)
                .then(result => {
                    this.setState({
                        coverArt: result.coverArtUri,
                        coverSongId: this.props.song.Id,
                    })
                })
                .catch(() => {
                    this.setState({
                        coverArt: this.props.song.CoverArt,
                        coverSongId: this.props.song.Id,
                    })
                })
        }
    }
    render() {
        if (!this.props.song) {
            return null
        }
        return (
            <div className="sticky-footer centered">
                <Comp.AudioPlayer src={[this.props.song.AudioUrl]} songFinished={this.props.songFinished} isCasting={this.props.isCasting} />
                <br />
                <div className="neighbor">
                    <Comp.CoverArt size="small" imageUrl={this.state.coverArt || this.props.song.CoverArt} />
                </div>
                <div className="neighbor">
                    <p className="short-text" title={this.props.song.Title}>
                        {this.props.song.Title}
                    </p>
                    <p className="short-text" title={this.props.song.Album}>
                        {this.props.song.DisplayAlbum}
                    </p>
                    <p className="short-text" title={this.props.song.Artist}>
                        {this.props.song.DisplayArtist}
                    </p>
                </div>
            </div>
        )
    }
}
