export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

  constructor ({ timeStamp, username, command, contents }) {
    this.timeStamp = timeStamp
    this.username = username
    this.command = command
    this.contents = contents
  }

  toJSON () {
    return JSON.stringify({
      timeStamp: this.timeStamp,
      username: this.username,
      command: this.command,
      contents: this.contents
    })
  }

  toString () {
    var chalk = require('chalk')
    switch (this.command) {
      case 'echo' :
        return chalk.yellow((this.timeStamp + ' <' + this.username + '> (echo): ' + this.contents))
      case 'broadcast' :
        return chalk.magenta((this.timeStamp + ' <' + this.username + '> (all): ' + this.contents))
      case 'failedWhisper' :
        return chalk.red((this.timeStamp + ': <' + this.contents + '> is not connected'))
      case 'connect' :
        return chalk.grey((this.timeStamp + ': <' + this.username + '> has connected'))
      case 'disconnect' :
        return chalk.grey((this.timeStamp + ': <' + this.username + '> has disconnected'))
      case 'users' :
        let returnString = chalk.blue((this.timeStamp + ': currently connected users:'))
        let usersArray = ((this.contents).split(','))
        usersArray.forEach((user) => {
          returnString = returnString + '\n' + chalk.blue('<' + user + '>')
        })
        return returnString
      default :
        if (this.command.charAt(0) === '@') {
          return chalk.green((this.timeStamp + ' <' + this.username + '> (whisper): ' + this.contents))
        } else {
          return (`Command <${this.command}> was not recognized`)
        }
    }
  }
}
