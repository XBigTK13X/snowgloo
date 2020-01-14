const fs = require('fs')
const path = require('path')

const settings = require('./settings')
const fileSystem = require('./file-system')

const Koa = require('koa')
const Router = require('koa-router')
const cors = require('@koa/cors')
const static = require('koa-static')
const koaBody = require('koa-body')

const app = new Koa()
const router = new Router()

console.log("Building catalog in-memory cache")
const catalog = require('./catalog')
catalog.readFiles()
router.get('/api/files', async (ctx, next) => {
    ctx.body = await catalog.readFiles()
})
router.post('/api/media/play', async (ctx, next)=>{
  ctx.body = await catalog.playMedia(ctx.request.body.path)
})

app.use(cors())
app.use(koaBody())
const webRoot = path.join(__dirname, 'web-build')
if (fs.existsSync(webRoot)) {
    console.log(`Web root found at ${webRoot}.`)
    const settingsPath = fileSystem.getFrontendSettingsPath(path.join(webRoot, 'static', 'js'))
    console.log(`Checking for file to token swap at ${settingsPath}`)
    if (settingsPath) {
        console.log(`Swapping tokens in ${settingsPath}`)
        fileSystem.tokenSwap(settingsPath, {
            WEB_API_URL: process.env.ARCHIVIST_WEB_API_URL,
        })
    }
    console.log(`Hosting static files`)
    app.use(static(webRoot))
} else {
    console.log(`No static web files found at ${webRoot}.`)
}
app.use(router.routes()).use(router.allowedMethods())

app.listen(settings.webServerPort)
