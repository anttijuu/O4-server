package oy.tol.chatserver;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.util.Properties;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class ChatSocketServer implements Runnable {

	private boolean running = true;
	private int serverPort = 10000;
	private boolean useSSL = false;
	private String certificateFile = "keystore.jks";
	private String certificatePassword = "";
	
	private static ChatSocketServer server;
	private ServerSocket serverSocket;
	private ChatChannels channels;

	public static void main(String[] args) {
		try {
			System.out.println("Launching ChatServer...");
			if (args.length != 2) {
				System.out.println("Usage java -jar jar-file.jar config.properties certpassword");
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ChatSocketServer(String [] args) throws FileNotFoundException, IOException {
		log("Reading configuration...");
		readConfiguration(args[0]);
		certificatePassword = args[1];
	}

	@Override
	public void run() {
		try {
			log("Initializing ChatServer...");
			if (useSSL) {
				SSLContext sslContext = chatServerSSLContext();
				ServerSocketFactory socketFactory = sslContext.getServerSocketFactory();
				serverSocket = socketFactory.createServerSocket(serverPort);	
			} else {
				ServerSocketFactory socketFactory = ServerSocketFactory.getDefault();
				serverSocket = socketFactory.createServerSocket(serverPort);
			}
			channels = ChatChannels.getInstance();
			while (running) {
				int sessionCount = 0;
				Socket clientSocket = serverSocket.accept();
				ChatServerSession newSession = new ChatServerSession(clientSocket, ++sessionCount);
				channels.add(newSession);
			}
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException
				| UnrecoverableKeyException | KeyManagementException e) {
			log("Something wrong with the certificate!", ANSI_RED);
			e.printStackTrace();
		} finally {
			channels.closeAll("Server is shutting down, bye all!");
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
	private SSLContext chatServerSSLContext() throws KeyStoreException, NoSuchAlgorithmException,
			CertificateException, IOException, UnrecoverableKeyException, KeyManagementException {
		char[] passphrase = certificatePassword.toCharArray();
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(new FileInputStream(certificateFile), passphrase);

		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks, passphrase);

		TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
		tmf.init(ks);

		SSLContext ssl = SSLContext.getInstance("TLS");
		ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
		return ssl;
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
		useSSL = config.getProperty("usessl", "false").equals("true");
		certificateFile = config.getProperty("certfile");
		istream.close();
		log("Server port: " + serverPort, ANSI_YELLOW);
		log("Using SSL: " + (useSSL ? "true" : "false"), ANSI_YELLOW);
		log("Certificate file: " + certificateFile, ANSI_YELLOW);
	}
}
