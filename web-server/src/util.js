const _ = require('lodash')
const hash = require('object-hash')

const searchify = text => {
    return _.deburr(text)
        .toLowerCase()
        .replace(/\W/g, '')
        .replace(/\s/g, '')
}

const alphabetize = (items, property) => {
    if (property) {
        return items.sort((a, b) => {
            return a[property].toLowerCase() > b[property].toLowerCase() ? 1 : -1
        })
    }
    return items.sort((a, b) => {
        return a.toLowerCase() > b.toLowerCase() ? 1 : -1
    })
}

const contentHash = (content)=>{
    return hash(content,{algorithm:'md5'})
}

module.exports = {
    searchify,
    alphabetize,
    contentHash
}
