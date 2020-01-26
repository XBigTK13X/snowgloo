import React, { Component } from 'react'

import settings from '../settings'

export default class Admin extends Component {
    constructor(props) {
        super(props)
        this.rebuildCatalog = this.rebuildCatalog.bind(this)
        this.state = {
            catalogStatus: null,
            systemInfo: null,
        }
    }

    rebuildCatalog() {
        this.props.api.catalogRebuild()
    }

    componentDidMount() {
        Promise.all([this.props.api.catalogStatus(), this.props.api.systemInfo()]).then(results => {
            this.setState({
                catalogStatus: results[0],
                systemInfo: results[1],
            })
        })
    }

    render() {
        let status = null
        if (this.state.catalogStatus) {
            if (this.state.catalogStatus.building) {
                status = (
                    <p>
                        Rebuilding the catalog ({this.state.catalogStatus.rebuildCount}/{this.state.catalogStatus.totalCount}).
                    </p>
                )
            } else {
                status = <p>The catalog is finished building.</p>
            }
        }
        let systemInfo = null
        if (this.state.systemInfo) {
            systemInfo = (
                <table className="compact-table">
                    <tbody>
                        <tr>
                            <td>Client Version</td>
                            <td>{settings.clientVersion}</td>
                        </tr>
                        <tr>
                            <td>Server Version</td>
                            <td>{this.state.systemInfo.version}</td>
                        </tr>
                        <tr>
                            <td>Client Built</td>
                            <td>{settings.buildDate}</td>
                        </tr>
                        <tr>
                            <td>Server Built</td>
                            <td>{this.state.systemInfo.buildDate}</td>
                        </tr>
                    </tbody>
                </table>
            )
        }
        return (
            <div>
                <button className="action-button" onClick={this.rebuildCatalog}>
                    Rebuild catalog
                </button>
                <br />
                {status}
                {systemInfo}
            </div>
        )
    }
}
