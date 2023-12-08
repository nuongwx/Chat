package server_src;

public class Message {
    public Long id;
    public String text;
    public String author;
    public Message(String text, String author) {
        this.id = System.currentTimeMillis();
        this.text = text;
        this.author = author;
    }
}
