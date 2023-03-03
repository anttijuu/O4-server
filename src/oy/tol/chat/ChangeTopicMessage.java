package oy.tol.chat;

import org.json.JSONObject;

public class ChangeTopicMessage extends Message {

	private String topic;

	public ChangeTopicMessage() {
		super(Message.CHANGE_TOPIC);
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
