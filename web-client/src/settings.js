/* eslint no-undef: 0 */

let webApiUrl
let castPollMilliseconds
let debounceMilliseconds
let clientVersion = '1.4.17'
let buildDate = 'August 15, 2022'
let songDurationMinimumSeconds = 10

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
    songDurationMinimumSeconds,
}
