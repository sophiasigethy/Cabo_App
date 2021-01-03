package myServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonCommandsTest {
    CardSuiteManager cardSuiteManager = null;
    Player tony = null;
    Player bob = null;

    @BeforeEach
    void setUp() {
        this.cardSuiteManager = new CardSuiteManager(false);
        this.tony = new Player(1, "Tony", this.cardSuiteManager);
        this.bob = new Player(2, "Bob", this.cardSuiteManager);
    }
    @Test
    public void testGameState() throws JsonProcessingException {
        tony.drawCard();
        tony.drawCard();
        tony.drawCard();
        tony.drawCard();
        JSONObject obj = JSON_commands.gameState(cardSuiteManager);
        System.out.println(obj);
    }
//    @Test
//    public void testCards() throws JsonProcessingException {
//        tony.drawCard();
//        tony.drawCard();
//        tony.drawCard();
//        // bob.drawCard();
//        tony.drawCard();
//
//        JSONObject obj = JSON_commands.cards(tony);
//        // System.out.println("obj: " + obj);
//
//        JSONObject expectedObj = new JSONObject("{\"cardIndexes\":[\"<Card: 0, SPADE>\",\"<Card: 0, CLUB>\",\"<Card: 1, SPADE>\",\"<Card: 1, CLUB>\"]}");
//        assertEquals(obj.toString(), expectedObj.toString());
//    }
//    @Test
//    public void testCardIndexes() throws JsonProcessingException {
//        tony.drawCard();
//        tony.drawCard();
//        tony.drawCard();
//        bob.drawCard();
//        tony.drawCard();
//
//        JSONObject obj = JSON_commands.cardIndexes(tony);
//        JSONObject expectedObj = new JSONObject("{\"cardIndexes\":[0,1,2,4]}");
//        assertEquals(obj.toString(), expectedObj.toString());
//    }
//
//    @Test
//    public void testPeek() throws JsonProcessingException {
//        tony.drawCard();
//        tony.drawCard();
//        tony.drawCard();
//        tony.drawCard();
//        tony.drawCard();
//        tony.drawCard();
//
//        int tonyCardIndex = tony.getCardIndexes().get(2);
//        JSONObject obj = JSON_commands.peek(tony, tonyCardIndex);
//        JSONObject expectedObj = new JSONObject("{\"peekedCard\":\"{\\\"type\\\":\\\"SPADE\\\",\\\"value\\\":1}\"}");
//
//        assertEquals(obj.toString(), expectedObj.toString());
//    }
//
//    @Test
//    public void testSpy() throws JsonProcessingException {
//        tony.drawCard();
//        int tonyCardIndex = tony.getCardIndexes().get(0);
//        JSONObject obj = JSON_commands.spy(bob, tony, tonyCardIndex);
//        JSONObject expectedObj = new JSONObject("{\"spiedCard\":\"{\\\"type\\\":\\\"SPADE\\\",\\\"value\\\":0}\"}");
//
//        assertEquals(obj.toString(), expectedObj.toString());
//    }
//
//    @Test
//    public void testSwapWithOtherPlayer() throws JsonProcessingException {
//        this.tony.drawCard(); // tonyId: 1, cardIndex: 0
//        this.bob.drawCard();  // bobId: 2,  cardIndex: 1
//
//        int tonyCardIndex = this.tony.getCardIndexes().get(0);
//        int bobCardIndex = this.bob.getCardIndexes().get(0);
//
//        // Tony swaps his with Bob
//        JSONObject obj = JSON_commands.swapWithOtherPlayer(tony, bob, tonyCardIndex, bobCardIndex);
//
//        // tonyId: 1, cardIndex: 1
//        // bobId:  2, cardIndex: 0
//        JSONObject expectedObj = new JSONObject("{\"1\":[1],\"2\":[0]}");
//        assertEquals(obj.toString(), expectedObj.toString());;
//    }
//
//    @Test
//    public void testSwapWithDiscardedCards() throws JsonProcessingException {
//        this.tony.drawCard();
//        this.tony.drawCard();
//
//        int discardedCardIndex = this.tony.getCardIndexes().get(0);
//        int tonyCardIndex = this.tony.getCardIndexes().get(1);
//        this.tony.discardCard(discardedCardIndex);
//
//        JSONObject obj = JSON_commands.swapWithDiscardedCards(this.cardSuiteManager, tony,
//            tonyCardIndex, discardedCardIndex);
//
//        JSONObject expectedObj = new JSONObject("{\"1\":[0],\"self\":[0],\"discardedCards\":[\"{\\\"type\\\":\\\"CLUB\\\",\\\"value\\\":0}\"]}");
//        assertEquals(obj.toString(), expectedObj.toString());;
//
//    }
}
