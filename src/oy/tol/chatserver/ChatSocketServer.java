package oy.tol.chatserver;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Properties;

import javax.net.ServerSocketFactory;

public class ChatSocketServer implements Runnable {

	private boolean running = true;
	private int serverPort = 10000;
	
	private static ChatSocketServer server;
	private ServerSocket serverSocket;
	private ChatChannels channels;
	private String botChannelName = "";

	public static void main(String[] args) {
		try {
			System.out.println("Launching ChatServer...");
			if (args.length != 1) {
				System.out.println("Usage java -jar jar-file.jar chatserver.properties");
				return;
			}
			server = new ChatSocketServer(args);
			new Thread(server).start();
			Console console = System.console();
			while (server.running) {
				String input = console.readLine();
				if (input.equalsIgnoreCase("/quit")) {
					server.close();
				} else if (input.equalsIgnoreCase("/status")) {
					System.out.println(server.channels);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private ChatSocketServer(String [] args) throws FileNotFoundException, IOException {
		log("Reading configuration...");
		readConfiguration(args[0]);
	}

	@Override
	public void run() {
		try {
			log("Initializing ChatServer...");
			ServerSocketFactory socketFactory = ServerSocketFactory.getDefault();
			serverSocket = socketFactory.createServerSocket(serverPort);
			channels = ChatChannels.getInstance();
			if (botChannelName.length() > 0) {
				channels.createBotChannel(botChannelName);
			}
			log("Starting to await client connections");
			while (running) {
				try {
					int sessionCount = 0;
					Socket clientSocket = serverSocket.accept();
					ChatServerSession newSession = new ChatServerSession(clientSocket, ++sessionCount);
					channels.add(newSession);						
				} catch (IOException e) {
					System.out.println("IOException when creating a ChatServerSession " + e.getLocalizedMessage());
				}
			}
		} catch (Exception e) {
			log("Something wrong !" + e.getLocalizedMessage(), ANSI_RED);
			e.printStackTrace();
		} finally {
			channels.closeAll("Server is finally shutting down, bye all!");
		}
		log("Server finished, bye!");
	}

	public void close() {
		running = false;
		channels.closeAll("Server is shutting down, bye all!");
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		serverSocket = null;
	}

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	public void log(String message) {
		System.out.println(ANSI_GREEN + LocalDateTime.now() + ANSI_RESET + " " + message);
	}

	public void log(String message, String color) {
		System.out.println(color + LocalDateTime.now() + ANSI_RESET + " " + message);
	}

	private void readConfiguration(String configFileName) throws FileNotFoundException, IOException {
		log("Using configuration: " + configFileName, ANSI_YELLOW);
		File configFile = new File(configFileName);
		Properties config = new Properties();
		FileInputStream istream;
		istream = new FileInputStream(configFile);
		config.load(istream);
		serverPort = Integer.valueOf(config.getProperty("port", "10000"));
		istream.close();
		log("Server port: " + serverPort, ANSI_YELLOW);
		botChannelName = config.getProperty("botchannel", "");
		File botFile = new File(botChannelName + ".txt");
		if (!botFile.isFile()) {
			log("Bot file does not exist", ANSI_RED);
			botChannelName = "";
		} else {
			log("Using bot channel " + botChannelName, ANSI_GREEN);
		}
	}
}
