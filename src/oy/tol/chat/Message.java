package oy.tol.chatserver.messages;

public abstract class Message {
	
	private int type;

	public static final int REGISTER_MESSAGE = 1;
	public static final int LOGIN_MESSAGE = 2;
	public static final int CHAT_MESSAGE = 3;
	public static final int MESSAGE_ARRAY = 4;
	public static final int JOIN_CHANNEL = 5;
	public static final int STATUS_MESSAGE = 98;
	public static final int ERROR_MESSAGE = 99;

	protected Message(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public abstract String toJSON();

}
