import React, { Component } from 'react'

export default class Home extends Component {
    render() {
        return <p>This is the home page for {this.props.user}.</p>
    }
}
