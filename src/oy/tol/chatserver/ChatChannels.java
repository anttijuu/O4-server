package oy.tol.chatserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oy.tol.chat.ChatMessage;

public class ChatChannels {

	private 	Map<String, Channel> channels;

	private static ChatChannels instance = null;
	
	public static synchronized ChatChannels getInstance() {
		if (null == instance) {
			instance = new ChatChannels();
		}
		return instance;
	}

	private ChatChannels() {
		channels = new HashMap<>();
		Channel main = new Channel("main", true);
		main.setTopic("Everything under the sun and moon");
		channels.put(main.getName(), main);
	}

	public synchronized void add(ChatServerSession newSession) {
		Channel main = channels.get("main");
		main.add(newSession);
		newSession.setChannel(main);
		System.out.println(this);
	}

	public synchronized void add(ChatServerSession newSession, String toChannel) {
		Channel channel = channels.get(toChannel);
		if (null != channel) {
			channel.add(newSession);
			newSession.setChannel(channel);
		} else {
			Channel newChannel = new Channel(toChannel);
			newChannel.add(newSession);
			newSession.setChannel(newChannel);
			channels.put(toChannel, newChannel);
		}
		System.out.println(this);
	}

	public synchronized void add(ChatServerSession newSession, String toChannel, String topic) {
		add(newSession, toChannel);
		Channel channel = channels.get(toChannel);
		channel.setTopic(topic);
		System.out.println(this);
	}


	public synchronized void remove(ChatServerSession session) {
		Channel channel = session.getChannel();
		if (null != channel) {
			channel.remove(session);
			session.setChannel(null);
			if (!channel.hasSessions() && !channel.isPermanent()) {
				channels.remove(channel.getName());
				System.out.println("Last user left from channel so it was closed");
			}
		}
	}

	public synchronized void removeAndClose(ChatServerSession session) {
		Channel channel = session.getChannel();
		if (null != channel) {
			channel.remove(session);
			session.setChannel(null);
			session.close();
			if (!channel.hasSessions() && !channel.isPermanent()) {
				channels.remove(channel.getName());
				System.out.println("Last user left from channel so it was closed");
			}
		}
		System.out.println(this);
	}

	public synchronized void move(ChatServerSession session, String toChannel) {
		if (session.getChannel() != null) {
			if (session.getChannel().getName().equals(toChannel)) {
				return;
			}
			remove(session);
		}
		add(session, toChannel);
		System.out.println(this);
	}

	public synchronized void changeTopic(ChatServerSession session, String topic) {
		Channel channel = session.getChannel();
		if (null != channel) {
			channel.setTopic(topic);
		}
	}

	public synchronized List<String> listChannels() {
		List<String> channelNames = new ArrayList<>();
		channels.forEach( (name, channel) -> {
			channelNames.add(name + " (" + channel.sessionCount() + ")");
		});
		return channelNames;
	}

	public synchronized void relayPrivateMessage(ChatServerSession session, ChatMessage message) {
		if (!message.isDirectMessage()) {
			return;
		}
		channels.forEach( (name, channel) -> {
			if (channel.relayPrivateMessage(session, message)) {
				return;
			}
		});
	}

	public synchronized void closeAll(String message) {
		channels.forEach( (name, channel) -> {
			channel.closeAllSessions(message);
		});
	}

	@Override 
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("---------------------\nServer has ");
		builder.append(channels.size());
		builder.append(" channels currently:\n");
		channels.forEach( (name, channel) -> {
			builder.append(channel.toString());
			builder.append("\n");
		});
		builder.append("---------------------\n");
		return builder.toString();
	}

	public void createBotChannel(String botChannelName) {
		Channel channel = channels.get(botChannelName);
		if (null == channel) {
			Channel newChannel = new BotChannel(botChannelName);
			channels.put(botChannelName, newChannel);
		}
	}
}
