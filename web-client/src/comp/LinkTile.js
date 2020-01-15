import React,{Component} from 'react';

import {UISref} from '@uirouter/react'

export default class LinkTile extends Component {
  render(){
    return (
      <UISref to={this.props.to}>
        <button>{this.props.text}</button>
      </UISref>
    )
  }
}
