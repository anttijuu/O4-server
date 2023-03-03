package oy.tol.chat;

import org.json.JSONObject;

public class ErrorMessage extends Message {

	private String error;
	private int requiresShutdown = 0;

	public ErrorMessage(String message) {
		super(Message.ERROR_MESSAGE);
		error = message;
	}

	public ErrorMessage(String message, boolean requireClientShutdown) {
		super(Message.ERROR_MESSAGE);
		error = message;
		requiresShutdown = requireClientShutdown ? 1 : 0;
	}

	public String getError() {
		return error;
	}

	public boolean requiresClientShutdown() {
		return requiresShutdown != 0;
	}

	@Override
	public String toJSON() {
		JSONObject object = new JSONObject();
		object.put("type", getType());
		object.put("error", error);
		object.put("clientshutdown", requiresShutdown);
		return object.toString();
	}
	
}
