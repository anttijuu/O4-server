package oy.tol.chat;

import org.json.JSONObject;

public class StatusMessage extends Message {

	public StatusMessage(String message) {
		super(Message.STATUS_MESSAGE);
		setMessage(message);
	}

	@Override
	public String toJSON() {
		JSONObject object = new JSONObject();
		object.put("type", getType());
		object.put("message", getMessage());
		return object.toString();
	}
	
}
