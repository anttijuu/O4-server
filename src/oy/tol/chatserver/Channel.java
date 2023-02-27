package oy.tol.chatserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Channel {
	private String name = "";
	private String topic = "";
	private List<ChatSession> sessions;

	public Channel() {
		name = "main";
		topic = "Everything about the universe and more";
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

	public void add(ChatSession session) throws IOException {
		sessions.add(session);
		session.setChannel(this);
	}

	public void remove(ChatSession session) throws IOException {
		sessions.remove(session);
		session.setChannel(null);
	}

	public boolean hasSessions() {
		return sessions.size() > 0;
	}

	public int sessionCount() {
		return sessions.size();
	}

}
