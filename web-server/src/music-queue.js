const database = require('./database')

class MusicQueue {
    read(user) {
        if (!user) {
            throw new Error('Unable to read queue.', { user })
        }
        return database.getInstance(`queue-${user}`).read()
    }

    write(user, queue) {
        if (!user || !queue) {
            throw new Error('Unable to persist queue.', { user, queue })
        }
        return database.getInstance(`queue-${user}`).write(queue)
    }
}

let instance
if (!instance) {
    instance = new MusicQueue()
}

module.exports = instance
