const fs = require('fs')
const path = require('path')

const getFrontendSettingsPath = root => {
    const entries = fs.readdirSync(root)
    for (ii = 0; ii < entries.length; ii++) {
        const entry = entries[ii]
        if (entry.indexOf('main') !== -1 && entry.indexOf('chunk.js') !== -1 && entry.indexOf('.map') === -1) {
            return path.join(root, entry)
        }
    }
    return null
}

const tokenSwap = (filePath, tokenMap) => {
    let content = fs.readFileSync(filePath, 'utf8')
    for (let tokenKey of Object.keys(tokenMap)) {
        content = content.replace(tokenKey, tokenMap[tokenKey])
    }
    fs.writeFileSync(filePath, content)
}

module.exports = {
    getFrontendSettingsPath,
    tokenSwap,
}
