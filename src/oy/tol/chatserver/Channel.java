package oy.tol.chatserver;

import java.util.ArrayList;
import java.util.List;

import oy.tol.chat.ChangeTopicMessage;
import oy.tol.chat.ErrorMessage;
import oy.tol.chat.Message;
import oy.tol.chat.ChatMessage;

public class Channel {
	private String name = "";
	private String topic = "";
	private List<ChatServerSession> sessions;
	private boolean isPermanent = false;

	public Channel(String name) {
		this.name = name;
		topic = "No topic";
		sessions = new ArrayList<>();
	}

	public Channel(String name, boolean isPermanent) {
		this.name = name;
		topic = "No topic";
		this.isPermanent = isPermanent;
		sessions = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public synchronized String getTopic() {
		return topic;
	}

	public synchronized void setTopic(String topic) {
		this.topic = topic;
		ChangeTopicMessage topicChanged = new ChangeTopicMessage(topic);
		relayMessage(null, topicChanged);
	}

	public boolean isPermanent() {
		return isPermanent;
	}
	
	public synchronized void add(ChatServerSession session) {
		sessions.add(session);
	}

	public synchronized void remove(ChatServerSession session) {
		sessions.remove(session);
	}

	public synchronized boolean hasSessions() {
		return !sessions.isEmpty();
	}

	public synchronized int sessionCount() {
		return sessions.size();
	}

	public synchronized void relayMessage(ChatServerSession fromSession, Message message) {
		System.out.println("Relaying msg to " + sessions.size() + " clients");
		sessions.forEach( session -> {
			if (session != fromSession && !((ChatMessage)message).isDirectMessage()) {
				session.write(message);
			}
		});	
	}

	public boolean relayPrivateMessage(ChatServerSession fromSession, ChatMessage message) {
		for (ChatServerSession session : sessions) {
			if (session != fromSession && message.directMessageRecipient().equals(session.userName())) {
				session.write(message);
				return true;
			}
		}
		return false;
	}


	public synchronized void closeAllSessions(String message) {
		ErrorMessage msg = new ErrorMessage(message, true);
		sessions.forEach( session -> {			
			session.write(msg);
			session.setChannel(null);
			session.close();
		});
		sessions.clear();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Channel: ");
		builder.append(name);
		builder.append(" with topic: \"");
		builder.append(topic);
		builder.append("\" has ");
		builder.append(sessions.size());
		builder.append(" users");
		return builder.toString();
	}

}
