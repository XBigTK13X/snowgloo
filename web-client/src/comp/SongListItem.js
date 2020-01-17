import React,{Component} from 'react'

export default class SongListItem extends Component {
  render(){
    return (
      <div className={this.props.alternate?'list-item-text alternate':'list-item-text'} onClick={()=>{this.props.playMedia(this.props.song)}}>
        {this.props.song.Artist} - {this.props.song.Album} - {this.props.song.Title}
      </div>
    )
  }
}
