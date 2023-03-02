package oy.tol.chatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;

import oy.tol.chat.ErrorMessage;
import oy.tol.chat.JoinMessage;
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
	private DataOutputStream out;
	private DataInputStream inStream;
	private int sessionID;
	private boolean running = true;

	private Channel atChannel;

	public ChatServerSession(Socket socket, int sessionID) throws IOException {
		this.socket = socket;
		this.sessionID = sessionID;
		state = socket.isConnected() ? State.CONNECTED : State.UNCONNECTED;
		out = new DataOutputStream(socket.getOutputStream());
		inStream = new DataInputStream(socket.getInputStream());
		new Thread(this).start();
	}

	public void setChannel(Channel channel) throws IOException {
		atChannel = channel;
		if (atChannel != null) {
			StatusMessage channelChanged = new StatusMessage("Switched to channel " + atChannel.getName());
			write(channelChanged);
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
			if (atChannel != null) {
				atChannel.remove(this);
			}
			state = State.UNCONNECTED;
			running = false;
			user = null;
			socket.close();
			socket = null;	
		} catch (IOException e) {
			System.out.println("Closing a session failed: " + e.getLocalizedMessage());
		}
	}

	@Override
	public void run() {
		while (running && null != socket) {
			switch (state) {
				case UNCONNECTED:				
					close();
					break;

				case CONNECTED:
					read();
					break;

				default:
					break;
			}
		}
	}

	public void write(Message msg) throws IOException {
		write(msg.toJSON());
	}

	private void write(String message) throws IOException {
		if (state == State.CONNECTED) {
			// System.out.println("Writing to client: " + message);
			byte [] msgBytes = message.getBytes(StandardCharsets.UTF_8);
			// System.out.println(" " + Arrays.toString(msgBytes));
			byte[] allBytes = new byte[msgBytes.length + 2];
			ByteBuffer byteBuffer = ByteBuffer.wrap(allBytes, 0, allBytes.length);
			short msgLen = (short)msgBytes.length;
			// System.out.println("Byte count: " + msgLen);
			byteBuffer = byteBuffer.putShort(msgLen);
			byteBuffer = byteBuffer.put(msgBytes);
			// System.out.println(" " + Arrays.toString(allBytes));
			out.write(allBytes);
		}
	}

	private void read() {
		// Read data from socket
		// System.out.println("Receiving data...");
		try {
			String data = "";
			byte[] sizeBytes = new byte[2];
			sizeBytes[0] = inStream.readByte();
			sizeBytes[1] = inStream.readByte();
			ByteBuffer byteBuffer = ByteBuffer.wrap(sizeBytes, 0, 2);
			int bytesToRead = byteBuffer.getShort();
			// System.out.println(sessionID + ": Reading " + bytesToRead + " bytes...");

			if (bytesToRead > 0) {
				int bytesRead = 0;
				byte [] messageBytes = new byte[bytesToRead];
				int read = inStream.read(messageBytes, 0, bytesToRead);
				// System.out.println("Actually read: " + read + " bytes");
				data = new String(messageBytes, 0, bytesToRead, StandardCharsets.UTF_8);
				// System.out.println("Received: " + data);
				handleMessage(data);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (EOFException e) {
			System.out.println("ChatSession: EOFException");
			close();
		} catch (IOException e) {
			System.out.println("ChatSession: IOException");
			close();
		}
	}

	private void handleMessage(String data) throws SQLException, IOException, JSONException {
		try {
			JSONObject jsonObject = new JSONObject(data);
			Message msg = MessageFactory.fromJSON(jsonObject);
			int msgType = msg.getType();
			switch (msgType) {
				case Message.CHAT_MESSAGE:
					handleChatMessage(msg);
					break;

				case Message.JOIN_CHANNEL:
					handleJoinChannelMessage((JoinMessage)msg);
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

	private void handleJoinChannelMessage(JoinMessage msg) throws IOException {
		String channel = msg.getChannel();
		String topic = msg.getTopic();
		if (topic == null || topic.length() == 0) {
			ChatChannels.getInstance().move(this, channel);
		} else {
			ChatChannels.getInstance().move(this, channel, topic);
		}
	}

	private void handleChatMessage(Message msg) throws IOException {
		if (null != atChannel) {
			atChannel.relayMessage(this, msg);
		} else {
			System.out.println("Channel for session is null so cannot relay messages to other sessions");
		}
	}
}
