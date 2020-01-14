import React,{Component} from 'react'

import audioPlayer from './audio-player'

export default class AudioControls extends Component {
  constructor(props){
    super(props)
    this.state = {
      player: audioPlayer
    }
    this.audio = React.createRef()
  }

  render(){
    if(!this.props.audioUrl){
      return null
    }
    if(this.audio.current){
      this.audio.current.setAttribute('src',this.props.audioUrl)
      this.audio.current.load()
      this.audio.current.play()
    }
    return (
      <audio ref={this.audio} controls autoPlay>
       <source src={this.props.audioUrl} />
       Your browser does not support the audio element.
      </audio>
    )
  }
}
