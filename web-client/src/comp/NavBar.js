import React, { Component } from 'react'

import Comp from './'

const links = [
    {
        to: 'queue',
        text: 'Queue',
    },
    {
        to: 'artist-list',
        text: 'Artists',
    },
    {
        to: 'album-list',
        text: 'Albums',
    },
    {
        to: 'search',
        text: 'Search',
    },
    {
        text: 'Logout',
        action: 'logout',
    },
]

export default class NavBar extends Component {
    render() {
        return links.map((link, linkIndex) => {
            if (link.action) {
                return <Comp.LinkTile key={linkIndex} text={link.text} action={this.props.logout} />
            }
            return <Comp.LinkTile key={linkIndex} to={link.to} text={link.text} />
        })
    }
}
