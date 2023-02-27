package oy.tol.chatserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatChannels {

	private 	Map<String, Channel> channels;

	public ChatChannels() {
		channels = new HashMap<>();
		Channel main = new Channel();
		channels.put(main.getName(), main);
	}

	public void add(ChatSession newSession) throws IOException {
		Channel main = channels.get("main");
		main.add(newSession);
	}

	public void add(ChatSession newSession, String toChannel) throws IOException {
		Channel channel = channels.get(toChannel);
		if (null != channel) {
			channel.add(newSession);
		} else {
			Channel newChannel = new Channel(toChannel);
			newChannel.add(newSession);
			channels.put(toChannel, newChannel);
		}
	}

	public void remove(ChatSession session) throws IOException {
		Channel channel = session.getChannel();
		if (null != channel) {
			channel.remove(session);
		}
	}

	public void removeAndClose(ChatSession session) throws IOException {
		Channel channel = session.getChannel();
		if (null != channel) {
			channel.remove(session);
			session.close();
		}
	}

	public void move(ChatSession session, String toChannel) throws IOException {
		remove(session);
		add(session, toChannel);
	}
}
