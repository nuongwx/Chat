package server_src;

import java.io.Serializable;
import java.util.ArrayList;

public class Room implements Serializable {
    public static Long count = 0L;
    public Long id;
    public String name;
    public boolean isPrivate;
    public ArrayList<Message> messages = new ArrayList<>();
    public ArrayList<String> users = new ArrayList<>();

    public Room(String name, ArrayList<String> users, boolean isPrivate) {
        this.id = count++;
        this.name = name;
        this.users = users;
        this.isPrivate = isPrivate;
    }

    public String toString() {
        return name;
    }

}
