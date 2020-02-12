const config = {
    webServerPort: process.env.SNOWGLOO_WEB_API_PORT || 5051,
    mediaRoot: process.env.SNOWGLOO_MEDIA_ROOT || '/media/trove/media/music',
    mediaServer: process.env.SNOWGLOO_MEDIA_SERVER_URL || 'http://192.168.1.20:5050',
    databaseDirectory: process.env.SNOWGLOO_DATABASE_DIR || '/home/kretst/snowgloo',
    ignoreDatabaseCache: process.env.SNOWGLOO_IGNORE_DATABASE_CACHE || false,
    webApiUrl: process.env.SNOWGLOO_WEB_API_URL || '"http://192.168.1.20:5051/api/"',
    userList: process.env.SNOWGLOO_USER_LIST_CSV || 'Snowman,QTFleur',
    apiPostBodySizeLimit: '100mb',
    serverVersion: '0.8.5',
    buildDate: 'February 12, 2020',
}

config.userList = config.userList.split(',')

console.log('Configuration read as ', { config })

module.exports = config
