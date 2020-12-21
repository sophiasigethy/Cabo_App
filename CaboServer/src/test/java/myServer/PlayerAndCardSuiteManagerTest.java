package myServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class PlayerAndCardSuiteManagerTest {
    CardSuiteManager cardSuiteManager = null;
    Player tony = null;
    Player bob = null;
    @BeforeEach
    void setUp() {
        this.cardSuiteManager = new CardSuiteManager(false);
        this.tony = new Player(1, "Tony", this.cardSuiteManager);
        this.bob = new Player(2, "Bob", this.cardSuiteManager);
    }

    @AfterEach
    void tearDown() {}


    @Test
    public void testDrawCard() {
        this.tony.drawCard();
        ArrayList<Card> cards = this.tony.getCards();
        assertEquals(cards.size(), 1);

        Card drawnCard = this.cardSuiteManager.getPlayedCards().get(0);
        Card tonyCard = cards.get(0);
        assertEquals(drawnCard, tonyCard);

        Card topCardOnAvailableCards = this.cardSuiteManager.getAvailableCards().get(0);
        assertNotEquals(tonyCard, topCardOnAvailableCards);
    }

    @Test
    public void testDiscardCard() {
        this.tony.drawCard();
        Card card = this.tony.getCards().get(0);
        ArrayList<Card> cardsToDiscard = new ArrayList<>();
        cardsToDiscard.add(card);
        this.tony.tryDiscardCards(cardsToDiscard);
        ArrayList<Card> cards = this.tony.getCards();

        assertEquals(cards.size(), 0);
        ArrayList<Card> discardedCards = this.cardSuiteManager.getDiscardedCards();
        assertEquals(discardedCards.size(), 1);
        assertEquals(discardedCards.get(0), card);
    }

    @Test
    public void testSwapWithOtherPlayer() {
        this.tony.drawCard();
        this.bob.drawCard();

        Card tonyCard = this.tony.getCards().get(0);
        Card bobCard = this.bob.getCards().get(0);

        this.tony.swapWithOtherPlayer(this.bob, tonyCard, bobCard);

        Card tonyNewCard = this.tony.getCards().get(0);
        Card bobNewCard = this.bob.getCards().get(0);

        assertEquals(tonyCard, bobNewCard);
        assertEquals(bobCard, tonyNewCard);
    }
    @Test
    public void testSwapWithAvailableCards() {
        this.tony.drawCard();

        Card tonyCard = this.tony.getCards().get(0);
        Card availableCard = cardSuiteManager.getAvailableCards().get(0);
        this.tony.swapWithAvailableCards(tonyCard, availableCard);

        assertEquals(this.tony.getCards().get(0), availableCard);
        assertEquals(cardSuiteManager.getAvailableCards().get(0), tonyCard);

        // Since Tony swaps his card with available cards, now the `tonyCardIndex` goes into `availableCards`
        // and `availableCardIndex` goes into `playedCards`
        assertEquals(availableCard,
            this.cardSuiteManager.getPlayedCards().get(0));
    }
    @Test
    public void testSwapWithDiscardedCards() {
        this.tony.drawCard();
        this.tony.drawCard();

        Card discardedCard = this.tony.getCards().get(0);
        Card tonyCard = this.tony.getCards().get(1);
        ArrayList<Card> discardedCards = new ArrayList<>();
        discardedCards.add(discardedCard);
        this.tony.tryDiscardCards(discardedCards);
        this.tony.swapWithDiscardedCards(tonyCard, discardedCard);
        assertEquals(this.tony.getCards().size(), 1);
        assertEquals(this.tony.getCards().get(0), discardedCard);
        assertEquals(this.cardSuiteManager.getDiscardedCards().get(0), tonyCard);
    }

    @Test
    public void testGetPoint() {
        this.tony.drawCard();   // 0
        this.tony.drawCard();   // 0
        this.tony.drawCard();   // 1
        this.tony.drawCard();   // 1

        assertEquals(this.tony.getPoint(), 2);
    }

    @Test
    public void testCallCaboSuccess() {
        this.cardSuiteManager.distributeCardsAtBeginning();
        if (this.tony.getPoint() <= this.bob.getPoint()) {
            this.tony.callCabo();
        } else {
            this.bob.callCabo();
        }

        this.cardSuiteManager.calcScores();

        if (this.tony.getPoint() < this.bob.getPoint()) {
            assertEquals(this.tony.getScore(), 0);
            assertEquals(this.bob.getScore(), this.bob.getPoint());
        } else {
            assertEquals(this.bob.getScore(), 0);
            assertEquals(this.tony.getScore(), this.tony.getPoint());
        }
    }

    @Test
    public void testCallCaboFailed() {
        this.cardSuiteManager.distributeCardsAtBeginning();
        if (this.tony.getPoint() > this.bob.getPoint()) {
            this.tony.callCabo();
        } else {
            this.bob.callCabo();
        }

        this.cardSuiteManager.calcScores();

        if (this.tony.getPoint() > this.bob.getPoint()) {
            assertEquals(this.tony.getScore(), this.tony.getPoint() * 2);
            assertEquals(this.bob.getScore(), this.bob.getPoint());
        } else {
            assertEquals(this.bob.getScore(), this.bob.getPoint() * 2);
            assertEquals(this.tony.getScore(), this.tony.getPoint());
        }
    }
}
