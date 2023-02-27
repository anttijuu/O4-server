package oy.tol.chatserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ChatSession {
	
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

	public ChatSession(Socket socket, int sessionID) {
		this.socket = socket;
		this.sessionID = sessionID;
		state = socket.isConnected() ? State.CONNECTED : State.UNCONNECTED;
	}

	public void run() {
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

	private void read() {
		DataInputStream inStream = new DataInputStream(socket.getInputStream());
		byte [] messageByte = new byte[4096];
		String data = "";

		while (socket != null && socket.isConnected()) {
			// Read data from socket
			System.out.println("Receiving data...");
			try {
				messageByte[0] = inStream.readByte();
				messageByte[1] = inStream.readByte();
				ByteBuffer byteBuffer = ByteBuffer.wrap(messageByte, 0, 2);
				int bytesToRead = byteBuffer.getShort();
				System.out.println(sessionID + ": Read " + bytesToRead + " bytes");

				if (bytesToRead > 0) {
					int bytesRead = 0;
					byteBuffer.clear();
					while (bytesToRead > bytesRead) {
						byteBuffer.put(inStream.readByte());
						bytesRead++;
					}
					if (bytesRead == bytesToRead) {
						data = new String(messageByte, 0, bytesRead, StandardCharsets.UTF_16);
						System.out.println(sessionId + ": Data received: " + data);
						JSONObject root;

						root = (JSONObject) new JSONParser().parse(data);

						String command = root.get("command").toString(); // id of the operation, for async operations.
						dir = (String) root.get("dir"); // request/response
						Boolean recursive = (Boolean)root.get("recursive");
						JSONArray words = (JSONArray)root.get("keywords");

						if (command != null && command.equalsIgnoreCase("watch")) {
							if (null != words) {
								@SuppressWarnings("unchecked")
								Iterator<String> iterator = words.iterator();
								while (iterator.hasNext()) {
									keywords.add(iterator.next());
								}
							}
							if (dir != null) {
								System.out.println(sessionId + ": Adding dir " + dir + " under watch");
								watcher.addWatchedDirectory(FileSystems.getDefault().getPath(dir), recursive, this);
							}
						}
					}
				}

			} catch (ParseException e) {
				e.printStackTrace();
			} catch (NoSuchFileException nsf) {
				JSONObject toSend = createResponse("response", "Path does not exist", dir);
				sendResponse(toSend.toString());
			} 

		} // while
	} catch (EOFException e1) {
		// Remove from Server, since connection was broken.
		System.out.println(sessionId + ": Session connection with client closed");
		manager.removeSession(this);
	} catch (IOException e1) {
		e1.printStackTrace();
		System.out.println(sessionId + ": Session: IOException in socket connection with client");
		// Remove from Server, since connection was broken.
		manager.removeSession(this);
	}
	}

}
