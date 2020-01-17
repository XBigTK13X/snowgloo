import React, { Component } from 'react'

import Comp from './'

export default class LinkTile extends Component {
    render() {
        return (
            <Comp.Href to={this.props.to}>
                <button>{this.props.text}</button>
            </Comp.Href>
        )
    }
}
