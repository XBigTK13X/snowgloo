import React, { Component } from 'react'
import _ from 'lodash'

export default class AdminLogs extends Component {
    constructor(props) {
        super(props)
        this.state = {
            clientId: 'null',
            logs: {},
            err: null,
            clientLogs: [],
        }
        this.selectClient = this.selectClient.bind(this)
        this.wipeLogs = this.wipeLogs.bind(this)
        this.persistLogs = this.persistLogs.bind(this)
    }

    componentDidMount() {
        this.props.api.getLogs().then(result => {
            this.setState({
                logs: result.logs,
            })
        })
    }

    selectClient(e) {
        console.log(e.target.value)
        this.setState({
            clientId: e.target.value,
        })
    }

    wipeLogs() {
        this.props.api.wipeLogs().then(() => {
            this.setState({
                logs: {},
            })
        })
    }

    persistLogs() {
        this.props.api.persistLogs()
    }

    render() {
        let err = this.state.err ? <p>{JSON.stringify(this.state.err)}</p> : null

        let clientPicker = !_.isEmpty(this.state.logs) ? (
            <label className="simple-input">
                Select a client to view
                <select className="simple-input" value={this.state.clientId} onChange={this.selectClient}>
                    <option key="null" id="null" name="null"></option>
                    {Object.keys(this.state.logs).map((clientKey, clientIndex) => {
                        return (
                            <option key={clientIndex} id={clientKey} name={clientKey}>
                                {clientKey}
                            </option>
                        )
                    })}
                </select>
            </label>
        ) : null

        let logs =
            this.state.clientId !== 'null' && this.state.logs[this.state.clientId] ? (
                <div>
                    {this.state.logs[this.state.clientId].workingSet.entries.map(entry => {
                        return <p>{entry}</p>
                    })}
                </div>
            ) : null

        return (
            <div>
                <button className="simple-input" onClick={this.persistLogs}>
                    Persist Logs
                </button>
                <button className="simple-input" onClick={this.wipeLogs}>
                    Delete Logs
                </button>
                {err}
                {clientPicker}
                {logs}
            </div>
        )
    }
}
