/* eslint no-undef: 0 */

let webApiUrl
let castPollMilliseconds
let debounceMilliseconds
let clientVersion = '1.3.5'
let buildDate = 'June 24, 2021'

try {
    webApiUrl = WEB_API_URL
    castPollMilliseconds = CAST_POLL_INTERVAL
    debounceMilliseconds = DEBOUNCE_MILLISECONDS
} catch {
    webApiUrl = 'http://192.168.1.20:5051/api/'
    castPollMilliseconds = 300
    debounceMilliseconds = 300
}
module.exports = {
    castPollMilliseconds,
    debounceMilliseconds,
    webApiUrl,
    clientVersion,
    buildDate,
}
