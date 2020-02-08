const settings = require('./settings')
const catalog = require('./catalog')
const musicQueue = require('./music-queue')
const playlists = require('./playlists')
const inspect = require('./inspect')
const asset = require('./asset')
const log = require('./log')

const register = router => {
    router.get('/api/song/list', async (request, response) => {
        response.send(await catalog.getSongs())
    })

    router.get('/api/album/list', async (request, response) => {
        let result = {
            albums: await catalog.getAlbums(),
        }
        response.send(result)
    })
    router.get('/api/artist/list', async (request, response) => {
        response.send(await catalog.getArtists())
    })
    router.get('/api/artist/view', async (request, response) => {
        let result = {
            albums: await catalog.getAlbums(decodeURIComponent(request.query.artist)),
            artist: request.query.artist
        }
        response.send(result)
    })
    router.get('/api/album/view', async (request, response) => {
        let result = {
            album: await catalog.getAlbum(decodeURIComponent(request.query.albumSlug)),
        }
        response.send(result)
    })

    router.get('/api/catalog/build/status', async (request, response) => {
        response.send(catalog.status())
    })

    router.get('/api/user/list', async (request, response) => {
        response.send({
            users: settings.userList,
        })
    })

    router.get('/api/queue/:username', async (request, response) => {
        response.send(await musicQueue.read(request.params.username))
    })

    router.post('/api/queue/:username', async (request, response) => {
        response.send({
            queue: await musicQueue.write(request.params.username, request.body.queue),
        })
    })

    router.delete('/api/queue/:username', async (request, response) => {
        response.send(await musicQueue.clear(request.params.username))
    })

    router.get('/api/system/info', async (request, response) => {
        response.send({
            version: settings.serverVersion,
            buildDate: settings.buildDate,
        })
    })

    router.get('/api/search', async (request, response) => {
        response.send(await catalog.search(request.query.query))
    })

    router.post('/api/playlist', async (request, response) => {
        response.send(await playlists.write(request.body.playlist))
    })

    router.get('/api/playlist/view', async (request, response) => {
        response.send(await playlists.read(request.query.playlistId))
    })

    router.get('/api/playlist/list', async (request, response) => {
        response.send(await playlists.readAll())
    })

    router.post('/api/admin/catalog/build', async (request, response) => {
        catalog.build(true)
        response.send('Building')
    })

    router.post('/api/admin/queues/clear', async (request, response) => {
        musicQueue.clearAll()
        response.send('Cleared')
    })

    router.get('/api/admin/playlists/deleted', async (request, response) => {
        response.send({
            playlists: await playlists.getDeleted(),
        })
    })

    router.post('/api/admin/log', async (request, response) => {
        let logFile = log.getInstance(`client-${request.body.clientId}`)
        let logs = await logFile.read()
        if(!logs){
            logs = {
                entries: []
            }
        }
        logs.entries.push(request.body.message)
        response.send(await logFile.write(logs))
    })

    router.get('/api/admin/log', async (request, response) => {
        let logFile = log.getInstance(`client-${request.query.clientId}`)
        response.send({
            logs: await logFile.read(logFile)
        })
    })
}

module.exports = {
    register,
}
