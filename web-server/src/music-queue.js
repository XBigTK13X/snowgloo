const database = require('./database')
const settings = require('./settings')

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

    clear(user) {
        return this.write(user, {
            songs: [],
            currentIndex: null,
        })
    }

    clearAll() {
        settings.userList.forEach(user => {
            this.clear(user)
        })
    }
}

let instance
if (!instance) {
    instance = new MusicQueue()
}

module.exports = instance
