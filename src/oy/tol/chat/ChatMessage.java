package oy.tol.chat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.json.JSONObject;

public class ChatMessage extends Message {
    private UUID id = UUID.randomUUID();
    private UUID inReplyTo;
    private LocalDateTime sent;
    private String nick;

    public ChatMessage(UUID id, LocalDateTime sent, String nick, String message) {
        super(Message.CHAT_MESSAGE);
        this.id = id;
        this.sent = sent;
        this.nick = nick;
        setMessage(message);
    }

    public ChatMessage(UUID id, Long sent, String nick, String message) {
        super(Message.CHAT_MESSAGE);
        this.id = id;
        this.nick = nick;
        setSent(sent);
        setMessage(message);
    }

    public ChatMessage(LocalDateTime sent, String nick, String message) {
        super(Message.CHAT_MESSAGE);
        this.sent = sent;
        this.nick = nick;
        setMessage(message);
    }

    public ChatMessage(String nick, String message) {
        super(Message.CHAT_MESSAGE);
        this.nick = nick;
        setMessage(message);
        sent = LocalDateTime.now();
    }

    public UUID id() {
        return id;
    }

    public void setAsReplyTo(UUID inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
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
        object.put("id", id.toString());
        if (null != inReplyTo) {
            object.put("inReplyTo", inReplyTo.toString());
        }
        object.put("type", getType());
        object.put("user", nick);
        object.put("message", getMessage());
        object.put("sent", dateAsLong());
        return object.toString();
    }
}
