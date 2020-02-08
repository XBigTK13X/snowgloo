import React,{Component} from 'react'

export default class AdminLogs extends Component{
    constructor(props){
        super(props)
        this.state = {
            clientId: "",
            logs: {
                entries: []
            },
            err: null
        }
        this.changeClientId = this.changeClientId.bind(this);
        this.getLogs = this.getLogs.bind(this);
    }
    changeClientId(e){
        this.setState({
            clientId: e.target.value
        })
    }
    getLogs(){
        this.props.api.getLogs(this.state.clientId)
        .then(result=>{
            this.setState({
                logs:result.logs
            })
        })
        .catch(err=>{
            this.setState({
                err
            })
        })
    }
    render(){
        let logs = this.state.logs.entries.length ? (
            <div>
                {this.state.logs.entries.map(entry=>{
                    return <p>{entry}</p>
                })}
            </div>
        ) : null

        let err = this.state.err ? (
            <p>{JSON.stringify(this.state.err)}</p>
        ) : null

        return (
            <div>
                <input className="large-text-input" autoFocus={true} type="text" name="clientId" onChange={this.changeClientId} value={this.state.clientId}/>
                <button onClick={this.getLogs}>Load logs</button>
                {err}
                {logs}
            </div>
        )
    }
}
