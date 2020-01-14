import React,{Component} from 'react'

import api from './snowgloo-api-client'

import AudioControls from './AudioControls'
import MediaEntry from './MediaEntry'

export default class App extends Component {
  constructor(props){
    super(props)
    this.api = api
    this.playMedia = this.playMedia.bind(this)
    this.state = {
      files:null
    }
  }

  componentDidMount(){
    this.api.getFiles()
    .then(result=>{
      this.setState({
        files: result
      })
    })
  }

  playMedia(path){
    this.api.playMedia(path)
    .then(result=>{
      this.setState({
        audioUrl: result
      })
    })
  }

  render(){
    if(!this.state.files){
      return null
    }
    return (
      <div>
      <AudioControls audioUrl={this.state.audioUrl} />
      {this.state.files.map((file,fileIndex)=>{
        return (
          <MediaEntry key={fileIndex} path={file} playMedia={this.playMedia}/>
        )
      })}
      </div>
    )
  }
}
