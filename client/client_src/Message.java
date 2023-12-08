package client_src;

public class Message {
    public Long id;
    public String text;
    public String author;
    public Message(Long id, String text, String author) {
        this.id = id;
        this.text = text;
        this.author = author;
    }
}
