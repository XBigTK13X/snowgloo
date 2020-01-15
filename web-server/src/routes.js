const catalog = require('./catalog')

const register = (router)=>{
  router.get('/api/files', async (ctx, next) => {
      ctx.body = await catalog.readFiles()
  })
  router.post('/api/media/play', async (ctx, next)=>{
    ctx.body = await catalog.playMedia(ctx.request.body.path)
  })
}

module.exports ={
  register
} 
