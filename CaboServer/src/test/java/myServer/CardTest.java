package myServer;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class CardTest {
    @Test
    public void testEqual() {
        Card card3 = new Card(1, "SPADE");
        Card card4 = new Card(1, "SPADE");
        assertEquals(card3, card4);

        Card card5 = new Card(7, "SPADE", "PEEK");
        Card card6 = new Card(7, "SPADE", "PEEK");
        assertEquals(card5, card6);
    }

    @Test
    public void testNotEqual() {
        Card card1 = new Card(1, "SPADE");
        Card card2 = new Card(2, "HEART");
        assertNotEquals(card1, card2);

        Card card3 = new Card(1, "SPADE");
        Card card4 = new Card(2, "SPADE");
        assertNotEquals(card3, card4);

        Card card5 = new Card(7, "SPADE", "PEEK");
        Card card6 = new Card(8, "HEART", "PEEK");
        assertNotEquals(card5, card6);

        Card card7 = new Card(1, "SPADE");
        Card card8 = new Card(1, "HEART");
        assertNotEquals(card7, card8);
    }
}
