const catalog = require('./catalog')
catalog.build(false).catch(err => {
    console.err('Unable to build the catalog', { err })
})

const fs = require('fs')
const path = require('path')

const settings = require('./settings')
const fileSystem = require('./file-system')

const express = require('express')
const cors = require('cors')

const app = express()
app.use(cors())
app.use(express.json({ limit: settings.apiPostBodySizeLimit }))

const routes = require('./routes')

routes.register(app)

const webRoot = path.join(__dirname, 'web-build')
if (fs.existsSync(webRoot)) {
    console.log(`Web root found at ${webRoot}.`)
    const settingsPath = fileSystem.getFrontendSettingsPath(path.join(webRoot, 'static', 'js'))
    console.log(`Checking for file to token swap at ${settingsPath}`)
    if (settingsPath) {
        console.log(`Swapping tokens in ${settingsPath}`)
        fileSystem.tokenSwap(settingsPath, {
            WEB_API_URL: settings.webApiUrl,
            CAST_POLL_INTERVAL: 300,
            DEBOUNCE_MILLISECONDS: 300
        })
    }
    console.log(`Hosting static files`)
    app.use('/', express.static(webRoot))
    app.use('/*', (request, response) => {
        response.sendFile(path.join(webRoot, 'index.html'))
    })
} else {
    console.log(`No static web files found at ${webRoot}.`)
}

app.listen(settings.webServerPort, () => {
    console.log(`Server listening on ${settings.webServerPort}`)
})
