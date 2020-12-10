package myServer;

import java.util.ArrayList;
import java.util.HashMap;

public class Player {

    private int id;
    private String name;

    private ArrayList<Integer> cardIndexes = new ArrayList<>();

    private CardSuiteManager cardSuiteManager;

    private int score = 0;

    public Player(CardSuiteManager mgr) {
        this.cardSuiteManager = mgr;
    }

    public Player(int id, String name){
        this.id= id;
        this.name= name;
    }

    public Player(int id, String name, CardSuiteManager mgr) {
        this.id = id;
        this.name = name;
        this.cardSuiteManager = mgr;
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
        this.cardIndexes.add(this.cardSuiteManager.getCardIndex());
    }

    public void discardCard(int index) {
        for (int i = 0; i < this.cardIndexes.size(); i ++) {
            if (this.cardIndexes.get(i) == index) {
                this.cardIndexes.remove(i);
                break;
            }
        }
        this.cardSuiteManager.addDiscardedCard(index);
    }
    
    /**
     * Peek a card from given player and its card index
     * @param player the player to be peeked
     * @param index the card index to be peeked
     * @return
     */
    private Card peek(Player player, int index) {
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

    public void swapWithOtherPlayer(Player other, int myIndex1, int myIndex2, int otherIndex1, int otherIndex2) {
        for (int i = 0; i < other.cardIndexes.size(); i ++) {
            if (otherIndex1 == other.cardIndexes.get(i)) {
                other.cardIndexes.set(i, myIndex1);
            }
            if (otherIndex2 == other.cardIndexes.get(i)) {
                other.cardIndexes.set(i, myIndex2);
            }
        }
        for (int i = 0; i < this.cardIndexes.size(); i ++) {
            if (myIndex1 == this.cardIndexes.get(i)) {
                this.cardIndexes.set(i, otherIndex1);
            }
            if (myIndex2 == this.cardIndexes.get(i)) {
                this.cardIndexes.set(i, otherIndex2);
            }
        }
    }

    private void swapWithPileCards(ArrayList<Card> cards, int myIndex1, int myIndex2, int otherIndex1, int otherIndex2) {
        HashMap<Integer, Card> indexToCardMap = this.cardSuiteManager.getIndexToCardMap();
        HashMap<Card, Integer> cardToIndexMap = this.cardSuiteManager.getCardToIndexMap();
        for (int i = 0; i < cards.size(); i ++) {
            if (indexToCardMap.get(otherIndex1) == cards.get(i)) {
                // `otherIndex1` is really inside cards, can swap;
                cards.set(i, indexToCardMap.get(myIndex1));
                this.cardSuiteManager.getPlayedCards().add(indexToCardMap.get(otherIndex1));

                this.cardSuiteManager.getPlayedCards().remove(indexToCardMap.get(myIndex1));
            }
            if (indexToCardMap.get(otherIndex2) == cards.get(i)) {
                // `otherIndex2` is really inside cards, can swap;
                cards.set(i, indexToCardMap.get(myIndex2));
                this.cardSuiteManager.getPlayedCards().add(indexToCardMap.get(otherIndex2));
                this.cardSuiteManager.getPlayedCards().remove(indexToCardMap.get(myIndex2));

            }
        }
        for (int i = 0; i < this.cardIndexes.size(); i ++) {
            if (myIndex1 == this.cardIndexes.get(i)) {
                this.cardIndexes.set(i, otherIndex1);
            }
            if (myIndex2 == this.cardIndexes.get(i)) {
                this.cardIndexes.set(i, otherIndex2);
            }
        }
    }

    public void swapWithAvailableCards(int myIndex1, int myIndex2, int otherIndex1, int otherIndex2) {
        this.swapWithPileCards(this.cardSuiteManager.getAvailableCards(),
                myIndex1, myIndex2, otherIndex1, otherIndex2);
    }

    public void swapWithDiscardedCards(int myIndex1, int myIndex2, int otherIndex1, int otherIndex2) {
        this.swapWithPileCards(this.cardSuiteManager.getDiscardedCards(),
                myIndex1, myIndex2, otherIndex1, otherIndex2);
    }

    public void debug() {
        System.out.println("============== DEBUG ====================");
        HashMap<Integer, Card> hm = this.cardSuiteManager.getIndexToCardMap();
        for (int i = 0; i < this.cardIndexes.size(); i ++) {
            System.out.println(this.name + " has " + hm.get(this.cardIndexes.get(i)) + " and cardIndex is " + this.cardIndexes.get(i));
        }
        System.out.println("=====================================");

    }

}
