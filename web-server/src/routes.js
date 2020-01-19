const settings = require('./settings')
const catalog = require('./catalog')
const musicQueue = require('./music-queue')

const register = router => {
    router.get('/api/song/list', async (request, response) => {
        response.send(await catalog.getSongs())
    })
    router.get('/api/album/list', async (request, response) => {
        response.send(await catalog.getAlbums())
    })
    router.get('/api/artist/list', async (request, response) => {
        response.send(await catalog.getArtists())
    })
    router.get('/api/artist/view', async (request, response) => {
        let result = {
            albums: await catalog.getAlbums(decodeURIComponent(request.query.artist)),
        }
        response.send(result)
    })
    router.get('/api/album/view', async (request, response) => {
        let result = {
            album: await catalog.getAlbum(decodeURIComponent(request.query.albumSlug)),
        }
        response.send(result)
    })

    router.post('/api/catalog/build', async (request, response) => {
        catalog.build(true)
        response.send('Building')
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
        response.send({
            queue: await musicQueue.read(request.params.username),
        })
    })

    router.post('/api/queue/:username', async (request, response) => {
        response.send({
            queue: await musicQueue.write(request.params.username, request.body.queue),
        })
    })
}

module.exports = {
    register,
}
