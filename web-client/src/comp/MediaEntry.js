import React,{Component} from 'react'

export default class MediaEntry extends Component {
  render(){
    return (
      <p onClick={()=>{this.props.playMedia(this.props.details.Path)}}>
        song: {this.props.details.Title} | album: {this.props.details.Album} | artist: {this.props.details.Artist}
      </p>
    )
  }
}
