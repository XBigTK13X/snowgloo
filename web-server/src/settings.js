const config = {
    webServerPort: process.env.SNOWGLOO_WEB_API_PORT || 5051,
    mediaRoot: process.env.SNOWGLOO_MEDIA_ROOT || '/media/trove/media/music',
    mediaServer: process.env.SNOWGLOO_MEDIA_SERVER_URL || 'http://192.168.1.20:5050',
    databasePath: process.env.SNOWGLOO_DATABASE_PATH || '/tmp/snowgloo.json',
    ignoreDatabaseCache: process.env.SNOWGLOO_IGNORE_DATABASE_CACHE || false,
}

console.log('Configuration read as ', { config })

module.exports = config
