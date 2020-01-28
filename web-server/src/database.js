const settings = require('./settings')
const fs = require('fs')
const _ = require('lodash')
const path = require('path')

class Database {
    constructor(name) {
        this.filePath = path.join(settings.databaseDirectory, `${name}.json`)
        this.workingSet = {}
    }

    isEmpty() {
        return _.isEmpty(this.workingSet)
    }

    read() {
        return new Promise((resolve, reject) => {
            if (settings.ignoreDatabaseCache) {
                return resolve(this.workingSet)
            }
            fs.access(this.filePath, err => {
                if (err) {
                    return resolve(this.workingSet)
                }
                fs.readFile(this.filePath, 'utf8', (err, data) => {
                    if (err) {
                        return reject(err)
                    }
                    this.workingSet = JSON.parse(data)
                    return resolve(this.workingSet)
                })
            })
        })
    }

    write(updates) {
        return new Promise((resolve, reject) => {
            if (updates) {
                this.workingSet = {
                    ...this.workingSet,
                    ...updates,
                }
            }
            fs.writeFile(this.filePath, JSON.stringify(this.workingSet), 'utf8', err => {
                if (err) {
                    return reject(err)
                }
                return resolve(this.workingSet)
            })
        })
    }
}

let instances = {}

let getInstance = name => {
    if (!_.has(instances, name)) {
        instances[name] = new Database(name)
    }
    return instances[name]
}

module.exports = {
    getInstance,
}
