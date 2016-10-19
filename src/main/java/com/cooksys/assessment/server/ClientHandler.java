package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);

	private Socket socket;

	private Map<String, Socket> users;
	
	private ExecutorService executor;

	public ClientHandler(Socket socket, Map<String, Socket> users, ExecutorService executor) {
		super();
		this.socket = socket;
		this.users = users;
		this.executor = executor;
	}

	public void run() {
		LinkedBlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>();
		ClientMessageHandler handler = new ClientMessageHandler(socket, messageQueue, users);
		executor.execute(handler);
		while (!socket.isClosed()) {
			try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String raw = reader.readLine();
				ObjectMapper mapper = new ObjectMapper();
				Message message = mapper.readValue(raw, Message.class);
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}

}
