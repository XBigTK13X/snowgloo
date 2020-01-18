import React, { Component } from 'react'

import Comp from './'

export default class LinkTile extends Component {
    render() {
        return (
            <Comp.Href to={this.props.to}>
              <a href="/">
                <div className="nav-button">
                  {this.props.text}
                </div>
              </a>
            </Comp.Href>
        )
    }
}
