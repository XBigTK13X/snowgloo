import React,{Component} from 'react'
import Comp from '../comp'

export default class SongList extends Component {
  constructor(props){
    super(props)

    this.state = {
      songs:null
    }
  }

  componentDidMount(){
    this.props.api.getSongs()
    .then(result=>{
      this.setState({
        songs:{
          list: result
        }
      })
    })
  }

  render(){
    if(!this.state.songs || !this.state.songs.list){
      return null
    }
    return (
      <div>
      {this.state.songs.list.map((song,songIndex)=>{
        return (
          <Comp.SongListItem key={songIndex} song={song} playMedia={this.props.playMedia} alternate={songIndex%2===0}/>
        )
      })}
      </div>
    )
  }
}
