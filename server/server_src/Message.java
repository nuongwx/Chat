package server_src;

import java.io.Serializable;

public class Message implements Serializable {
    public Long id;
    public String text;
    public String author;

    public Message(String text, String author) {
        this.id = System.currentTimeMillis();
        this.text = text;
        this.author = author;
    }
}
