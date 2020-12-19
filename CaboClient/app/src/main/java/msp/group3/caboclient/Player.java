package msp.group3.caboclient;

import java.util.ArrayList;

public class Player {

    private int id;
    private String name;
    private ArrayList<Card> myCards = new ArrayList<>();


    private String status;

    public Player(int id, String name) {
        this.id = id;
        this.name = name;
        this.status=TypeDefs.waiting;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ArrayList<Card> getMyCards() {
        return myCards;
    }

    public void setMyCards(ArrayList<Card> myCards) {
        this.myCards = myCards;
    }

    public void replacePlayer(Player player){
        this.id= player.getId();
        this.name=player.getName();
        this.myCards= player.getMyCards();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}

