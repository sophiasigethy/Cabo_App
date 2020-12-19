package myServer;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;

public class Player {



    private int id;
    private String name;

   @JsonIgnore
    private ArrayList<Integer> cardIndexes = new ArrayList<>();

    private ArrayList<Card> myCards = new ArrayList<>();

    @JsonIgnore
    private CardSuiteManager cardSuiteManager;


    private String status;

    @JsonIgnore
    private int score = 0;
    @JsonIgnore
    private boolean calledCabo = false;


    public Player(int id, String name){
        this.id= id;
        this.name= name;
        this.status=TypeDefs.waiting;
    }

    public Player(int id, String name, CardSuiteManager mgr) {
        this.id = id;
        this.name = name;
        this.cardSuiteManager = mgr;
        this.cardSuiteManager.addPlayer(this);
        this.status=TypeDefs.waiting;
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




    public ArrayList<Integer> getCardIndexes() {
        return this.cardIndexes;
    }

    /**
     * A play draws an index corresponding to the card from `availableCards`,
     */
    public void drawCard() {
        if (this.calledCabo) return;
        this.cardIndexes.add(this.cardSuiteManager.getIndexByCard(
                this.cardSuiteManager.getFirstCardFromAvailableCards()
        ));
    }

    /**
     * A play discard a card by given index
     * @param index an index which associated with real card
     */
    public void discardCard(int index) {
        if (this.calledCabo) return;

        for (int i = 0; i < this.cardIndexes.size(); i ++) {
            if (this.cardIndexes.get(i) == index) {
                this.cardIndexes.remove(i);
                this.cardSuiteManager.addDiscardedCard(index);
                break;
            }
        }
    }

    public void discardCards(int [] indexes) {
        if (this.calledCabo) return;

        for (int i = 0; i < indexes.length; i ++ ) {
            if (! this.cardIndexes.contains(indexes[i])) {
                return;
            }
        }
        ArrayList<Card> cards = new ArrayList<>();

        for (int i = 0; i < indexes.length; i ++) {
            cards.add(this.cardSuiteManager.getIndexToCardMap().get(indexes[i]));
        }
        Card card = cards.get(0);
        for (int i = 1; i < cards.size(); i ++) {
            if (card.getValue() != cards.get(i).getValue()) {
                return;
            }
        }

        // Remove all of them
        for (int i = 0; i < indexes.length; i ++) {
            this.cardSuiteManager.getDiscardedCards().add(this.cardSuiteManager.getIndexToCardMap().get(indexes[i]));
            this.cardIndexes.remove(new Integer(indexes[i]));

        }
    }
    public void tryDiscardTwoCards(int index1, int index2) {
        int [] indexes = {index1, index2};
        this.discardCards(indexes);
    }
    public void tryDiscardThreeCards(int index1, int index2, int index3) {
        int [] indexes = {index1, index2, index3};
        this.discardCards(indexes);
        // Draw additional card
        this.drawCard();
    }

    /**
     * Peek a card from given player and its card index
     * No need to invoke this method if client part already knows the detailed card information
     * @param player the player to be peeked
     * @param index the card index to be peeked
     * @return
     */
    private Card peek(Player player, int index) {
        if (this.calledCabo) return null;

        for (int i = 0; i < player.cardIndexes.size(); i ++) {
            if (player.cardIndexes.get(i) == index) {
                return this.cardSuiteManager.getCardByIndex(index);
            }
        }
        return null;
    }
    private Card peek(Player player, Card card) {
        if (this.calledCabo) return null;

        return card;
    }
    /**
     * Peek a card
     * No need to invoke this method if client part already knows the detailed card information
     * @param index
     */
    public Card peek(int index) {
        return this.peek(this, index);
    }
    public Card peek(Card card) {
        return this.peek(this, card);
    }
    /**
     * Spy a player's card
     * No need to invoke this method if client part already knows the detailed card information
     * @param player
     * @param index
     * @return
     */
    public Card spy(Player player, int index) {
        if (this != player) {
            return this.peek(player, index);
        }
        return null;
    }
    public Card spy(Player player, Card card) {
        if (this != player)
            return this.peek(player, card);
        return null;
    }
    /**
     * Swap with other player.
     * No need to invoke this method if client part already knows the detailed card information
     * @param other
     * @param myIndex
     * @param otherIndex
     */
    public void swapWithOtherPlayer(Player other, int myIndex, int otherIndex) {
        if (this.calledCabo) return;

        for (int i = 0; i < other.cardIndexes.size(); i ++) {
            if (otherIndex == other.cardIndexes.get(i)) {
                other.cardIndexes.set(i, myIndex);
                break;
            }
        }
        for (int i = 0; i < this.cardIndexes.size(); i ++) {
            if (myIndex == this.cardIndexes.get(i)) {
                this.cardIndexes.set(i, otherIndex);
                break;
            }
        }
    }
    public void swapWithOtherPlayer(Player other, Card myCard, Card otherCard) {
        if (this.calledCabo) return;
        int myIndex = this.cardSuiteManager.getIndexByCard(myCard);
        int otherIndex = this.cardSuiteManager.getIndexByCard(otherCard);
        this.swapWithOtherPlayer(other, myIndex, otherIndex);
    }
    /**
     * Swap with card pile
     * No need to invoke this method if client part already knows the detailed card information
     * @param cards
     * @param myIndex
     * @param otherIndex
     * @param shouldUpdatePlayedCards
     */
    private void swapWithPileCards(ArrayList<Card> cards, int myIndex, int otherIndex, boolean shouldUpdatePlayedCards) {
        if (this.calledCabo) return;

        for (int i = 0; i < cards.size(); i ++) {
            if (this.cardSuiteManager.getCardByIndex(otherIndex) == cards.get(i)) {
                // `otherIndex` is really inside cards, can swap;
                cards.set(i, cardSuiteManager.getCardByIndex(myIndex));
                // : It only need to be called in `availableCards`, no need for `discardedCards`
                if (shouldUpdatePlayedCards) {
                    this.cardSuiteManager.getPlayedCards().add(cardSuiteManager.getCardByIndex(otherIndex));
                    this.cardSuiteManager.getPlayedCards().remove(cardSuiteManager.getCardByIndex(myIndex));
                }
                break;
            }
        }
        for (int i = 0; i < this.cardIndexes.size(); i ++) {
            if (myIndex == this.cardIndexes.get(i)) {
                this.cardIndexes.set(i, otherIndex);
            }
        }
    }
    private void swapWithPileCards(ArrayList<Card> cards, Card myCard, Card otherCard, boolean shouldUpdatePlayedCards) {
        int myIndex = this.cardSuiteManager.getIndexByCard(myCard);
        int otherIndex = this.cardSuiteManager.getIndexByCard(otherCard);
        this.swapWithPileCards(cards, myIndex, otherIndex, shouldUpdatePlayedCards);
    }
    /**
     * Swap with available card pile
     * No need to invoke this method if client part already knows the detailed card information
     * @param myIndex
     * @param otherIndex
     */
    public void swapWithAvailableCards(int myIndex, int otherIndex) {
        this.swapWithPileCards(this.cardSuiteManager.getAvailableCards(),
                myIndex, otherIndex, true);
    }
    public void swapWithAvailableCards(Card myCard, Card otherCard) {
        this.swapWithPileCards(this.cardSuiteManager.getAvailableCards(), myCard, otherCard, true);
    }
    /**
     * Swap with discarded card pile
     * No need to invoke this method if client part already knows the detailed card information
     * @param myIndex
     * @param otherIndex
     */
    public void swapWithDiscardedCards(int myIndex, int otherIndex) {
        this.swapWithPileCards(this.cardSuiteManager.getDiscardedCards(),
                myIndex, otherIndex, false);
    }
    public void swapWithDiscardedCards(Card myCard, Card otherCard) {
        this.swapWithPileCards(this.cardSuiteManager.getDiscardedCards(), myCard, otherCard, false);
    }
    /**
     * Get the points of all cards play obtains
     * @return
     */
    public int calculatePoints() {
        int points = 0;
        for (int i = 0; i < this.cardIndexes.size(); i++) {
            points += this.cardSuiteManager.getCardByIndex(this.cardIndexes.get(i)).getValue();
        }
        return points;
    }

    public void debug() {
        System.out.println("============== DEBUG ====================");
        HashMap<Integer, Card> hm = this.cardSuiteManager.getIndexToCardMap();
        for (int i = 0; i < this.cardIndexes.size(); i ++) {
            System.out.println(this.name + " has " + hm.get(this.cardIndexes.get(i)) + " and cardIndex is " + this.cardIndexes.get(i));
        }
        System.out.println(this.name + "'s score is: " + this.score);
        System.out.println("=====================================");

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

    public int getScore() {
        return this.score;
    }




    public void setMyCards(ArrayList<Card> myCards) {
        this.myCards = myCards;
    }

    public ArrayList<Card> getMyCards() {
        updateCardList();
        return this.myCards;
    }
    public void updateCardList() {
        ArrayList<Card> cards = new ArrayList<>();
        for (int i = 0; i < this.cardIndexes.size(); i ++) {
            cards.add(this.cardSuiteManager.getCardByIndex(this.cardIndexes.get(i)));
        }
        this.myCards =cards;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
