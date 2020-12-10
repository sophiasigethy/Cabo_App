package myServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;


public class CardSuiteManager {
    // reference: https://www.youtube.com/watch?v=aCo-iNedw2g
    private final static String SPADE = "SPADE";
    private final static String CLUB = "CLUB";
    private final static String HEART = "HEART";
    private final static String DIAMOND = "DIAMOND";

    private final static String PEEK = "PEEK";
    private final static String SPY = "SPY";
    private final static String SWAP = "SWAP";

    private ArrayList<Card> availableCards = new ArrayList<>();
    private ArrayList<Card> playedCards = new ArrayList<>();
    private ArrayList<Card> discardedCards = new ArrayList<>();

    private HashMap<Integer, Card> indexToCardMap = new HashMap<>();
    private HashMap<Card, Integer> cardToIndexMap = new HashMap<>();

    final static int CARD_NUMBER = 52;

    public CardSuiteManager() {
        this.generateSuite();
        this.shuffleSuite();
    }

    /**
     * Generate cards
     */
    private void generateSuite() {
        for (int i = 0; i <= CARD_NUMBER / 4; i ++) {
            if (i == 0 || i == CARD_NUMBER / 4) {
                this.availableCards.add(new Card(i, SPADE));
                this.availableCards.add(new Card(i, CLUB));
            } else if (i == 7 || i == 8) {
                // NOTE: 7 or 8: 'peek'
                this.availableCards.add(new Card(i, SPADE, PEEK));
                this.availableCards.add(new Card(i, CLUB, PEEK));
                this.availableCards.add(new Card(i, HEART, PEEK));
                this.availableCards.add(new Card(i, DIAMOND, PEEK));
            } else if (i == 9 || i == 10) {
                // NOTE: 9 or 10: 'spy'
                this.availableCards.add(new Card(i, SPADE, SPY));
                this.availableCards.add(new Card(i, CLUB, SPY));
                this.availableCards.add(new Card(i, HEART, SPY));
                this.availableCards.add(new Card(i, DIAMOND, SPY));
            } else if (i == 11 || i == 12) {
                // 11 or 12: 'swap'
                this.availableCards.add(new Card(i, SPADE, SWAP));
                this.availableCards.add(new Card(i, CLUB, SWAP));
                this.availableCards.add(new Card(i, HEART, SWAP));
                this.availableCards.add(new Card(i, DIAMOND, SWAP));
            } else {
                this.availableCards.add(new Card(i, SPADE));
                this.availableCards.add(new Card(i, CLUB));
                this.availableCards.add(new Card(i, HEART));
                this.availableCards.add(new Card(i, DIAMOND));
            }
        }
    }

    /**
     * Shuffle cards
     */
    private void shuffleSuite() {
        // Collections.shuffle(this.availableCards);

        for (int i = 0; i < this.availableCards.size(); i ++) {
            Card card = this.availableCards.get(i);
            indexToCardMap.put(i, card);
            cardToIndexMap.put(card, i);
        }
    }

    /**
     * Distribute a card to given player
     * @param player the player going to draw a card from deck
     */
//    void distributeCardToPlayer(Player player) {
//        Card card = this.availableCards.get(0);
//        this.availableCards.remove(0);
//        this.playedCards.add(card);
//        player.addCard(card);
//    }

    /**
     * Debug: Simply check the implementation
     */
    public void debug() {
        System.out.println("============== DEBUG ====================");
        System.out.println("----- Available Card ---------");
        for (int i = 0; i < this.availableCards.size(); i ++) {
            System.out.println(this.availableCards.get(i));
        }
        System.out.println("----- Played Card ---------");
        for (int i = 0; i < this.playedCards.size(); i ++) {
            System.out.println(this.playedCards.get(i));
        }
        System.out.println("----- Discarded Card ---------");
        for (int i = 0; i < this.discardedCards.size(); i ++) {
            System.out.println(this.discardedCards.get(i));
        }
        if (this.playedCards.size() + this.availableCards.size() != CARD_NUMBER) {
            System.out.println("Error");
        } else {
            System.out.println("Seems Correct");
        }
        System.out.println("=====================================");

    }

//    public Card getCard() {
//        Card card = this.availableCards.get(0);
//        this.availableCards.remove(0);
//        this.playedCards.add(card);
//        return card;
//    }
    public int getCardIndex() {
        Card card = this.availableCards.get(0);
        this.availableCards.remove(0);
        this.playedCards.add(card);
        return this.cardToIndexMap.get(card);
    }
    public void addDiscardedCard(int index) {
        this.discardedCards.add(this.indexToCardMap.get(index));
    }

    public ArrayList<Card> getAvailableCards() {
        return this.availableCards;
    }
    public ArrayList<Card> getDiscardedCards() {
        return this.discardedCards;
    }
    public ArrayList<Card> getPlayedCards() {
        return this.playedCards;
    }

    public HashMap<Integer, Card> getIndexToCardMap() { return this.indexToCardMap; };
    public HashMap<Card, Integer> getCardToIndexMap() { return this.cardToIndexMap; }

    
}
