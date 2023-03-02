package oy.tol.chat;

import org.json.JSONObject;

public class JoinMessage extends Message {

	private String channel;

	public JoinMessage(String channel, String topic) {
		super(Message.JOIN_CHANNEL);
		this.channel = channel;
		setMessage(topic);
	}

	public String getChannel() {
		return channel;
	}

	public String getTopic() {
		return getMessage();
	}
	
	@Override
	public String toJSON() {
		JSONObject object = new JSONObject();
		object.put("type", getType());
		object.put("channel", channel);
		object.put("message", getMessage());
		return object.toString();
	}
	
}
