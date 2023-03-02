package oy.tol.chatserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatChannels {

	private 	Map<String, Channel> channels;

	private static ChatChannels instance = null;
	
	public static synchronized ChatChannels getInstance() {
		if (null == instance) {
			return new ChatChannels();
		}
		return instance;
	}

	private ChatChannels() {
		channels = new HashMap<>();
		Channel main = new Channel();
		channels.put(main.getName(), main);
	}

	public synchronized void add(ChatServerSession newSession) throws IOException {
		Channel main = channels.get("main");
		main.add(newSession);
	}

	public synchronized void add(ChatServerSession newSession, String toChannel) throws IOException {
		Channel channel = channels.get(toChannel);
		if (null != channel) {
			channel.add(newSession);
		} else {
			Channel newChannel = new Channel(toChannel);
			newChannel.add(newSession);
			channels.put(toChannel, newChannel);
		}
	}

	public synchronized void remove(ChatServerSession session) throws IOException {
		Channel channel = session.getChannel();
		if (null != channel) {
			channel.remove(session);
		}
	}

	public synchronized void removeAndClose(ChatServerSession session) throws IOException {
		Channel channel = session.getChannel();
		if (null != channel) {
			channel.remove(session);
			session.close();
		}
	}

	public synchronized void move(ChatServerSession session, String toChannel) throws IOException {
		remove(session);
		add(session, toChannel);
	}

	public synchronized void closeAll(String message) {
		channels.forEach( (name, channel) -> {
			channel.closeAllSessions(message);
		});
	}
}
