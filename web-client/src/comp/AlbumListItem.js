import React,{Component} from 'react'

import Comp from './'

export default class AlbumListItem extends Component {
  render(){
    if(!this.props.album){
      return null
    }
    return (
      <div className="list-item" onClick={()=>{this.props.playMedia(this.props.album.Path)}}>
        <Comp.CoverArt imageUrl={this.props.album.CoverArt} />
        <p>
        </p>
          {this.props.album.Album}
        <p>
          {this.props.album.Artist}
        </p>

      </div>
    )
  }
}
