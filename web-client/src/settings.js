/* eslint no-undef: 0 */

let webApiUrl = ''

try {
    webApiUrl = WEB_API_URL
} catch {
    webApiUrl = 'http://192.168.1.20:5051/api/'
}
module.exports = {
    webApiUrl,
}
