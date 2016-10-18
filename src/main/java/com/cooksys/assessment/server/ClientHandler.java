package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;

	public ClientHandler(Socket socket) {
		super();
		this.socket = socket;
	}

	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				java.util.Date date= new java.util.Date();
				message.setTimeStamp(String.valueOf(date.toString()));
				switch (message.getCommand()) {
					case "connect":
						log.info(message.getTimeStamp() + " user <{}> connected", message.getUsername());
						Server.users.put(message.getUsername(),this.socket);
						log.debug(Server.users.toString());
						break;
					case "disconnect":
						log.info(message.getTimeStamp() + " user <{}> disconnected", message.getUsername());
						Server.users.remove(message.getUsername()); // add a way to see when they close
						this.socket.close();
						break;
					case "echo":
						log.info(message.getTimeStamp() + " user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						String echo = mapper.writeValueAsString(message);
						writer.write(echo);
						writer.flush();
						break;
					case "broadcast":
						log.info(message.getTimeStamp() + " user <{}> broadcasted to all users <{}>", message.getUsername(), message.getContents());
						String broadcast = mapper.writeValueAsString(message);
						for(String currentKey : Server.users.keySet()) {
							PrintWriter tempWriter = new PrintWriter(new OutputStreamWriter(Server.users.get(currentKey).getOutputStream()));
							tempWriter.write(broadcast);
							tempWriter.flush();
						}
						break;
					case "users":
						log.info(message.getTimeStamp() + " user <{}> requested all users <{}>", message.getUsername(), (Server.users.keySet()).toString());
						message.setContents((Server.users.keySet()).toString());
						String users = mapper.writeValueAsString(message);
						writer.write(users);
						writer.flush();
						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
