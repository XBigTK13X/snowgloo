import React, { Component } from 'react'

export default class CoverArt extends Component {
    render() {
        const size = this.props.size === 'small' ? 'cover-art-small' : 'cover-art-medium'
        return <img className={size} src={this.props.imageUrl} alt="Cover art" />
    }
}
