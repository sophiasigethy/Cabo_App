package msp.group3.caboclient;

public class ChatMessage {

    String name;
    String message;
    boolean myMessage;
    int avatar;

    public ChatMessage(String name, String message, boolean myMessage, int avatar){
        this.name = name;
        this.message = message;
        this.myMessage = myMessage;
        this.avatar = avatar;
    }

    public String getName(){
        return this.name;
    }

    public String getMessage(){
        return this.message;
    }

    public boolean getIfMyMessage(){
        return this.myMessage;
    }

    public int getAvatar(){ return this.avatar; }
}
