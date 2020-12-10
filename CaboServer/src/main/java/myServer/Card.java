package myServer;

public class Card {

    private String type;
    private int value;

    public Card (int value, String type){
        this.value=value;
        this.type=type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
