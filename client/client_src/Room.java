package client_src;
import java.util.*;
public class Room {
    public Long id;
    public String name;
    public ArrayList<String> members = new ArrayList<String>();
    private ArrayList<Message> messages = new ArrayList<Message>();
    public RoomMsgPanel roomMsgPanel;

    public Room(Long id, String name) {
        this.id = id;
        this.name = name;
        roomMsgPanel = new RoomMsgPanel(this);
        MainScreen.roomsListModel.addElement(this);
        MainScreen.rooms.add(this);
    }

    public void addMessage(Message message) {
        // if messageid already exists, delete the old message
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
    }

    public void addMember(String username) {
        if(!members.contains(username)) {
            members.add(username);
        }
    }

    public void removeMember(String username) {
        members.remove(username);
    }

    public void refresh() {
        roomMsgPanel.refresh();
    }

    public String toString() {
        return name;
    }
}