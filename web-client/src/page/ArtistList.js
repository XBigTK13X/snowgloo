import React,{Component} from 'react'
import Comp from '../comp'

export default class AlbumList extends Component {
  constructor(props){
    super(props)

    this.state = {
      artists:null
    }
  }

  componentDidMount(){
    this.props.api.getArtists()
    .then(result=>{
      this.setState({
        artists: result.list
      })
    })
  }

  render(){
    if(!this.state.artists){
      return null
    }
    return (
      <div className="list-grid">
      {this.state.artists.map((artist,artistIndex)=>{
        return (
          <Comp.ArtistListItem key={artistIndex} artist={artist} playMedia={this.props.playMedia}/>
        )
      })}
      </div>
    )
  }
}
