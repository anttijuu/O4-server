package oy.tol.chat;

public abstract class Message {
	
	private int type;

	public static final int ERROR_MESSAGE = -1;
	public static final int STATUS_MESSAGE = 0;
	public static final int CHAT_MESSAGE = 1;
	public static final int JOIN_CHANNEL = 2;
	public static final int CHANGE_TOPIC = 3;
	public static final int LIST_CHANNELS = 4;

	protected Message(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}
	
	public abstract String toJSON();
}
