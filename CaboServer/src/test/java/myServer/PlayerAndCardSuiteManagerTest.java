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
        ArrayList<Integer> cards = this.tony.getCardIndexes();
        assertEquals(cards.size(), 1);

        Card drawnCard = this.cardSuiteManager.getPlayedCards().get(0);
        Card tonyCard = this.cardSuiteManager.getCardByIndex(cards.get(0));
        assertEquals(drawnCard, tonyCard);

        Card topCardOnAvailableCards = this.cardSuiteManager.getAvailableCards().get(0);
        assertNotEquals(tonyCard, topCardOnAvailableCards);
    }

    @Test
    public void testDiscardCard() {
        this.tony.drawCard();
        Card card = this.cardSuiteManager.getCardByIndex(this.tony.getCardIndexes().get(0));

        this.tony.discardCard(0);
        ArrayList<Integer>  cards = this.tony.getCardIndexes();

        assertEquals(cards.size(), 0);
        ArrayList<Card> discardedCards = this.cardSuiteManager.getDiscardedCards();
        assertEquals(discardedCards.size(), 1);
        assertEquals(discardedCards.get(0), card);
    }

    @Test
    public void testSpyCards() {
        this.bob.drawCard();
        int bobCardIndex = this.bob.getCardIndexes().get(0);
        Card spiedCard = this.tony.spy(this.bob, bobCardIndex);
        Card bobCard = this.cardSuiteManager.getCardByIndex(bobCardIndex);
        assertEquals(spiedCard, bobCard);
    }

    @Test
    public void testSwapWithOtherPlayer() {
        this.tony.drawCard();
        this.bob.drawCard();

        int tonyCardIndex = this.tony.getCardIndexes().get(0);
        int bobCardIndex = this.bob.getCardIndexes().get(0);
        Card tonyOriginalCard = this.cardSuiteManager.getCardByIndex(tonyCardIndex);
        Card bobOriginalCard = this.cardSuiteManager.getCardByIndex(bobCardIndex);

        this.tony.swapWithOtherPlayer(this.bob, tonyCardIndex, bobCardIndex);

        int tonyNewCardIndex = this.tony.getCardIndexes().get(0);
        int bobNewCardIndex = this.bob.getCardIndexes().get(0);

        Card tonyNewCard = this.cardSuiteManager.getCardByIndex(tonyNewCardIndex);
        Card bobNewCard = this.cardSuiteManager.getCardByIndex(bobNewCardIndex);

        assertEquals(tonyOriginalCard, bobNewCard);
        assertEquals(bobOriginalCard, tonyNewCard);
    }
    @Test
    public void testSwapWithAvailableCards() {
        this.tony.drawCard();

        int tonyCardIndex = this.tony.getCardIndexes().get(0);
        int availableCardIndex = this.cardSuiteManager.getIndexByCard(cardSuiteManager.getAvailableCards().get(0));
        this.tony.swapWithAvailableCards(tonyCardIndex, availableCardIndex);

        assertEquals(this.tony.getCardIndexes().get(0), availableCardIndex);
        assertEquals(cardSuiteManager.getAvailableCardIndexes().get(0), tonyCardIndex);

        // Since Tony swaps his card with available cards, now the `tonyCardIndex` goes into `availableCards`
        // and `availableCardIndex` goes into `playedCards`
        assertEquals(this.cardSuiteManager.getCardByIndex(availableCardIndex),
            this.cardSuiteManager.getPlayedCards().get(0));
    }
    @Test
    public void testSwapWithDiscardedCards() {
        this.tony.drawCard();
        this.tony.drawCard();

        int discardedCardIndex = this.tony.getCardIndexes().get(0);
        int tonyCardIndex = this.tony.getCardIndexes().get(1);

        this.tony.discardCard(discardedCardIndex);
        this.tony.swapWithDiscardedCards(tonyCardIndex, discardedCardIndex);
        assertEquals(this.tony.getCardIndexes().size(), 1);
        assertEquals(this.tony.getCardIndexes().get(0), discardedCardIndex);
        assertEquals(this.cardSuiteManager.getIndexByCard(this.cardSuiteManager.getDiscardedCards().get(0)), tonyCardIndex);
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
            assertEquals(this.tony.getScore(), this.tony.getPoint() + 10);
            assertEquals(this.bob.getScore(), this.bob.getPoint());
        } else {
            assertEquals(this.bob.getScore(), this.bob.getPoint() + 10);
            assertEquals(this.tony.getScore(), this.tony.getPoint());
        }
    }
}
