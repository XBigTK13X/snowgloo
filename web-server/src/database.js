const settings = require('./settings')
const fs = require('fs')
const _ = require('lodash')

class Database {
    constructor() {
        this.filePath = settings.databasePath
        this.workingSet = {}
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

    isEmpty() {
        return _.isEmpty(this.workingSet)
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

let instance
if (!instance) {
    instance = new Database()
}

module.exports = instance
