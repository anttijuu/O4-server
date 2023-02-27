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

import org.json.JSONObject;

public class ChatSession extends Thread {
	
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

	public ChatSession(Socket socket, int sessionID) throws IOException {
		this.socket = socket;
		this.sessionID = sessionID;
		state = socket.isConnected() ? State.CONNECTED : State.UNCONNECTED;
		out = new DataOutputStream(socket.getOutputStream());
		new Thread(this).start();
	}

	public void setChannel(Channel channel) throws IOException {
		if (channel != null && state == State.CONNECTED) {
			if (channel.getName().length() > 0) {
				write("Joined the channel " + channel.getName() + " with " + channel.sessionCount() + " chatters on topic:\n  " + channel.getTopic());
			} else {
				write("Leaving the channel " + channel.getName());
			}	
		}
		atChannel = channel;
	}

	public Channel getChannel() {
		return atChannel;
	}

	public void close() throws IOException {
		if (atChannel != null) {
			atChannel.remove(this);
		}
		state = State.UNCONNECTED;
		running = false;
		socket.close();
	}

	@Override
	public void run() {
		while (running) {
			switch (state) {
				case UNCONNECTED:
					break;
	
				case CONNECTED:
					read();
					break;
	
				case AUTHENTICATED:
					break;
	
				default:
					break;
			}	
		}
	}

	public void write(String message) throws IOException {
		out.writeBytes(message + "\n");
	}

	private void read() {

		while (socket != null && socket.isConnected()) {
			// Read data from socket
			System.out.println("Receiving data...");
			try {
				DataInputStream inStream = new DataInputStream(socket.getInputStream());
				String data = "";
				byte [] sizeBytes = new byte[2];
				sizeBytes[0] = inStream.readByte();
				sizeBytes[1] = inStream.readByte();
				ByteBuffer byteBuffer = ByteBuffer.wrap(sizeBytes, 0, 2);
				int bytesToRead = byteBuffer.getShort();
				System.out.println(sessionID + ": Read " + bytesToRead + " bytes");

				if (bytesToRead > 0) {
					int bytesRead = 0;
					byte [] messageBytes = new byte[bytesToRead];
					byteBuffer = ByteBuffer.wrap(messageBytes, 0, bytesToRead);
					while (bytesToRead > bytesRead) {
						byteBuffer.put(inStream.readByte());
						bytesRead++;
					}
					if (bytesRead == bytesToRead) {
						data = new String(messageBytes, 0, bytesRead, StandardCharsets.UTF_8);
						
						JSONObject jsonObject = new JSONObject(data);
						String userName = jsonObject.getString("user");
						String msg = jsonObject.getString("message");
						String dateStr = jsonObject.getString("sent");
						OffsetDateTime odt = OffsetDateTime.parse(dateStr);
						ChatMessage newMessage = new ChatMessage(odt.toLocalDateTime(), userName, msg);
						ChatDatabase.getInstance().insertMessage(userName, newMessage);						
						System.out.println(sessionID + ": Data received: " + data);
					}
				}
			} catch (EOFException e) {
				System.out.println("ChatSession: EOFException");
				try {
					close();
				} catch (Exception f) {

				}
			} catch (IOException e) {
				System.out.println("ChatSession: IOException");
				try {
					close();
				} catch (Exception f) {

				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} // while
	}
}
