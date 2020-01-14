/* eslint no-undef: 0 */

let webApiUrl = "";

try {
  webApiUrl = WEB_API_URL
}
catch{
  webApiUrl = 'http://localhost:5051/api/'
}
module.exports = {
  webApiUrl
}
