package me.edgeconsult.chat;

public class Message {
    private String username;
    private Long time;
    private String body;

    public Message(String username, Long time, String body) {
        this.username = username;
        this.time = time;
        this.body = body;
    }

    public String getUsername() {
        return username;
    }

    public Long getTime() {
        return time;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return this.body;
    }
}
