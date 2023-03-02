package oy.tol.chatserver.messages;

import org.json.JSONObject;

public class StatusMessage extends Message {

	private String message;

	public StatusMessage(String message) {
		super(Message.STATUS_MESSAGE);
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
