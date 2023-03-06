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
			ChatMessage randomMessage = createRandomMessage();
			if (null != randomMessage) {
				System.out.println("BOT: relaying message from " + randomMessage.getNick());
				relayMessage(null, randomMessage);
			} else {
				System.out.println("BOT: Failed to create a message");
			}
			int nextGap = ThreadLocalRandom.current().nextInt(MAX_TIMER_GAP_MS) + MIN_TIMER_GAP_MS;
			timer.schedule(new PostMessageTask(), nextGap);		}
	}

	public BotChannel(String name) {
		super(name, true);
	}

	@Override
	public void add(ChatServerSession session) {
		System.out.println("BOT: a client joined the bot channel");
		super.add(session);
		try {
			activateBot();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public void closeAllSessions(String message) {
		super.closeAllSessions(message);
		deactivateBot();
	}

	@Override
	public void remove(ChatServerSession session) {
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
		readRandomMessages();
		botActive = true;
		timer = new Timer();
		timer.schedule(new PostMessageTask(), MAX_TIMER_GAP_MS);
	}

	private ChatMessage createRandomMessage() {
		if (messageIndex >= messageStrings.size()) {
			messageIndex = 0;
		}
		String [] elements = messageStrings.get(messageIndex).split(":");
		messageIndex++;
		if (elements.length == 2) {
			return new ChatMessage(elements[0], elements[1]);
		}
		return null;
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

	private void readRandomMessages() throws IOException {
		String fileName = getName() + ".txt";
		BufferedReader messageReader = new BufferedReader(new FileReader(fileName, StandardCharsets.UTF_8));
		String line;
		while ((line = messageReader.readLine()) != null && line.length() > 0) {
			messageStrings.add(line.trim());
		}
		messageReader.close();
		System.out.println("Bot has " + messageStrings.size() + " messages to use");
	}

}
