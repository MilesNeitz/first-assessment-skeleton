package com.cooksys.assessment.server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientMessageHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientMessageHandler.class);

	private LinkedBlockingQueue<Message> messageQueue;

	private Socket socket;

	private Map<String, Socket> users;

	public ClientMessageHandler(Socket socket, LinkedBlockingQueue<Message> messageQueue, Map<String, Socket> users) {
		this.messageQueue = messageQueue;
		this.users = users;
		this.socket = socket;
	}

	public void sendMessage(Message m) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		String echo = mapper.writeValueAsString(m);
		writer.write(echo);
		writer.flush();
	} 
	
	public void sendBroadcast(Message m) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String connect = mapper.writeValueAsString(m);
		// sends a message to all connected users
		for (String currentKey : users.keySet()) {
			PrintWriter tempWriter = new PrintWriter(
					new OutputStreamWriter(users.get(currentKey).getOutputStream()));
			tempWriter.write(connect);
			tempWriter.flush();
		}
	}
	
	public void sendWhisper(Message m) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		String whisper = mapper.writeValueAsString(m);
		// get the socket of the user you want to message
		PrintWriter tempWriter = new PrintWriter(
				new OutputStreamWriter(users.get(m.getCommand().substring(1)).getOutputStream()));
		tempWriter.write(whisper);
		tempWriter.flush();
	}
	
	public void run() {
		try {
			while (!socket.isClosed()) {
				// waits till there is a message in queue then pulls the first message
				Message message = messageQueue.take();

				java.util.Date date = new java.util.Date();
				DateFormat df = new SimpleDateFormat("HH:mm:ss");
				message.setTimeStamp(df.format(date));
				// finds the right command and sends the message
				switch (message.getCommand()) {
				case "connect":
					log.info(message.getTimeStamp() + " user <{}> connected", message.getUsername());
					users.put(message.getUsername(), this.socket);
					log.debug(users.toString());
					message.setCommand("connect");
					sendBroadcast(message);
					break;
				case "disconnect":
					log.info(message.getTimeStamp() + " user <{}> disconnected", message.getUsername());
					users.remove(message.getUsername());
					message.setCommand("disconnect");
					sendBroadcast(message);
					this.socket.close();
					break;
				case "echo":
					log.info(message.getTimeStamp() + " user <{}> echoed message <{}>", message.getUsername(),
							message.getContents());
					sendMessage(message);
					break;
				case "broadcast":
					log.info(message.getTimeStamp() + " user <{}> broadcasted to all users <{}>", message.getUsername(),
							message.getContents());
					sendBroadcast(message);
					break;
				case "users":
					log.info(message.getTimeStamp() + " user <{}> requested all users <{}>", message.getUsername(),
							(users.keySet()).toString());
					message.setContents(((users.keySet()).toString()).replaceAll("\\[|\\]|\\ ", ""));
					sendMessage(message);
					break;
				}
				// checks to see if the command is a whisper
				if ((message.getCommand().substring(0, 1)).equals("@")) {
					// checks to see if whisper is to a valid user
					if (users.containsKey((message.getCommand().substring(1)))) {
						log.info(message.getTimeStamp() + " user <{}> wispered to <{}>", message.getUsername(),
								message.getCommand().substring(1));
						sendWhisper(message);
						
					// if not valid send a failedWhisper message
					} else {
						log.debug(message.getCommand().substring(1));
						log.debug(users.keySet().toString());
						log.info(message.getTimeStamp() + " user <{}> tried to message <{}>", message.getUsername(),
								message.getCommand().substring(1));
						message.setContents(message.getCommand().substring(1));
						message.setCommand("failedWhisper");
						sendMessage(message);
					}
				}
			}
		} catch (IOException | InterruptedException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
