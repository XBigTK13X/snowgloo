let config = {
    webServerPort: process.env.SNOWGLOO_WEB_API_PORT || 5051,
    mediaRoot: process.env.SNOWGLOO_MEDIA_ROOT || '/media/trove/media/music',
    mediaServer: process.env.SNOWGLOO_MEDIA_SERVER_URL || 'http://192.168.1.20:5050',
    databaseDirectory: process.env.SNOWGLOO_DATABASE_DIR || '/home/kretst/snowgloo',
    ignoreDatabaseCache: process.env.SNOWGLOO_IGNORE_DATABASE_CACHE || false,
    webApiUrl: process.env.SNOWGLOO_WEB_API_URL || '"http://192.168.1.20:5051/api/"',
    userList: process.env.SNOWGLOO_USER_LIST_CSV || 'Snowman,QTFleur,Link',
    apiPostBodySizeLimit: '100mb',
    serverVersion: '1.6.0',
    buildDate: 'December 01, 2022',
    randomListSize: 40,
    subKinds: ['Single', 'Special', 'Collab', 'Live'],
    sortKinds: ['Album', 'Single', 'Special', 'Collab', 'Live'],
}

config.emptyThumbnailLookupPath = `${config.mediaRoot}/.snowgloo/thumbnails/empty.json`
config.durationLookupPath = `${config.mediaRoot}/.snowgloo/duration.json`
config.randomWeightsPath = `${config.mediaRoot}/.snowgloo/random-weights.json`

config.userList = config.userList.split(',')

console.log('Configuration read as ', { config })

module.exports = config
