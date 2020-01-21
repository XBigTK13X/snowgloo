import React, { Component } from 'react'

import Comp from '../comp'

export default class ArtistView extends Component {
    constructor(props) {
        super(props)
        this.state = {
            albums: null,
        }
    }

    componentDidMount() {
        this.props.api.getArtist(this.props.$stateParams.artist).then(result => {
            this.setState({
                albums: result.albums,
            })
        })
    }

    render() {
        if (!this.state.albums) {
            return null
        }

        return (
            <div>
                {this.state.albums.listKinds.map((listKind, listKindIndex) => {
                    if (!this.state.albums.lists[listKind].length) {
                        return null
                    }
                    return (
                        <div key={listKindIndex}>
                            <h3>{listKind}</h3>
                            <Comp.AlbumPicker
                                albums={{
                                    list: this.state.albums.lists[listKind],
                                    lookup: this.state.albums.lookup,
                                }}
                            />
                        </div>
                    )
                })}
            </div>
        )
    }
}
