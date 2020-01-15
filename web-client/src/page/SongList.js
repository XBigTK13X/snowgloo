import React,{Component} from 'react'
import Comp from '../comp'

export default class App extends Component {
  constructor(props){
    super(props)

    this.state = {
      files:null
    }
  }

  componentDidMount(){
    console.log({props:this.props})
    this.props.api.getFiles()
    .then(result=>{
      this.setState({
        files: result
      })
    })
  }

  render(){
    if(!this.state.files){
      return null
    }
    return (
      <div>
      {this.state.files.map((file,fileIndex)=>{
        return (
          <Comp.MediaEntry key={fileIndex} path={file.Path} playMedia={this.props.playMedia}/>
        )
      })}
      </div>
    )
  }
}
