package oy.tol.chat;

import org.json.JSONObject;

public class StatusMessage extends Message {

	private String status;

	public StatusMessage(String message) {
		super(Message.STATUS_MESSAGE);
		status = message;
	}

	public String getStatus() {
		return status;
	}
	
	@Override
	public String toJSON() {
		JSONObject object = new JSONObject();
		object.put("type", getType());
		object.put("status", status);
		return object.toString();
	}
	
}
