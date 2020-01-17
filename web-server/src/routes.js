const catalog = require('./catalog')

const register = (router)=>{
  router.get('/api/song/list', async (ctx, next) => {
      ctx.body = await catalog.getSongs()
  })
  router.get('/api/album/list', async (ctx, next) => {
      ctx.body = await catalog.getAlbums()
  })
  router.get('/api/artist/list', async (ctx, next) => {
      ctx.body = await catalog.getArtists()
  })
}

module.exports ={
  register
}
