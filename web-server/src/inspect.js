const jsmediatags = require('jsmediatags')
const spawn = require('child_process').spawn
const btoa = require('btoa')

const probe = file => {
    return new Promise((resolve, reject) => {
        let proc = spawn('/usr/bin/ffprobe', ['-hide_banner', '-loglevel', 'fatal', '-show_error', '-show_format', '-show_streams', '-show_programs', '-show_chapters', '-show_private_data', '-print_format', 'json', file])
        let probeData = []
        let errData = []

        proc.stdout.setEncoding('utf8')
        proc.stderr.setEncoding('utf8')

        proc.stdout.on('data', function(data) {
            probeData.push(data)
        })
        proc.stderr.on('data', function(data) {
            errData.push(data)
        })

        proc.on('exit', code => {
            exitCode = code
        })
        proc.on('error', err => reject(err))
        proc.on('close', () => resolve(JSON.parse(probeData.join(''))))
    })
}

const audio = path => {
    return probe(path).then(info => {
        return info
    })
}

const embeddedArt = (path) => {
    return new Promise((resolve, reject) => {
        jsmediatags.read(path, {
            onSuccess: function(tags) {
                let picture = tags.tags.picture
                if (!picture) {
                    return resolve(null)
                }
                resolve({
                    content: Buffer.from(picture.data),
                    extension: picture.format.split('/')[1]
                })
            },
            onError: function(error) {
                resolve(null)
            },
        })
    })
}

module.exports = {
    audio,
    embeddedArt,
}
