package oy.tol.chat;

public abstract class Message {
	
	private int type;
	private String message;

	public static final int ERROR_MESSAGE = -1;
	public static final int STATUS_MESSAGE = 0;
	public static final int CHAT_MESSAGE = 1;
	public static final int JOIN_CHANNEL = 2;

	protected Message(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public String getMessage() {
		 return message;
	}

	public void setMessage(String message) {
		 this.message = message;
	}

	public abstract String toJSON();
}
