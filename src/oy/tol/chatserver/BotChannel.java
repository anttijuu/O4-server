package oy.tol.chatserver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import oy.tol.chat.ChatMessage;
import oy.tol.chat.Message;

public class BotChannel extends Channel {

	private boolean botActive = false;
	private Timer timer;
	private static final int MIN_TIMER_GAP_MS = 1500;
	private static final int MAX_TIMER_GAP_MS = 5000;
	private List<String> messageStrings;
	private int messageIndex = 0;

	private class PostMessageTask extends TimerTask {
		@Override
		public void run() {
			Message message = createMessage();
			if (null != message) {
				relayMessage(null, message);
			}
			int nextGap = ThreadLocalRandom.current().nextInt(MAX_TIMER_GAP_MS) + MIN_TIMER_GAP_MS;
			timer.schedule(new PostMessageTask(), nextGap);
		}
	}

	public BotChannel(String name) {
		super(name, true);
		setTopic("Bot channel");
	}

	@Override
	public synchronized void add(ChatServerSession session) {
		System.out.println("BOT: a client joined the bot channel");
		super.add(session);
		try {
			activateBot();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized void closeAllSessions(String message) {
		super.closeAllSessions(message);
		deactivateBot();
	}

	@Override
	public synchronized void remove(ChatServerSession session) {
		System.out.println("BOT: a client departed the bot channel");
		super.remove(session);
		if (!hasSessions()) {
			deactivateBot();
		}
	}

	private void activateBot() throws IOException {
		if (botActive) {
			return;
		}
		System.out.println("BOT: Activating the bot");
		messageStrings = new ArrayList<>();
		readMessagesFromFile();
		botActive = true;
		messageIndex = 0;
		timer = new Timer();
		timer.schedule(new PostMessageTask(), MAX_TIMER_GAP_MS);
	}

	private Message createMessage() {
		Message msg = null;
		if (messageIndex >= messageStrings.size()) {
			messageIndex = 0;
		}
		String nextItem = messageStrings.get(messageIndex);
		if (nextItem.startsWith("$")) {
			setTopic(nextItem.substring(1));
		} else {
			int firstColonIndex = nextItem.indexOf(':');
			if (firstColonIndex > 0) {
				String nick = nextItem.substring(0, firstColonIndex);
				String msgText = nextItem.substring(firstColonIndex + 1);
				msg = new ChatMessage(nick, msgText);
			}
		}
		messageIndex++;
		return msg;
	}

	private void deactivateBot() {
		if (!botActive) {
			return;
		}
		System.out.println("BOT: Deactivating the bot");
		botActive = false;
		timer.cancel();
		messageStrings = null;
		timer = null;
	}

	private void readMessagesFromFile() throws IOException {
		String fileName = getName() + ".txt";
		BufferedReader messageReader = new BufferedReader(new FileReader(fileName, StandardCharsets.UTF_8));
		String line;
		while ((line = messageReader.readLine()) != null) {
			if (!line.startsWith("#") && line.length() > 0) {
				messageStrings.add(line.trim());
			}
		}
		messageReader.close();
		System.out.println("Bot has " + messageStrings.size() + " messages to use");
	}

}
