const catalog = require('./catalog')

const register = router => {
    router.get('/api/song/list', async (ctx, next) => {
        ctx.body = await catalog.getSongs()
    })
    router.get('/api/album/list', async (ctx, next) => {
        ctx.body = await catalog.getAlbums()
    })
    router.get('/api/artist/list', async (ctx, next) => {
        ctx.body = await catalog.getArtists()
    })
    router.get('/api/artist/view', async (ctx, next) => {
        let result = {
            albums: await catalog.getAlbums(decodeURIComponent(ctx.request.query.artist)),
        }
        ctx.body = result
    })
    router.get('/api/album/view', async (ctx, next) => {
        let result = {
            album: await catalog.getAlbum(decodeURIComponent(ctx.request.query.albumSlug)),
        }
        ctx.body = result
    })
}

module.exports = {
    register,
}
