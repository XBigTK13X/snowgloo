const settings = require('./settings')
const fs = require('fs')
const _ = require('lodash')
const path = require('path')
const mkdirp = require('mkdirp')

class Log {
    constructor(name) {
        this.filePath = path.join(settings.databaseDirectory, `/log/${name}.json`)
        let dirPath = path.dirname(this.filePath)
        if (!fs.existsSync(dirPath)) {
            mkdirp.sync(dirPath)
        }
    }

    read() {
        return new Promise((resolve, reject) => {
            fs.access(this.filePath, err => {
                if (err) {
                    return resolve(null)
                }
                fs.readFile(this.filePath, 'utf8', (err, data) => {
                    if (err) {
                        return reject(err)
                    }
                    return resolve(JSON.parse(data))
                })
            })
        })
    }

    write(logs) {
        return new Promise((resolve, reject) => {
            fs.writeFile(this.filePath, JSON.stringify(logs, null, '\t'), 'utf8', err => {
                if (err) {
                    return reject(err)
                }
                return resolve()
            })
        })
    }
}

let instances = {}

let getInstance = name => {
    if (!_.has(instances, name)) {
        instances[name] = new Log(name)
    }
    return instances[name]
}

module.exports = {
    getInstance,
}
