const _ = require('lodash')

const searchify = text => {
    return _.deburr(text)
        .toLowerCase()
        .replace(/\s/g, '')
}

module.exports = {
    searchify,
}
