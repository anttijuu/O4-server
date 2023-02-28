package oy.tol.chatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.OffsetDateTime;

import org.json.JSONException;
import org.json.JSONObject;

import oy.tol.chatserver.messages.ChatMessage;
import oy.tol.chatserver.messages.ErrorMessage;
import oy.tol.chatserver.messages.Message;
import oy.tol.chatserver.messages.StatusMessage;

public class ChatServerSession extends Thread {

	enum State {
		UNCONNECTED,
		CONNECTED,
		AUTHENTICATED
	}

	private Socket socket;
	private User user;
	private State state = State.UNCONNECTED;
	private DataOutputStream out = null;
	private int sessionID;
	private boolean running = true;

	private Channel atChannel;

	public ChatServerSession(Socket socket, int sessionID) throws IOException {
		this.socket = socket;
		this.sessionID = sessionID;
		state = socket.isConnected() ? State.CONNECTED : State.UNCONNECTED;
		out = new DataOutputStream(socket.getOutputStream());
		new Thread(this).start();
	}

	public void setChannel(Channel channel) throws IOException {
		atChannel = channel;
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

	public void close() throws IOException {
		if (atChannel != null) {
			atChannel.remove(this);
		}
		state = State.UNCONNECTED;
		running = false;
		user = null;
		socket.close();
		socket = null;
	}

	@Override
	public void run() {
		while (running && null != socket) {
			switch (state) {
				case UNCONNECTED:
					break;

				case CONNECTED:
					read();
					break;

				case AUTHENTICATED:
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
			out.writeBytes(message + "\n");
		}
	}

	private void read() {

		// Read data from socket
		System.out.println("Receiving data...");
		try {
			DataInputStream inStream = new DataInputStream(socket.getInputStream());
			String data = "";
			byte[] sizeBytes = new byte[2];
			sizeBytes[0] = inStream.readByte();
			sizeBytes[1] = inStream.readByte();
			ByteBuffer byteBuffer = ByteBuffer.wrap(sizeBytes, 0, 2);
			int bytesToRead = byteBuffer.getShort();
			System.out.println(sessionID + ": Read " + bytesToRead + " bytes");

			if (bytesToRead > 0) {
				int bytesRead = 0;
				byte[] messageBytes = new byte[bytesToRead];
				byteBuffer = ByteBuffer.wrap(messageBytes, 0, bytesToRead);
				while (bytesToRead > bytesRead) {
					byteBuffer.put(inStream.readByte());
					bytesRead++;
				}
				if (bytesRead == bytesToRead) {
					data = new String(messageBytes, 0, bytesRead, StandardCharsets.UTF_8);
					handleMessage(data);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (EOFException e) {
			System.out.println("ChatSession: EOFException");
			try {
				close();
			} catch (Exception f) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			System.out.println("ChatSession: IOException");
			try {
				close();
			} catch (Exception f) {
				e.printStackTrace();
			}
		}
	}

	private void handleMessage(String data) throws SQLException, IOException, JSONException {

		try {
			JSONObject jsonObject = new JSONObject(data);
			int msgType = jsonObject.getInt("type");

			switch (msgType) {
				case Message.REGISTER_MESSAGE:
					handleRegistrationMessage(jsonObject);
					break;

				case Message.LOGIN_MESSAGE:
					handleLoginMessage(jsonObject);
					break;

				case Message.CHAT_MESSAGE:
					handleChatMessage(jsonObject);
					break;

				case Message.JOIN_CHANNEL:
					handleJoinChannelMessage(jsonObject);
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

	private void handleJoinChannelMessage(JSONObject jsonObject) throws IOException {
		String channel = jsonObject.getString("channel");
		ChatChannels.getInstance().move(this, channel);
	}

	private void handleRegistrationMessage(JSONObject jsonObject) throws SQLException, IOException {
		String userName = jsonObject.getString("userName");
		String passWord = jsonObject.getString("password");
		String eMail = jsonObject.getString("email");
		User user = new User(userName, passWord, eMail);
		if (ChatDatabase.getInstance().addUser(user)) {
			// Success, no need to tell anything to client.
			this.user = user;
			this.state = State.AUTHENTICATED;
			if (null != atChannel) {
				StatusMessage status = new StatusMessage("User " + userName + " joined the server as a new user!");
				atChannel.relayMessage(this, status);
			}
		} else {
			// Failure
			ErrorMessage errorMsg = new ErrorMessage("Cannot register this user");
			write(errorMsg);
		}
	}

	private void handleLoginMessage(JSONObject jsonObject) throws IOException {
		String userName = jsonObject.getString("userName");
		String passWord = jsonObject.getString("password");
		if (ChatDatabase.getInstance().isRegisteredUser(userName, passWord)) {
			this.user = new User(userName, passWord, "");
			this.state = State.AUTHENTICATED;
			StatusMessage status = new StatusMessage("User " + userName + " just logged in!");
			atChannel.relayMessage(this, status);
		} else {
			ErrorMessage errorMsg = new ErrorMessage("Cannot login this user");
			write(errorMsg);
		}
	}

	private void handleChatMessage(JSONObject jsonObject) throws SQLException, IOException {
		if (state == State.AUTHENTICATED) {
			String userName = jsonObject.getString("user");
			String msg = jsonObject.getString("message");
			String dateStr = jsonObject.getString("sent");
			OffsetDateTime odt = OffsetDateTime.parse(dateStr);
			ChatMessage newMessage = new ChatMessage(odt.toLocalDateTime(), userName, msg);
			ChatDatabase.getInstance().insertMessage(userName, newMessage);
			if (null != atChannel) {
				atChannel.relayMessage(this, newMessage);
			}
		} else {
			ErrorMessage errorMsg = new ErrorMessage("Not authenticate, register or login first");
			write(errorMsg);
		}
	}
}
