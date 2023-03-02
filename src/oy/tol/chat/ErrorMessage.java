package oy.tol.chat;

import org.json.JSONObject;

public class ErrorMessage extends Message {

	public ErrorMessage(String message) {
		super(Message.ERROR_MESSAGE);
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
