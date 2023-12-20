package server_src;

import java.util.ArrayList;
import java.util.Objects;

public class Room {
    public static Long count = 0L;
    public Long id;
    public String name;
    public boolean isPrivate = false;
    public ArrayList<Message> messages = new ArrayList<Message>();
    public ArrayList<String> users = new ArrayList<String>();
    public Room(String name, ArrayList<String> users, boolean isPrivate) {
        this.id = count++;
        this.name = name;
        this.users = users;
        this.isPrivate = isPrivate;
    }
    public void addMessage(Message message) {
        messages.add(message);
    }
    public void removeMessage(Message message) {
        messages.stream().findFirst().filter(m -> Objects.equals(m.id, message.id)).ifPresent(m -> m.text = "This message has been deleted");
    }
    public void addUser(String username) {
        users.add(username);
    }
    public void removeUser(String username) {
        users.remove(username);
    }
    public String toString() {
        return name;
    }

}
