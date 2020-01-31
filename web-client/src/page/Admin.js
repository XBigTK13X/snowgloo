import React, { Component } from 'react'

import settings from '../settings'

export default class Admin extends Component {
    constructor(props) {
        super(props)
        this.rebuildCatalog = this.rebuildCatalog.bind(this)
        this.clearQueues = this.clearQueues.bind(this)
        this.state = {
            catalogStatus: null,
            systemInfo: null,
            deletedPlaylists: null,
        }
    }

    clearQueues() {
        this.props.api.clearQueues()
    }

    rebuildCatalog() {
        this.props.api.catalogRebuild()
    }

    restorePlaylist(playlist) {
        playlist.deleted = false
        this.props.api.savePlaylist(playlist).then(() => {
            this.props.api.getDeletedPlaylists().then(result => {
                this.setState({
                    deletedPlaylists: result.playlists,
                })
            })
        })
    }

    componentDidMount() {
        Promise.all([this.props.api.catalogStatus(), this.props.api.systemInfo(), this.props.api.getDeletedPlaylists()]).then(results => {
            this.setState({
                catalogStatus: results[0],
                systemInfo: results[1],
                deletedPlaylists: results[2].playlists,
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
        let deletedPlaylists = null
        if (this.state.deletedPlaylists && this.state.deletedPlaylists.length) {
            deletedPlaylists = (
                <div>
                    <h3>Deleted Playlists</h3>
                    <table>
                        <thead>
                            <th>Name</th>
                            <th>Id</th>
                            <th>Songs</th>
                            <th>Restore</th>
                        </thead>
                        <tbody>
                            {this.state.deletedPlaylists.map((playlist, playlistIndex) => {
                                return (
                                    <tr key={playlistIndex}>
                                        <td>{playlist.name}</td>
                                        <td>{playlist.id}</td>
                                        <td>{playlist.songs.length}</td>
                                        <td>
                                            <button
                                                onClick={() => {
                                                    this.restorePlaylist(playlist)
                                                }}
                                            >
                                                Restore
                                            </button>
                                        </td>
                                    </tr>
                                )
                            })}
                        </tbody>
                    </table>
                </div>
            )
        }
        return (
            <div>
                <button className="action-button" onClick={this.rebuildCatalog}>
                    Rebuild catalog
                </button>
                <button className="action-button" onClick={this.clearQueues}>
                    Clear all queues
                </button>
                <br />
                {status}
                {systemInfo}
                {deletedPlaylists}
            </div>
        )
    }
}
