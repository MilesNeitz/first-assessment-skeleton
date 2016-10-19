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
    if (this.command === 'echo') {
      return (this.timeStamp + ' <' + this.username + '> (echo): ' + this.contents)
    } else if (this.command === 'broadcast') {
      return (this.timeStamp + ' <' + this.username + '> (all): ' + this.contents)
    } else if (this.command.charAt(0) === '@') {
      return (this.timeStamp + ' <' + this.username + '> (whisper): ' + this.contents)
    } else if (this.command === 'connection alert') {
      return (this.timeStamp + ': <' + this.username + '> has ' + this.contents)
    } else if (this.command === 'users') {
      return (this.timeStamp + ': currently connected users:' + this.contents)
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
