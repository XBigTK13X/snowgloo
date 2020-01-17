import React, { Component } from 'react'

import Comp from './'

export default class AlbumListItem extends Component {
    render() {
        if (!this.props.album) {
            return null
        }
        return (
            <Comp.Href to="album-view" params={{ albumSlug: this.props.album.AlbumSlug }}>
                <a href="/">
                    <div className="list-item">
                        <Comp.CoverArt imageUrl={this.props.album.CoverArt} />
                        <p></p>
                        {this.props.album.Album} ({this.props.album.ReleaseYear})<p>{this.props.album.Artist}</p>
                    </div>
                </a>
            </Comp.Href>
        )
    }
}
