package oy.tol.chat;

import org.json.JSONObject;

public class ChangeTopicMessage extends Message {

	private String topic;

	public ChangeTopicMessage(String topic) {
		super(Message.CHANGE_TOPIC);
		this.topic = topic;
	}

	public String getTopic() {
		return topic;
	}
	
	@Override
	public String toJSON() {
		JSONObject object = new JSONObject();
		object.put("type", getType());
		object.put("topic", topic);
		return object.toString();
	}
	
}
