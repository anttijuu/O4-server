package oy.tol.chatserver;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import oy.tol.chat.ChangeTopicMessage;
import oy.tol.chat.ErrorMessage;
import oy.tol.chat.JoinMessage;
import oy.tol.chat.ListChannelsMessage;
import oy.tol.chat.Message;
import oy.tol.chat.MessageFactory;
import oy.tol.chat.StatusMessage;

public class ChatServerSession implements Runnable {

	enum State {
		UNCONNECTED,
		CONNECTED
	}

	private Socket socket;
	private User user;
	private State state = State.UNCONNECTED;
	private PrintWriter out;
	private BufferedReader in;
	private int sessionID;
	private boolean running = true;

	private Channel atChannel;

	public ChatServerSession(Socket socket, int sessionID) throws IOException {
		this.socket = socket;
		this.sessionID = sessionID;
		state = socket.isConnected() ? State.CONNECTED : State.UNCONNECTED;
		out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
		new Thread(this).start();
	}

	public void setChannel(Channel channel) {
		if (null != atChannel) {
			StatusMessage leaving = new StatusMessage("You left the channel " + atChannel.getName());
			write(leaving);
		}
		atChannel = channel;
		if (atChannel != null) {
			StatusMessage arriving = new StatusMessage("You joined the channel " + atChannel.getName());
			write(arriving);
		}
	}

	public Channel getChannel() {
		return atChannel;
	}

	public String userName() {
		if (null != user) {
			return user.getName();
		}
		return "";
	}

	public void close() {
		try {
			System.out.println("Closing a server side session with client");
			running = false;
			if (atChannel != null) {
				atChannel.remove(this);
			}
			state = State.UNCONNECTED;
			user = null;
			in.close();
			out.close();
			socket.close();
		} catch (IOException e) {
			System.out.println("Closing a session failed: " + e.getLocalizedMessage());
		} finally {
			socket = null;
		}
	}

	@Override
	public void run() {
		System.out.println("ServerSession thread started");
		while (running && null != socket) {
			// Read data from socket
			System.out.println("Receiving data...");
			try {
				String data;
				while ((data = in.readLine()) != null) {
					handleMessage(data);
				}
			} catch (EOFException e) {
				System.out.println("ChatSession: EOFException");
			} catch (IOException e) {
				System.out.println("ChatSession: IOException");
			} finally {
				close();
				System.out.println("In session finally...");
			}
		}
		System.out.println("ServerSession run loop finished");
	}

	public void write(Message msg) {
		write(msg.toJSON());
	}

	private void write(String message) {
		if (state == State.CONNECTED) {
			System.out.println("DEBUG OUT: " + message);
			out.write(message + "\n");
			out.flush();
		}
	}

	private void handleMessage(String data) throws IOException {
		System.out.println("DEBUG IN: " + data);
		try {
			JSONObject jsonObject = new JSONObject(data);
			Message msg = MessageFactory.fromJSON(jsonObject);
			if (null == msg) {
				ErrorMessage error = new ErrorMessage("Unknown message type from client");
				write(error);
				return;
			}
			int msgType = msg.getType();
			switch (msgType) {
				case Message.CHAT_MESSAGE:
					handleChatMessage(msg);
					break;

				case Message.JOIN_CHANNEL:
					handleJoinChannelMessage((JoinMessage) msg);
					break;

				case Message.LIST_CHANNELS: 
					handleListChannelsMessage((ListChannelsMessage)msg);
					break;

				case Message.CHANGE_TOPIC:
					handleChangeChannelTopicMessage((ChangeTopicMessage) msg);
					break;

				default: // Clients cannot send other message types.
					ErrorMessage errorMsg = new ErrorMessage("Invalid message from client, not handled");
					write(errorMsg);
					break;
			}
		} catch (JSONException e) {
			e.printStackTrace();
			ErrorMessage errorMsg = new ErrorMessage("Invalid JSON message from client");
			write(errorMsg);
		}
	}

	private void handleListChannelsMessage(ListChannelsMessage msg) {
		List<String> channelNames = ChatChannels.getInstance().listChannels();
		ListChannelsMessage listMessage = new ListChannelsMessage();
		for (String name : channelNames) {
			listMessage.addChannel(name);
		}
		write(listMessage);
	}

	private void handleChangeChannelTopicMessage(ChangeTopicMessage msg) {
		String newTopic = msg.getTopic();
		System.out.println("Received topic change msg to " + newTopic);
		ChatChannels.getInstance().changeTopic(this, newTopic);
	}

	private void handleJoinChannelMessage(JoinMessage msg) {
		String channel = msg.getChannel();
		System.out.println("Received channel change msg to " + channel);
		ChatChannels.getInstance().move(this, channel);
	}

	private void handleChatMessage(Message msg) {
		if (null != atChannel) {
			atChannel.relayMessage(this, msg);
		} else {
			System.out.println("Channel for session is null so cannot relay messages to other sessions");
		}
	}
}
