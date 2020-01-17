console.log('Reading catalog into memory')
const catalog = require('./catalog')
catalog
    .build()
    .then(() => {
        console.log('Catalog ready')
    })
    .catch(err => {
        console.err('Unable to build the catalog', { err })
    })

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

const routes = require('./routes')

routes.register(router)

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
            WEB_API_URL: process.env.SNOWGLOO_WEB_API_URL,
        })
    }
    console.log(`Hosting static files`)
    app.use(static(webRoot))
} else {
    console.log(`No static web files found at ${webRoot}.`)
}
app.use(router.routes()).use(router.allowedMethods())
console.log(`Server listening on ${settings.webServerPort}`)
app.listen(settings.webServerPort)
