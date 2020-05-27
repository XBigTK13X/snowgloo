import React, { Component } from 'react'

import Comp from './'

const actionLinks = [
    {
        to: 'queue',
        text: 'Queue',
    },
    {
        to: 'playlist-list',
        text: 'Playlists',
    },
    {
        to: 'search',
        text: 'Search',
    },
    {
        to: 'random-list',
        text: 'Random'
    },
    {
        to: 'album-list',
        text: 'Albums',
    },
    {
        text: 'Admin',
        to: 'admin',
    },
    {
        text: 'Logout',
        action: 'logout',
    },
]

const categoryLinks = [
    {
        to: 'artist-list',
        params: {
            category: "Anime"
        },
        text: 'Anime',
    },
    {
        to: 'artist-list',
        params: {
            category: "Artist"
        },
        text: 'Artist',
    },
    {
        to: 'artist-view',
        params: {
            artist: "Compilation"
        },
        text: 'Compilation',
    },
    {
        to: 'artist-view',
        params: {
            artist: "Disney"
        },
        text: 'Disney',
    },
    {
        to: 'artist-view',
        params: {
            artist: "Movie"
        },
        text: 'Movie',
    },
    {
        to: 'artist-list',
        params: {
            category: "Game"
        },
        text: 'Game',
    },
]

export default class NavBar extends Component {
    render() {
        return (
            <div>
            {actionLinks.map((link, linkIndex) => {
                if (link.action) {
                    return <Comp.LinkTile key={linkIndex} text={link.text} action={this.props.logout} />
                }
                return <Comp.LinkTile key={linkIndex} to={link.to} text={link.text} params={link.params}/>
            })}
            <br/>
            {categoryLinks.map((link, linkIndex) => {
                if (link.action) {
                    return <Comp.LinkTile key={linkIndex} text={link.text} action={this.props.logout} />
                }
                return <Comp.LinkTile key={linkIndex} to={link.to} text={link.text} params={link.params}/>
            })}
            </div>
        )
    }
}
