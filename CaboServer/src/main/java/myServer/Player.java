package myServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Player {

    private int id;
    private String name;

    private ArrayList<Integer> cardIndexes = new ArrayList<>();

    private CardSuiteManager cardSuiteManager;

    private int score = 0;
    private boolean calledCabo = false;

    public Player(CardSuiteManager mgr) {
        this.cardSuiteManager = mgr;
        this.cardSuiteManager.addPlayer(this);
    }

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

    public ArrayList<Integer> getCardIndexes() {
        return this.cardIndexes;
    }

    public void drawCard() {
        if (this.calledCabo) return;
        this.cardIndexes.add(this.cardSuiteManager.getCardIndex());
    }

    public void discardCard(int index) {
        if (this.calledCabo) return;

        for (int i = 0; i < this.cardIndexes.size(); i ++) {
            if (this.cardIndexes.get(i) == index) {
                this.cardIndexes.remove(i);
                break;
            }
        }
        this.cardSuiteManager.addDiscardedCard(index);
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
     * @param player the player to be peeked
     * @param index the card index to be peeked
     * @return
     */
    private Card peek(Player player, int index) {
        if (this.calledCabo) return null;

        for (int i = 0; i < player.cardIndexes.size(); i ++) {
            if (player.cardIndexes.get(i) == index) {
                return this.cardSuiteManager.getIndexToCardMap().get(index);
            }
        }
        return null;
    }
    /**
     * Peek a card
     * @param index
     */
    public Card peek(int index) {
        return this.peek(this, index);
    }

    /**
     * Spy a player's card
     * @param player
     * @param index
     * @return
     */
    public Card spy(Player player, int index) {
        return this.peek(player, index);
    }

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

    private void swapWithPileCards(ArrayList<Card> cards, int myIndex, int otherIndex) {
        if (this.calledCabo) return;

        HashMap<Integer, Card> indexToCardMap = this.cardSuiteManager.getIndexToCardMap();
        HashMap<Card, Integer> cardToIndexMap = this.cardSuiteManager.getCardToIndexMap();
        for (int i = 0; i < cards.size(); i ++) {
            if (indexToCardMap.get(otherIndex) == cards.get(i)) {
                // `otherIndex1` is really inside cards, can swap;
                cards.set(i, indexToCardMap.get(myIndex));
                this.cardSuiteManager.getPlayedCards().add(indexToCardMap.get(otherIndex));

                this.cardSuiteManager.getPlayedCards().remove(indexToCardMap.get(myIndex));
                break;
            }
        }
        for (int i = 0; i < this.cardIndexes.size(); i ++) {
            if (myIndex == this.cardIndexes.get(i)) {
                this.cardIndexes.set(i, otherIndex);
            }
        }
    }

    public void swapWithAvailableCards(int myIndex, int otherIndex) {
        this.swapWithPileCards(this.cardSuiteManager.getAvailableCards(),
                myIndex, otherIndex);
    }

    public void swapWithDiscardedCards(int myIndex, int otherIndex) {
        this.swapWithPileCards(this.cardSuiteManager.getAvailableCards(),
                myIndex, otherIndex);
    }
    public int getPoint() {
        int points = 0;
        for (int i = 0; i < this.cardIndexes.size(); i++) {
            points += this.cardSuiteManager.getIndexToCardMap().get(this.cardIndexes.get(i)).getValue();
        }
        return points;
    }

//    public void swapWithOtherPlayer(Player other, int myIndex1, int myIndex2, int otherIndex1, int otherIndex2) {
//        for (int i = 0; i < other.cardIndexes.size(); i ++) {
//            if (otherIndex1 == other.cardIndexes.get(i)) {
//                other.cardIndexes.set(i, myIndex1);
//            }
//            if (otherIndex2 == other.cardIndexes.get(i)) {
//                other.cardIndexes.set(i, myIndex2);
//            }
//        }
//        for (int i = 0; i < this.cardIndexes.size(); i ++) {
//            if (myIndex1 == this.cardIndexes.get(i)) {
//                this.cardIndexes.set(i, otherIndex1);
//            }
//            if (myIndex2 == this.cardIndexes.get(i)) {
//                this.cardIndexes.set(i, otherIndex2);
//            }
//        }
//    }
//
//    private void swapWithPileCards(ArrayList<Card> cards, int myIndex1, int myIndex2, int otherIndex1, int otherIndex2) {
//        HashMap<Integer, Card> indexToCardMap = this.cardSuiteManager.getIndexToCardMap();
//        HashMap<Card, Integer> cardToIndexMap = this.cardSuiteManager.getCardToIndexMap();
//        for (int i = 0; i < cards.size(); i ++) {
//            if (indexToCardMap.get(otherIndex1) == cards.get(i)) {
//                // `otherIndex1` is really inside cards, can swap;
//                cards.set(i, indexToCardMap.get(myIndex1));
//                this.cardSuiteManager.getPlayedCards().add(indexToCardMap.get(otherIndex1));
//
//                this.cardSuiteManager.getPlayedCards().remove(indexToCardMap.get(myIndex1));
//            }
//            if (indexToCardMap.get(otherIndex2) == cards.get(i)) {
//                // `otherIndex2` is really inside cards, can swap;
//                cards.set(i, indexToCardMap.get(myIndex2));
//                this.cardSuiteManager.getPlayedCards().add(indexToCardMap.get(otherIndex2));
//                this.cardSuiteManager.getPlayedCards().remove(indexToCardMap.get(myIndex2));
//
//            }
//        }
//        for (int i = 0; i < this.cardIndexes.size(); i ++) {
//            if (myIndex1 == this.cardIndexes.get(i)) {
//                this.cardIndexes.set(i, otherIndex1);
//            }
//            if (myIndex2 == this.cardIndexes.get(i)) {
//                this.cardIndexes.set(i, otherIndex2);
//            }
//        }
//    }
//
//    public void swapWithAvailableCards(int myIndex1, int myIndex2, int otherIndex1, int otherIndex2) {
//        this.swapWithPileCards(this.cardSuiteManager.getAvailableCards(),
//                myIndex1, myIndex2, otherIndex1, otherIndex2);
//    }
//
//    public void swapWithDiscardedCards(int myIndex1, int myIndex2, int otherIndex1, int otherIndex2) {
//        this.swapWithPileCards(this.cardSuiteManager.getDiscardedCards(),
//                myIndex1, myIndex2, otherIndex1, otherIndex2);
//    }

    public void debug() {
        System.out.println("============== DEBUG ====================");
        HashMap<Integer, Card> hm = this.cardSuiteManager.getIndexToCardMap();
        for (int i = 0; i < this.cardIndexes.size(); i ++) {
            System.out.println(this.name + " has " + hm.get(this.cardIndexes.get(i)) + " and cardIndex is " + this.cardIndexes.get(i));
        }
        System.out.println(this.name + "'s score is: " + this.score);
        System.out.println("=====================================");

    }
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
