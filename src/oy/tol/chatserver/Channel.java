package oy.tol.chatserver;

import java.util.ArrayList;
import java.util.List;

import oy.tol.chat.ErrorMessage;
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
		topic = "No topic";
		sessions = new ArrayList<>();
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
		StatusMessage topicChanged = new StatusMessage("Channel topic changed to " + topic);
		relayMessage(null, topicChanged);
	}

	public void add(ChatServerSession session) {
		sessions.add(session);
	}

	public void remove(ChatServerSession session) {
		sessions.remove(session);
	}

	public boolean hasSessions() {
		return !sessions.isEmpty();
	}

	public int sessionCount() {
		return sessions.size();
	}

	public void relayMessage(ChatServerSession fromSession, Message message) {
		sessions.forEach( session -> {
			if (session != fromSession) {
				session.write(message);
			}
		});	
	}

	public void closeAllSessions(String message) {
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
