const probe = require('node-ffprobe')

const audio = (path)=>{
  return probe(path)
  .then(info=>{
    return info
  })
}

module.exports = {
  audio
}
