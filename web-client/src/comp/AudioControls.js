import React,{Component} from 'react'

import Comp from './'

import service from '../service'

export default class AudioControls extends Component {
  constructor(props){
    super(props)
    this.state = {
      player: service.audio
    }
    this.audio = React.createRef()
  }

  render(){
    if(!this.props.song){
      return null
    }
    if(this.audio.current){
      this.audio.current.setAttribute('src',this.props.song.Path)
      this.audio.current.load()
      this.audio.current.play()
    }
    return (
      <div className="sticky-footer centered">
        <audio ref={this.audio} controls autoPlay>
         <source src={this.props.song.Path} />
         Your browser does not support the audio element.
        </audio>
        <div className="neighbor">
          <p className="short-text" title={this.props.song.Title}>
            {this.props.song.Title}
          </p>
          <p className="short-text" title={this.props.song.Album}>
            {this.props.song.Album}
          </p>
          <p className="short-text" title={this.props.song.Artist}>
            {this.props.song.Artist}
          </p>
        </div>
        <div className="neighbor">
          <Comp.CoverArt size="small" imageUrl={this.props.song.CoverArt} />
        </div>
      </div>
    )
  }
}
