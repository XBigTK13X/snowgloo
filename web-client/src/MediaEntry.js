import React,{Component} from 'react'

export default class MediaEntry extends Component {
  render(){
    if(!this.props.path){
      return null
    }
    return (
      <p onClick={()=>{this.props.playMedia(this.props.path)}}>
        {this.props.path}
      </p>
    )
  }
}
