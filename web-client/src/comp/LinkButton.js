import React, { Component } from 'react'

import Comp from './'

export default class LinkButton extends Component {
    render() {
        return (
            <Comp.Href to={this.props.to} params={this.props.params}>
                <a href="/">
                    <button className="large-button">{this.props.text}</button>
                </a>
            </Comp.Href>
        )
    }
}
