import React, {Component} from 'react'

export default class Admin extends Component {
  constructor(props){
    super(props)
    this.rebuildCatalog = this.rebuildCatalog.bind(this)
    this.state = {
      catalogStatus:null
    }
  }

  rebuildCatalog(){
    this.props.api.catalogRebuild()
  }

  componentDidMount(){
    this.props.api.catalogStatus().then((result)=>{
      console.log({result})
      this.setState({
        catalogStatus: result
      })
    })
  }

  render(){
    let status = null
    if(this.state.catalogStatus){
      if(this.state.catalogStatus.building){
        status = <p>Rebuilding the catalog ({this.state.catalogStatus.rebuildCount}/{this.state.catalogStatus.totalCount}).</p>
      }
      else {
        status = <p>The catalog is finished building.</p>
      }
    }
    return (
      <div>
        <button className="action-button" onClick={this.rebuildCatalog}>
          Rebuild catalog
        </button>
        <br/>
        {status}
      </div>
    )
  }
}
