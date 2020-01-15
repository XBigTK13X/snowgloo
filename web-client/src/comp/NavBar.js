import React,{Component} from 'react'

import Comp from './'

const links = [
  {
    to: 'home',
    text: 'Home'
  },
  {
    to: 'song-list',
    text: 'Songs'
  }
]

export default class NavBar extends Component {
  render(){
      return links.map((link, linkIndex)=>{
        return <Comp.LinkTile key={linkIndex} to={link.to} text={link.text} />
      })
  }
}
