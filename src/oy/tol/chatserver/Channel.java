package oy.tol.chatserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import oy.tol.chat.Message;
import oy.tol.chat.StatusMessage;

public class Channel {
	private String name = "";
	private String topic = "";
	private List<ChatServerSession> sessions;

	public Channel() {
		name = "main";
		topic = "Everything about the universe and more you could ever dream of";
		sessions = new ArrayList<>();
	}

	public Channel(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public void add(ChatServerSession session) throws IOException {
		sessions.add(session);
		session.setChannel(this);
	}

	public void remove(ChatServerSession session) throws IOException {
		sessions.remove(session);
		session.setChannel(null);
	}

	public boolean hasSessions() {
		return !sessions.isEmpty();
	}

	public int sessionCount() {
		return sessions.size();
	}

	public void relayMessage(ChatServerSession fromSession, Message message) {
		sessions.forEach( session -> {
			try {
				if (session != fromSession) {
					session.write(message);
				}
			} catch (IOException e) {
				// Log error
				System.out.println("Could not write to session, closing it: " + e.getLocalizedMessage());
				sessions.remove(session);
				session.close();
			}
		});	
	}

	public void closeAllSessions(String message) {
		StatusMessage msg = new StatusMessage(message);
		sessions.forEach( session -> {			
			try {
				session.write(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
			session.close();
		});
	}

}
