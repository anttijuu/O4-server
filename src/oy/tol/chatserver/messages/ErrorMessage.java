package oy.tol.chatserver.messages;

import org.json.JSONObject;

public class ErrorMessage extends Message {

	private String message;

	public ErrorMessage(String message) {
		super(Message.ERROR_MESSAGE);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public String toJSON() {
		JSONObject object = new JSONObject();
		object.put("type", getType());
		object.put("message", message);
		return object.toString();
	}
	
}
