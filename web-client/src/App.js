import React,{Component} from 'react'
import { UIRouter, UIView, pushStateLocationPlugin } from '@uirouter/react';
import routes from './routes'
import Comp from './comp'
import service from './service'

const plugins = [pushStateLocationPlugin];

const configRouter = (router)=>{
  router.urlRouter.otherwise('/')
}

export default class App extends Component {
  constructor(props){
    super(props)
    this.playMedia = this.playMedia.bind(this)
    this.state = {
      audioUrl:null
    }
  }

  playMedia(path){
    service.api.playMedia(path)
    .then(result=>{
      this.setState({
        audioUrl: result
      })
    })
  }

  render(){
    return (
    <div>
      <UIRouter plugins={plugins} states={routes} config={configRouter} >
        <Comp.AudioControls audioUrl={this.state.audioUrl} />
        <Comp.NavBar />
        <UIView render={(Component,props)=>{
          return <Component {...props} playMedia={this.playMedia} api={service.api} />
        }} />
      </UIRouter>
    </div>
  )
  }
}
