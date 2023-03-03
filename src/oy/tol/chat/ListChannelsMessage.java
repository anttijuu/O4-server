package oy.tol.chat;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

public class ListChannelsMessage extends Message {

	private List<String> channels;

	public ListChannelsMessage() {
		super(Message.LIST_CHANNELS);
	}

	public List<String> getChannels() {
		return channels;
	}

	public void addChannel(String channel) {
		if (null == channels) {
			channels = new ArrayList<>();
		}
		channels.add(channel);
	}

	@Override
	public String toJSON() {
		JSONObject object = new JSONObject();
		object.put("type", getType());
		if (null != channels) {
			JSONArray array = new JSONArray();
			for (String item : channels) {
				array.put(item);
			}
			object.put("channels", array);
		}
		return object.toString();
	}
	
}
