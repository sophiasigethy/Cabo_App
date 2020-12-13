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
    public void testCards() throws JsonProcessingException {
        tony.drawCard();
        tony.drawCard();
        tony.drawCard();
        tony.drawCard();

        JSONObject obj = JSON_commands.cards(tony);
        JSONObject expectedObj = new JSONObject("{\"cardIndexes\":[0,1,2,3]}");
        assertEquals(obj.toString(), expectedObj.toString());
    }

    @Test
    public void testPeek() throws JsonProcessingException {
        tony.drawCard();
        int tonyCardIndex = tony.getCardIndexes().get(0);
        JSONObject obj = JSON_commands.peek(tony, tonyCardIndex);
        JSONObject expectedObj = new JSONObject("{\"peekedCard\":\"{\\\"type\\\":\\\"SPADE\\\",\\\"value\\\":0}\"}");

        assertEquals(obj.toString(), expectedObj.toString());
    }

    @Test
    public void testSpy() throws JsonProcessingException {
        tony.drawCard();
        int tonyCardIndex = tony.getCardIndexes().get(0);
        JSONObject obj = JSON_commands.spy(bob, tony, tonyCardIndex);
        JSONObject expectedObj = new JSONObject("{\"spiedCard\":\"{\\\"type\\\":\\\"SPADE\\\",\\\"value\\\":0}\"}");

        assertEquals(obj.toString(), expectedObj.toString());
    }

    @Test
    public void testSwapWithOtherPlayer() throws JsonProcessingException {
        this.tony.drawCard();
        this.bob.drawCard();

        int tonyCardIndex = this.tony.getCardIndexes().get(0);
        int bobCardIndex = this.bob.getCardIndexes().get(0);
        Card tonyOriginalCard = this.cardSuiteManager.getCardByIndex(tonyCardIndex);
        Card bobOriginalCard = this.cardSuiteManager.getCardByIndex(bobCardIndex);

        // Tony swaps his with Bob
        JSONObject obj = JSON_commands.swapWithOtherPlayer(tony, bob, tonyCardIndex, bobCardIndex);

        JSONObject expectedObj = new JSONObject("{\"1\":[1],\"2\":[0]}");
        assertEquals(obj.toString(), expectedObj.toString());;
    }

    @Test
    public void testSwapWithDiscardedCards() throws JsonProcessingException {
        this.tony.drawCard();
        this.tony.drawCard();

        int discardedCardIndex = this.tony.getCardIndexes().get(0);
        int tonyCardIndex = this.tony.getCardIndexes().get(1);
        this.tony.discardCard(discardedCardIndex);

        JSONObject obj = JSON_commands.swapWithDiscardedCards(this.cardSuiteManager, tony,
            tonyCardIndex, discardedCardIndex);

        JSONObject expectedObj = new JSONObject("{\"1\":[0],\"discardedCards\":[\"{\\\"type\\\":\\\"CLUB\\\",\\\"value\\\":0}\"]}");
        assertEquals(obj.toString(), expectedObj.toString());;

    }
}
