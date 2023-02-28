package oy.tol.chatserver.messages;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import javax.swing.JComboBox;

import org.json.JSONObject;

public class ChatMessage extends Message {
    private LocalDateTime sent;
    private String nick;
    private String message;
    
    public ChatMessage(LocalDateTime sent, String nick, String message) {
        super(Message.CHAT_MESSAGE);
        this.sent = sent;
        this.nick = nick;
        this.message = message;
    }

    public ChatMessage(String nick, String message) {
        super(Message.CHAT_MESSAGE);
        this.nick = nick;
        this.message = message;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long dateAsLong() {
        return sent.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public void setSent(long epoch) {
        sent = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
    }

    @Override
    public String toJSON() {
        JSONObject object = new JSONObject();
        object.put("type", getType());
        object.put("userName", nick);
        object.put("message", message);
        object.put("sent", dateAsLong());
        return object.toString();
    }
}
