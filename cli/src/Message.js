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
    if (this.command === 'echo') {
      return chalk.yellow((this.timeStamp + ' <' + this.username + '> (echo): ' + this.contents))
    } else if (this.command === 'broadcast') {
      return chalk.magenta((this.timeStamp + ' <' + this.username + '> (all): ' + this.contents))
    } else if (this.command.charAt(0) === '@') {
      return chalk.green((this.timeStamp + ' <' + this.username + '> (whisper): ' + this.contents))
    } else if (this.command === 'connect') {
      return chalk.red((this.timeStamp + ': <' + this.username + '> has connected'))
    } else if (this.command === 'disconnect') {
      return chalk.red((this.timeStamp + ': <' + this.username + '> has disconnected'))
    } else if (this.command === 'users') {
      return chalk.blue((this.timeStamp + ': currently connected users:' + this.contents))
    } else {
      return (this.contents)
    }
//     echo:
// `${timestamp} <${username}> (echo): ${contents}`
//
// broadcast:
// `${timestamp} <${username}> (all): ${contents}`
//
// direct message:
// `${timestamp} <${username}> (whisper): ${contents}`
//
// connection alert:
// `${timestamp}: <${username}> has connected`
// `${timestamp}: <${username}> has disconnected`
//
// users:
// `${timestamp}: currently connected users:`
// (repeated)
// `<${username}>`
  }
}
