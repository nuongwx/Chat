package client_src;
import java.util.*;
public class Room {
    public Long id;
    public String name;
    public ArrayList<String> members = new ArrayList<>();
    private final ArrayList<Message> messages = new ArrayList<>();
    public RoomMsgPanel roomMsgPanel;
    public boolean isPrivate = false;

    public Room(Long id, String name) {
        this.id = id;
        this.name = name;
        roomMsgPanel = new RoomMsgPanel(this);
        MainScreen.roomsListModel.addElement(this);
        MainScreen.rooms.add(this);
    }

    public Room(Long id, String name, boolean isPrivate) {
        this.id = id;
        this.name = name;
        this.isPrivate = isPrivate;
        roomMsgPanel = new RoomMsgPanel(this);
        MainScreen.roomsListModel.addElement(this);
        MainScreen.rooms.add(this);
    }

    public void addMessage(Message message) {
        // if messageId already exists, delete the old message
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).id.equals(message.id)) {
                messages.remove(i);
                roomMsgPanel.refresh();
                return;
            }
        }

        messages.add(message);
        roomMsgPanel.addMessage(message);
    }

    public void clearMessages() {
        messages.clear();
        roomMsgPanel.refresh();
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setName(String name) {
        this.name = name;
        MainScreen.roomsListModel.set(MainScreen.roomsListModel.indexOf(this), this);
    }

    public void addMember(String username) {
        if(!members.contains(username)) {
            members.add(username);
        }
    }

    public String toString() {
        return name;
    }
}