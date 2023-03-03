package oy.tol.chat;

import org.json.JSONObject;

public class ErrorMessage extends Message {

	private String error; 

	public ErrorMessage(String message) {
		super(Message.ERROR_MESSAGE);
		error = message;
	}

	public String getError() {
		return error;
	}

	@Override
	public String toJSON() {
		JSONObject object = new JSONObject();
		object.put("type", getType());
		object.put("error", error);
		return object.toString();
	}
	
}
