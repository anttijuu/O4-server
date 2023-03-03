package oy.tol.chat;

import org.json.JSONObject;

public class JoinMessage extends Message {

	private String channel;

	public JoinMessage(String channel) {
		super(Message.JOIN_CHANNEL);
		this.channel = channel;
	}

	public String getChannel() {
		return channel;
	}
	
	@Override
	public String toJSON() {
		JSONObject object = new JSONObject();
		object.put("type", getType());
		object.put("channel", channel);
		return object.toString();
	}
	
}
