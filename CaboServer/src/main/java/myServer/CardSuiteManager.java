package myServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CardSuiteManager {
    // reference: https://www.youtube.com/watch?v=aCo-iNedw2g
    private final static String SPADE = "SPADE";
    private final static String CLUB = "CLUB";
    private final static String HEART = "HEART";
    private final static String DIAMOND = "DIAMOND";

    private final static String PEEK = "PEEK";
    private final static String SPY = "SPY";
    private final static String SWAP = "SWAP";

    final static int CARD_NUMBER = 13 * 4;
    private final static int DISTRIBUTION_CARD_NUMBER_AT_BEGINNING = 4;

    // The cards in available pile, face-down, never exposed these cards to clients
    private ArrayList<Card> availableCards = null;
    // The cards drawn from available card pile, they may be in discarded card pile, clients' decks.
    private ArrayList<Card> playedCards = null;
    // The cards discarded by clients, face-up
    private ArrayList<Card> discardedCards = null;

    // A mapping between the order of shuffled cards and real card value
    private HashMap<Integer, Card> indexToCardMap = null;
    // A mapping between the real card value and the order of shuffled cards.
    private HashMap<Card, Integer> cardToIndexMap = null;

    // The players participated in this game
    private ArrayList<Player> players = new ArrayList<>();

    // The game is terminate or not. It mean at least one client reaches more than 100 score.
    private boolean terminated = false;

    // Logger handler
    private final static Logger logger = Logger.getLogger(CardSuiteManager.class.getName());

    public CardSuiteManager() {
        generateSuite();
    }
    public CardSuiteManager(boolean shouldShuffle) {
        generateSuite(shouldShuffle);
    }
    public void generateSuite() {
        this.generateSuite(true);
    }

    public void generateSuite(boolean shouldShuffle) {
        if (this.terminated) {
            logger.log(Level.INFO, "The game is terminated, no need to generate suites anymore.");
            this.availableCards = null;
            this.playedCards = null;
            this.discardedCards = null;
            this.indexToCardMap = null;
            this.cardToIndexMap = null;
            return;
        }
        this.availableCards = new ArrayList<>();
        this.playedCards = new ArrayList<>();
        this.discardedCards = new ArrayList<>();

        this.indexToCardMap = new HashMap<>();
        this.cardToIndexMap = new HashMap<>();

        this.generateCards();
        this.shuffleCards(shouldShuffle);
        logger.log(Level.INFO, "The card suite is generated successfully.");

    }
    /**
     * Generate cards
     */
    private void generateCards() {
        for (int i = 0; i <= 13; i ++) {
            if (i == 0 || i == 13) {
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
     * @param shouldShuffle the flag to show whether the cards should be shuffled
     */
    private void shuffleCards(boolean shouldShuffle) {
        if (shouldShuffle) {
            logger.log(Level.FINER, "Shuffling the cards");
            Collections.shuffle(this.availableCards);
        }
        for (int i = 0; i < this.availableCards.size(); i ++) {
            Card card = this.availableCards.get(i);
            indexToCardMap.put(i, card);
            cardToIndexMap.put(card, i);
        }
    }

    /**
     * Terminate the game
     */
    public void terminate() {
        this.terminated = true;
    }

    public void addPlayer(Player player) {
        this.players.add(player);
    }
    private ArrayList<Player> getPlayers() {
        return this.players;
    }

    /**
     * @TODO Make this function easy to extend
     * Calculate the scores once caboed
     */
    public void calcScores() {
        // ArrayList<Player> allPlayers = this.cardSuiteManager.getPlayers();
        // Case1: Checking the special case, (0, 0, 13, 13)
        for (int i = 0; i < players.size(); i ++) {
            Player player = players.get(i);
            if (player.getCardIndexes().size() == 4) {
                int a = player.getCardIndexes().get(0);
                int b = player.getCardIndexes().get(1);
                int c = player.getCardIndexes().get(2);
                int d = player.getCardIndexes().get(3);

                Card ca = this.indexToCardMap.get(a);
                Card cb = this.indexToCardMap.get(b);
                Card cc = this.indexToCardMap.get(c);
                Card cd = this.indexToCardMap.get(d);

                boolean special = false;
                if (ca.getValue() == cb.getValue() && (ca.getValue() == 0 || ca.getValue() == 13)) {
                    if (cc.getValue() == cd.getValue() && (cd.getValue() == 0 || cd.getValue() == 13)) {
                        // Player has special cards (0, 0, 13, 13) or (13, 13, 0, 0)
                        special = true;
                    }
                } else if (ca.getValue() == cc.getValue() && (ca.getValue() == 0 || ca.getValue() == 13)) {
                    if (cb.getValue() == cd.getValue() && (cd.getValue() == 0 || cd.getValue() == 13)) {
                        // Player has special cards (0, 13, 0, 13) or (13, 0, 13, 0)
                        special = true;
                    }
                } else if (ca.getValue() == cd.getValue() && (ca.getValue() == 0 || ca.getValue() == 13)) {
                    if (cb.getValue() == cc.getValue() && (cb.getValue() == 0 || cb.getValue() == 13)) {
                        // Player has special cards (0, 13, 13, 0) or (13, 0, 0, 13)
                        special = true;
                    }
                }
                if (special) {
                    for (int k = 0; k < players.size(); k++) {
                        Player _player = players.get(k);
                        if (k != i) {
                            _player.setScore(_player.getScore() + 50);
                            if (_player.getScore() == 100) {
                                _player.setScore(50);
                             }
                            if (_player.getScore() > 100) {
                                 this.terminate();
                             }
                        }
                    }
                    // Inside speical case
                    return;
                }
            }
        }

        // Case 2: common case
        int smallestPoint = Integer.MAX_VALUE;
        for (int i = 0; i < players.size(); i ++) {
            if (smallestPoint > players.get(i).getPoint()) {
                smallestPoint = players.get(i).getPoint();
            }
        }
        for (int i = 0; i < players.size(); i ++) {
            Player player = this.players.get(i);
            if (player.getCalledCabo()) {
                if (smallestPoint != player.getPoint()) {
                    player.setScore(player.getPoint() + 10 + player.getScore());
                } else {
                    // get zero score this time
                }
            } else {
                player.setScore(player.getScore() + player.getPoint());

            }
            if (player.getScore() == 100) {
                player.setScore(50);
            }
            if (player.getScore() > 100) {
                this.terminate();
            }
        }
    }
    /**
     * @Debug: Simply check the implementation
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

    /**
     * Draw a card from `availableCards` pile
     * @return a card retrieve from `availableCard`
     */
    public Card getFirstCardFromAvailableCards() {
        Card card = this.availableCards.get(0);
        this.playedCards.add(card);
        this.availableCards.remove(0);
        return card;
    }

    /**
     * Retrieve the real card information by given index
     * @return The real card of given index in the card pile this game round
     */
    public Card getCardByIndex(int index) {
        return this.indexToCardMap.get(index);
    }
    /**
     * Retrieve the `index` of a card by given `card`
     * @return The index of given card in the card pile this game round
     */
    public int getIndexByCard(Card card) {
        return this.cardToIndexMap.get(card);
    }
    /**
     * Add discarded card by given card index
     * @param index the index which corresponds to the card
     */
    public void addDiscardedCard(int index) {
        this.discardedCards.add(this.indexToCardMap.get(index));
    }

    public ArrayList<Card> getAvailableCards() {
        return this.availableCards;
    }
    public ArrayList<Integer> getAvailableCardIndexes() {
        ArrayList<Integer> array = new ArrayList<>();

        for (int i = 0; i < availableCards.size(); i ++) {
            array.add(this.cardToIndexMap.get(availableCards.get(i)));
        }
        return array;
    }

    public ArrayList<Card> getDiscardedCards() {
        return this.discardedCards;
    }
    public ArrayList<Card> getPlayedCards() {
        return this.playedCards;
    }

    public HashMap<Integer, Card> getIndexToCardMap() { return this.indexToCardMap; };
    public HashMap<Card, Integer> getCardToIndexMap() { return this.cardToIndexMap; }

    /**
     * Distribute cards to all participated players;
     */
    public void distributeCardsAtBeginning() {
        players.forEach(player -> {
            for (int i = 0; i < DISTRIBUTION_CARD_NUMBER_AT_BEGINNING; i ++) {
                player.drawCard();
            }
        });
    }

}
