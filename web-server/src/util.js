const _ = require('lodash')
const hash = require('object-hash')

const searchify = text => {
    return _.deburr(text)
        .toLowerCase()
        .replace(/\W/g, '')
        .replace(/\s/g, '')
}

const sortify = text => {
    return text
        .toLowerCase()
        .replace('the ','')
}

const alphabetize = (items, property) => {
    if (property) {
        return items.sort((a, b) => {
            return sortify(a[property]) > sortify(b[property]) ? 1 : -1
        })
    }
    return items.sort((a, b) => {
        return sortify(a) > sortify(b) ? 1 : -1
    })
}

const contentHash = (content)=>{
    return hash(content,{algorithm:'md5'})
}

module.exports = {
    searchify,
    sortify,
    alphabetize,
    contentHash
}
