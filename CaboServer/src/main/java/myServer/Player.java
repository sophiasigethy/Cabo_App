package myServer;

import java.util.ArrayList;

public class Player {

    private int id;
    private String name;
    //every card the player posseses is saved here
    private ArrayList cards = new ArrayList();

    public Player(int id, String name){
        this.id= id;
        this.name= name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public ArrayList getCards() {
        return cards;
    }

    public void setCards(ArrayList cards) {
        this.cards = cards;
    }
}
