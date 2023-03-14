package oy.tol.chat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.json.JSONObject;

public class ChatMessage extends Message {
    private UUID id = UUID.randomUUID();
    private UUID inReplyTo;
    private String directMessageTo;
    private LocalDateTime sent;
    private String nick;
    private String message;

    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

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

    public UUID isReplyTo() {
        return inReplyTo;
    }

    public void setAsReplyTo(UUID inReplyTo) {
        this.inReplyTo = inReplyTo;
    }

    public boolean isDirectMessage() {
        return directMessageTo != null;
    }

    public String directMessageRecipient() {
        return directMessageTo;
    }

    public void setRecipient(String recipientNick) {
        directMessageTo = recipientNick;
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

    public LocalDateTime getSent() {
        return this.sent;
    }

    public void setSent(long epoch) {
        sent = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
    }

    public String sentAsString() {
        if (LocalDateTime.now().getDayOfMonth() == sent.getDayOfMonth()) {
            return sent.format(timeFormatter);
        }
        return sent.format(dateTimeFormatter);
    }

    @Override
    public String toJSON() {
        JSONObject object = new JSONObject();
        object.put("id", id.toString());
        if (null != inReplyTo) {
            object.put("inReplyTo", inReplyTo.toString());
        }
        if (null != directMessageTo) {
            object.put("directMessageTo", directMessageTo);
        }
        object.put("type", getType());
        object.put("user", nick);
        object.put("message", getMessage());
        object.put("sent", dateAsLong());
        return object.toString();
    }
}
