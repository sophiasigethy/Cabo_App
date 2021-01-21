package msp.group3.caboclient;

public class ChatMessage {

    String name;
    String message;
    boolean myMessage;

    public ChatMessage(String name, String message, boolean myMessage){
        this.name = name;
        this.message = message;
        this.myMessage = myMessage;
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
}
