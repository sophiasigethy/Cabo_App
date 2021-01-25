package myServer;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Objects;

public class Player {

    private int id;
    private String nick;

    private String name="";
    private String dbId;
    private int avatarID;

    @JsonIgnore
    private boolean isKI= false;



    @JsonIgnore
    private boolean noAccount= false;



    @JsonIgnore
    private ArrayList<Card> knownCards= new ArrayList<>();

     @JsonIgnore
    private ArrayList<Card> knownCardsOfOther= new ArrayList<>();

    // The cards belong to this player
    private ArrayList<Card> cards = new ArrayList<>();



    @JsonIgnore
    private Gamestate gamestate;

    private String status;


    private int score = 0;
    @JsonIgnore
    private boolean calledCabo = false;
    private String picture="";
    private String smiley="";

    public Player() {

    }

    public Player(int id, String name){
        this.id= id;
        this.nick =name;
        this.name= name;
        this.status=TypeDefs.MATCHING;
        this.smiley=TypeDefs.smiling;
    }

    public Player(String dbId, String nick, int avatarID) {
        this.nick = nick;
        this.name = nick;
        this.dbId = dbId;
        this.avatarID = avatarID;
    }

    public Player(int id, String nick, Gamestate gs) {
        this.id = id;
        this.name = nick;
        this.nick= nick;
        this.gamestate = gs;
        this.status = TypeDefs.MATCHING;
        this.smiley=TypeDefs.smiling;
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
    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public int getAvatarID() {
        return avatarID;
    }

    public void setAvatarID(int avatarID) {
        this.avatarID = avatarID;
    }

    /**
     * Reset player cards and his status
     */
    public void reset() {
        this.cards = new ArrayList<>();
        this.calledCabo = false;
    }

    /**
     * Get player's cards
     */
    public ArrayList<Card> getCards() {
        return this.cards;
    }

    /**
     * Does a player run out of his cards ?
     *
     * @return
     */
    public boolean hasNoCards() {
        return this.cards.size() == 0;
    }

    /**
     * A play draws a card from `availableCards`,
     */
    public void drawCard() {
        if (this.calledCabo) return;
        this.cards.add(this.gamestate.takeFirstCardFromAvailableCards());
    }

    /**
     * A play discard a given card
     * @param cardsToRemove a list of cards to be discarded
     */
    public void tryDiscardCards(ArrayList<Card> cardsToRemove) {
        if (this.calledCabo) return;

        Card card = cardsToRemove.get(0);
        for (int i = 1; i < cardsToRemove.size(); i ++) {
            if (card.getValue() != cardsToRemove.get(i).getValue()) {
                // not equal, return
                return;
            }
        }

        // Remove all of them
        for (int i = 0; i < cardsToRemove.size(); i ++) {
            this.gamestate.getDiscardedCards().add(cardsToRemove.get(i));
            this.cards.remove(cardsToRemove.get(i));
        }
    }

    /**
     * Swap with other player.
     * No need to invoke this method if client part already knows the detailed card information
     * @param other
     * @param myCard
     * @param otherCard
     */
    public void swapWithOtherPlayer(Player other, Card myCard, Card otherCard) {
        if (this.calledCabo) return;

        for (int i = 0; i < other.cards.size(); i ++) {
            if (otherCard.equalsCard(other.cards.get(i))) {
                other.cards.set(i, myCard);
                break;
            }
        }
        for (int i = 0; i < this.cards.size(); i ++) {
            if (myCard.equalsCard(this.cards.get(i))) {
                this.cards.set(i, otherCard);
                break;
            }
        }

    }
    /**
     * Swap with card pile
     * No need to invoke this method if client part already knows the detailed card information
     * @param cardsToSwap
     * @param myCard
     * @param otherCard
     * @param shouldUpdatePlayedCards
     */
    public void swapWithPileCards(ArrayList<Card> cardsToSwap, Card myCard, Card otherCard, boolean shouldUpdatePlayedCards) {

        if (this.calledCabo) return;
        boolean swapped = false;
        for (int i = 0; i < cardsToSwap.size(); i ++) {
            if (swapped) {
                break;
            }
            if (otherCard.equals(cardsToSwap.get(i))) {
                // `otherCard` is really inside cards, can swap;
                for (int j = 0; j < this.cards.size(); j ++) {
                    if (myCard.equals(this.cards.get(j))) {
                        // `myCard` is really inside cards, can swap
                        this.cards.set(j, otherCard);
                        cardsToSwap.set(i, myCard);
                        // NOTE: It only need to be called in `availableCards`, no need for `discardedCards`
                        if (shouldUpdatePlayedCards) {
                            this.gamestate.getPlayedCards().add(otherCard);
                            this.gamestate.getPlayedCards().remove(myCard);
                        }
                        swapped = true;
                    }
                }
            }
        }
    }
    /**
     * Swap with available card pile
     * No need to invoke this method if client part already knows the detailed card information
     * @param myCard
     * @param otherCard
     */
    public void swapWithAvailableCards(Card myCard, Card otherCard) {
        this.swapWithPileCards(this.gamestate.getAvailableCards(), myCard, otherCard, true);
    }
    /**
     * Swap with discarded card pile
     * No need to invoke this method if client part already knows the detailed card information
     * @param myCard
     * @param otherCard
     */
    public void swapWithDiscardedCards(Card myCard, Card otherCard) {
        this.swapWithPileCards(this.gamestate.getDiscardedCards(), myCard, otherCard, false);
    }
    /**
     * Get the points of all cards play obtains
     * @return
     */
    public int calculatePoints() {
        int points = 0;

        for (int i = 0; i < this.cards.size(); i ++) {
            points += this.cards.get(i).getValue();
        }
        return points;
    }

    /**
     * Call cabo
     */
    public void callCabo() {
        this.calledCabo = true;
    }

    public void setCalledCabo(boolean calledCabo) {
        this.calledCabo = calledCabo;
    }

    public boolean getCalledCabo() {
        return this.calledCabo;
    }

    public void setScore(int score) {
        this.score = score;
    }
//
    public int getScore() {
        return this.score;
    }

    public void setMyCards(ArrayList<Card> myCards) {
        this.cards = myCards;
    }

    public ArrayList<Card> getMyCards() {
        return this.cards;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void swapWithOwnCard(Card ownCard, Card currentPickedCard){
        //gezogene Karte wird von availableCards entfernt und in playedCards hinzugefügt-> ist letzter Eintrag von playedCards jetzt
        //diese Karte muss currentPickedCard entsprechen
        gamestate.takeFirstCardFromAvailableCards();
        for (int i=0; i<this.cards.size(); i++ ){
            if (ownCard.equalsCard(cards.get(i))){
                cards.remove(i);
                cards.add(i, currentPickedCard);
            }
        }

    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getSmiley() {
        return smiley;
    }

    public void setSmiley(String smiley) {
        this.smiley = smiley;
    }

    public String getDbId() {
        return dbId;
    }

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(nick, player.nick) && Objects.equals(dbId, player.dbId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nick, dbId);
    }


    public Gamestate getGamestate() {
        return gamestate;
    }

    public void setGamestate(Gamestate gamestate) {
        this.gamestate = gamestate;
    }

    public boolean isKI() {
        return isKI;
    }

    public void setKI(boolean KI) {
        isKI = KI;
    }

    public ArrayList<Card> getKnownCards() {
        return knownCards;
    }

    public void setKnownCards(ArrayList<Card> knownCards) {
        this.knownCards = knownCards;
    }
    public ArrayList<Card> getKnownCardsOfOther() {
        return knownCardsOfOther;
    }

    public void setKnownCardsOfOther(ArrayList<Card> knownCardsOfOther) {
        this.knownCardsOfOther = knownCardsOfOther;
    }

    public boolean isNoAccount() {
        return noAccount;
    }

    public void setNoAccount(boolean noAccount) {
        this.noAccount = noAccount;
    }


}
