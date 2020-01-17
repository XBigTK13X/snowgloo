import React,{Component} from 'react'
import Comp from '../comp'

export default class ArtistList extends Component {
  constructor(props){
    super(props)

    this.state = {
      albums:{}
    }
  }

  componentDidMount(){
    this.props.api.getAlbums()
    .then(result=>{
      this.setState({
        albums: result
      })
    })
  }

  render(){
    if(!this.state.albums.list){
      return null
    }
    return (
      <div className="list-grid">
      {this.state.albums.list.map((album,albumIndex)=>{
        return (
          <Comp.AlbumListItem key={albumIndex} album={this.state.albums.lookup[album]} playMedia={this.props.playMedia}/>
        )
      })}
      </div>
    )
  }
}
