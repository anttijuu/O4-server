package oy.tol.chatserver;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class ChatMessage {
    private LocalDateTime sent;
    private String nick;
    private String message;
    
    public ChatMessage(LocalDateTime sent, String nick, String message) {
        this.sent = sent;
        this.nick = nick;
        this.message = message;
    }

    public ChatMessage(String nick, String message) {
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

    long dateAsLong() {
        return sent.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    void setSent(long epoch) {
        sent = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
    }
}
