import React,{Component} from 'react'

export default class SongListItem extends Component {
  render(){
    return (
      <p onClick={()=>{this.props.playMedia(this.props.song)}}>
        {this.props.song.Artist} - {this.props.song.Album} - {this.props.song.Title}
      </p>
    )
  }
}
