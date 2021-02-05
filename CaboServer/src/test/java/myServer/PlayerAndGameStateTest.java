package myServer;

import myServer.Controller.Gamestate;
import myServer.Model.Card;
import myServer.Model.Player;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class PlayerAndGameStateTest {
    Gamestate gamestate = null;
    Player tony = null;
    Player bob = null;
    @BeforeEach
    void setUp() {
        this.gamestate = new Gamestate(null,false);
         this.tony = new Player(1, "Tony", this.gamestate);
        this.bob = new Player(2, "Bob", this.gamestate);
    }

    @AfterEach
    void tearDown() {}

    @Test
    public void testDrawCard() {
        this.tony.drawCard();
        ArrayList<Card> cards = this.tony.getCards();
        assertEquals(cards.size(),1);
        System.out.println("tony"+tony.getCards().get(0));

        Card drawnCard = this.gamestate.getPlayedCards().get(0);
        Card tonyCard = cards.get(0);
        assertEquals(drawnCard, tonyCard);
        System.out.println("drawnCard"+drawnCard);

        Card topCardOnAvailableCards = this.gamestate.getAvailableCards().get(0);
        //Does tony really draw the first Card from AvailableCards?
        assertNotEquals(tonyCard, topCardOnAvailableCards);
        System.out.println("topCardOnAvailableCards"+topCardOnAvailableCards);
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
        ArrayList<Card> discardedCards = this.gamestate.getDiscardedCards();
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
        System.out.println("tonyCard"+tonyCard);
        System.out.println("bobCard"+bobCard);
        System.out.println("swap with other");
        System.out.println("tonyNewCard"+tonyCard);
        System.out.println("bobNewCard"+bobCard);
    }
    @Test
    public void testSwapWithAvailableCards() {
        this.tony.drawCard();

        Card tonyCard = this.tony.getCards().get(0);
        Card availableCard = gamestate.getAvailableCards().get(0);
        this.tony.swapWithAvailableCards(tonyCard, availableCard);
        System.out.println("tonyCard"+tonyCard);
        System.out.println("availableCard"+availableCard);

        assertEquals(this.tony.getCards().get(0), availableCard);
        assertEquals(gamestate.getAvailableCards().get(0), tonyCard);
        System.out.println("after swap with availableCards");
        System.out.println("tonyCard"+tony.getCards().get(0));
        System.out.println("availableCard"+gamestate.getAvailableCards().get(0));
        // Since Tony swaps his card with available cards, now the `tonyCardIndex` goes into `availableCards`
        // and `availableCardIndex` goes into `playedCards`
        assertEquals(availableCard,
            this.gamestate.getPlayedCards().get(0));
    }
    @Test
    public void testSwapWithDiscardedCards() {
        this.tony.drawCard();
        this.tony.drawCard();

        Card discardedCard = this.tony.getCards().get(0);
        Card tonyCard = this.tony.getCards().get(1);
        System.out.println("discardedCard"+discardedCard);
        System.out.println("tonyCard"+tonyCard);
        ArrayList<Card> discardedCards = new ArrayList<>();
        discardedCards.add(discardedCard);
        this.tony.tryDiscardCards(discardedCards);
        this.tony.swapWithDiscardedCards(tonyCard, discardedCard);
        assertEquals(this.tony.getCards().size(), 1);
        assertEquals(this.tony.getCards().get(0), discardedCard);
        assertEquals(this.gamestate.getDiscardedCards().get(0), tonyCard);
        System.out.println("after swap with DiscardedCards");
        System.out.println("discardedCard"+gamestate.getDiscardedCards().get(0));
        System.out.println("tonyCard"+tony.getCards().get(0));
    }

    @Test
    public void testGetPoint() {
        this.tony.drawCard();   // 0
        this.tony.drawCard();   // 0
        this.tony.drawCard();   // 1
        this.tony.drawCard();   // 1
        assertEquals(this.tony.calculatePoints(), 2);
        for(int i=0;i<tony.getCards().size();i++){
            System.out.println("each card is"+tony.getCards().get(i));
        }
        System.out.println("Get Point is" + tony.calculatePoints());
        assertEquals(this.tony.calculatePoints(), 2);
    }

    @Test
    public void testCallCaboSuccess() {
        this.gamestate.distributeCardsAtBeginning();
        if (this.tony.calculatePoints() <= this.bob.calculatePoints()) {
            this.tony.callCabo();
        } else {
            this.bob.callCabo();
        }

        this.gamestate.calcScores();

        if (this.tony.calculatePoints() < this.bob.calculatePoints()) {
            assertEquals(this.tony.getScore(), 0);
            assertEquals(this.bob.getScore(), this.bob.calculatePoints());
        } else {
            assertEquals(this.bob.getScore(), 0);
            assertEquals(this.tony.getScore(), this.tony.calculatePoints());
        }
    }

    @Test
    public void testCallCaboFailed() {
        this.gamestate.distributeCardsAtBeginning();
        if (this.tony.calculatePoints() > this.bob.calculatePoints()) {
            this.tony.callCabo();
        } else {
            this.bob.callCabo();
        }

        this.gamestate.calcScores();

        if (this.tony.calculatePoints() > this.bob.calculatePoints()) {
            assertEquals(this.tony.getScore(), this.tony.calculatePoints() * 2);
            assertEquals(this.bob.getScore(), this.bob.calculatePoints());
        } else {
            assertEquals(this.bob.getScore(), this.bob.calculatePoints() * 2);
            assertEquals(this.tony.getScore(), this.tony.calculatePoints());
        }
    }
}
