import React, { Component } from 'react'

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome'
import { faAlignJustify, faMusic } from '@fortawesome/free-solid-svg-icons'
import MicroModal from 'react-micro-modal'

import Comp from './'

export default class SongPickerItem extends Component {
    constructor(props) {
        super(props)
        this.handleClick = this.handleClick.bind(this)
    }
    handleClick(e) {
        console.log(e.target)
        const kind = e.target.getAttribute('data-kind')
        if (kind === 'play') {
            this.props.playMedia(this.props.song)
        }
    }

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
            <tr {...this.props.provided.draggableProps} ref={this.props.innerRef} className={style} onClick={this.handleClick}>
                <td data-kind="action-menu" className="centered">
                    <MicroModal
                        trigger={handleOpen => (
                            <button className="small-icon-button" onClick={handleOpen} title="Open the action menu">
                                <FontAwesomeIcon icon={faMusic} />
                            </button>
                        )}
                        children={handleClose => (
                            <div>
                                <Comp.LinkButton to="album-view" params={{ albumSlug: this.props.song.AlbumSlug }} text="View Album" />
                                <Comp.LinkButton to="artist-view" params={{ artist: this.props.song.Artist }} text="View Artist" />
                                {this.props.removeItem ? (
                                    <button
                                        className="large-button"
                                        onClick={() => {
                                            this.props.removeItem(this.props.songIndex)
                                        }}
                                    >
                                        Remove
                                    </button>
                                ) : null}
                                {this.props.addToQueue ? (
                                    <button
                                        className="large-button"
                                        onClick={() => {
                                            this.props.addToQueue(this.props.song)
                                        }}
                                    >
                                        Queue Up
                                    </button>
                                ) : null}
                                <button className="large-button" onClick={handleClose}>
                                    Cancel
                                </button>
                            </div>
                        )}
                    />
                </td>
                <td data-kind="play">
                    Disc {this.props.song.Disc} Track {this.props.song.Track}
                </td>
                <td data-kind="play">{this.props.song.Title}</td>
                <td data-kind="play">{this.props.song.DisplayAlbum}</td>
                <td data-kind="play">{this.props.song.DisplayArtist}</td>
                <td data-kind="play">
                    <Comp.SongDuration song={this.props.song} />
                </td>
                {this.props.updateSongList ? (
                    <td data-kind="reorder" {...this.props.provided.dragHandleProps} className="centered">
                        <div className="small-icon-button" title="Drag to reorder">
                            <FontAwesomeIcon icon={faAlignJustify} />
                        </div>
                    </td>
                ) : null}
            </tr>
        )
    }
}
