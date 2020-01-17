import React,{Component} from 'react'

export default class ArtistListItem extends Component {
  render(){
    return (
      <div className="list-item-small">
        {this.props.artist}
      </div>
    )
  }
}
