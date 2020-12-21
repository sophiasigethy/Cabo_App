package myServer;

import java.util.ArrayList;
import java.util.HashMap;

public class Player {

    private int id;
    private String name;

    private ArrayList<Card> cards = new ArrayList<>();
    private CardSuiteManager cardSuiteManager;

    private int score = 0;
    private boolean calledCabo = false;

    public Player(int id, String name){
        this.id= id;
        this.name= name;
    }

    public Player(int id, String name, CardSuiteManager mgr) {
        this.id = id;
        this.name = name;
        this.cardSuiteManager = mgr;
        this.cardSuiteManager.addPlayer(this);
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

    /**
     * Reset player cards and his status
     */
    public void reset() {
        this.cards = new ArrayList<>();
        this.calledCabo = false;
    }
    /**
     * Retrieve player's cards
     */
    public ArrayList<Card> getCards() {
        return this.cards;
    }

    /**
     * Does a player run out of his cards ?
     * @return
     */
    public boolean hasNoCards() {
        return this.cards.size() == 0;
    }
    /**
     * A play draws an index corresponding to the card from `availableCards`,
     */
    public void drawCard() {
        if (this.calledCabo) return;

        this.cards.add(this.cardSuiteManager.getFirstCardFromAvailableCards());
    }

    /**
     * A play discard a card by given index
     * @param cardsToRemove an index which associated with real card
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
            this.cardSuiteManager.getDiscardedCards().add(cardsToRemove.get(i));
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
            if (otherCard == other.cards.get(i)) {
                other.cards.set(i, myCard);
                break;
            }
        }
        for (int i = 0; i < this.cards.size(); i ++) {
            if (myCard == this.cards.get(i)) {
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
    private void swapWithPileCards(ArrayList<Card> cardsToSwap, Card myCard, Card otherCard, boolean shouldUpdatePlayedCards) {

        if (this.calledCabo) return;
        boolean swapped = false;
        for (int i = 0; i < cardsToSwap.size(); i ++) {
            if (swapped) {
                break;
            }
            if (otherCard == cardsToSwap.get(i)) {
                // `otherCard` is really inside cards, can swap;
                for (int j = 0; j < this.cards.size(); j ++) {
                    if (myCard == this.cards.get(j)) {
                        // `myCard` is really inside cards, can swap
                        this.cards.set(j, otherCard);
                        cardsToSwap.set(i, myCard);
                        // NOTE: It only need to be called in `availableCards`, no need for `discardedCards`
                        if (shouldUpdatePlayedCards) {
                            this.cardSuiteManager.getPlayedCards().add(otherCard);
                            this.cardSuiteManager.getPlayedCards().remove(myCard);
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
        this.swapWithPileCards(this.cardSuiteManager.getAvailableCards(), myCard, otherCard, true);
    }
    /**
     * Swap with discarded card pile
     * No need to invoke this method if client part already knows the detailed card information
     * @param myCard
     * @param otherCard
     */
    public void swapWithDiscardedCards(Card myCard, Card otherCard) {
        this.swapWithPileCards(this.cardSuiteManager.getDiscardedCards(), myCard, otherCard, false);
    }
    /**
     * Get the points of all cards play obtains
     * @return
     */
    public int getPoint() {
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

    public int getScore() {
        return this.score;
    }
}
