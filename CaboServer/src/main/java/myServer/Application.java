package myServer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    /**
     * this class must be started to start the server
     * @param args
     */
    public static void main(String[] args) {
        testInitializedPlayers();
        testDrawCards();
        testDiscardCards();
        testSpyCards();
        testSwapWithOtherPlayer();
        testSwapWithAvailableCards();
        testSwapWithDiscadedCards();
        System.out.println("================== Finished Simple Tests ===================");
        // SpringApplication.run(Application.class, args);
    }

    public static void testInitializedPlayers() {
        System.out.println("=================== Test Initialized Players' Status =================");
        Gamestate gs = new Gamestate();
        gs.players.put("Alice", new Player(1, "Alice", gs.cardSuiteMgr));
        gs.players.put("Bob", new Player(2, "Bob", gs.cardSuiteMgr));
        gs.players.put("Charile", new Player(3, "Charile", gs.cardSuiteMgr));
        gs.players.put("Tony", new Player(4, "Tony", gs.cardSuiteMgr));
        gs.distributeCardAtBeginning();
        gs.players.forEach((k, v) -> {
            v.debug();
        });
        gs.cardSuiteMgr.debug();
    }

    public static void testDrawCards() {
        System.out.println("=================== Test Draw Cards ===================");

        Gamestate gs = new Gamestate();
        gs.players.put("Tony", new Player(1, "Tony", gs.cardSuiteMgr));
        gs.players.get("Tony").drawCard();

        gs.players.get("Tony").debug();
        gs.cardSuiteMgr.debug();
    }

    public static void testDiscardCards() {
        System.out.println("=================== Test Discard Cards ===================");

        Gamestate gs = new Gamestate();
        gs.players.put("Tony", new Player(1, "Tony", gs.cardSuiteMgr));
        System.out.println("Tony draws: ");
        gs.players.get("Tony").drawCard();
        gs.players.get("Tony").debug();
        System.out.println("Tony draw: ");
        gs.players.get("Tony").discardCard(gs.players.get("Tony").getCardIndexes().get(0));
        gs.players.get("Tony").debug();

        gs.cardSuiteMgr.debug();
    }
    public static void testSpyCards() {
        System.out.println("=========== Test SPY Card ===============");
        Gamestate gs = new Gamestate();
        gs.players.put("Bob", new Player(1, "Bob", gs.cardSuiteMgr));
        gs.players.put("Tony", new Player(1, "Tony", gs.cardSuiteMgr));
        gs.players.get("Bob").drawCard();
        int bobIndex = gs.players.get("Bob").getCardIndexes().get(0);

        Card spiedCard = gs.players.get("Tony").spy(gs.players.get("Bob"), bobIndex);
        gs.players.get("Bob").debug();
        System.out.println("Tony spies Bob's card at index "+ bobIndex + "， and the card is： " + spiedCard);
    }

    public static void testSwapWithOtherPlayer() {
        System.out.println("=========== Test SWAP With Other Player ===============");
        Gamestate gs = new Gamestate();
        gs.players.put("Bob", new Player(1, "Bob", gs.cardSuiteMgr));
        gs.players.put("Tony", new Player(1, "Tony", gs.cardSuiteMgr));

        gs.players.get("Bob").drawCard();
        gs.players.get("Bob").drawCard();

        gs.players.get("Tony").drawCard();
        gs.players.get("Tony").drawCard();
        System.out.println("Before swap: ");
        gs.players.get("Bob").debug();
        gs.players.get("Tony").debug();

        int bobIndex1 = gs.players.get("Bob").getCardIndexes().get(0);
        int bobIndex2 = gs.players.get("Bob").getCardIndexes().get(1);

        int tonyIndex1 = gs.players.get("Tony").getCardIndexes().get(0);
        int tonyIndex2 = gs.players.get("Tony").getCardIndexes().get(1);

        gs.players.get("Tony").swapWithOtherPlayer(gs.players.get("Bob"),
                tonyIndex1, tonyIndex2, bobIndex1, bobIndex2);
        System.out.println("After swap: ");
        gs.players.get("Bob").debug();
        gs.players.get("Tony").debug();

    }

    public static void testSwapWithAvailableCards() {
        System.out.println("=========== Test SWAP With Available Cards ===============");

        Gamestate gs = new Gamestate();
        gs.players.put("Tony", new Player(1, "Tony", gs.cardSuiteMgr));

        gs.players.get("Tony").drawCard();
        gs.players.get("Tony").drawCard();
        System.out.println("Before Swap: ");
        gs.players.get("Tony").debug();
        gs.cardSuiteMgr.debug();

        int tonyIndex1 = gs.players.get("Tony").getCardIndexes().get(0);
        int tonyIndex2 = gs.players.get("Tony").getCardIndexes().get(1);

        int cardIndex1 = gs.cardSuiteMgr.getCardToIndexMap().get(gs.cardSuiteMgr.getAvailableCards().get(0));
        int cardIndex2 = gs.cardSuiteMgr.getCardToIndexMap().get(gs.cardSuiteMgr.getAvailableCards().get(1));

        gs.players.get("Tony").swapWithAvailableCards(tonyIndex1, tonyIndex2, cardIndex1, cardIndex2);
        System.out.println("After Swap: ");
        gs.players.get("Tony").debug();
        gs.cardSuiteMgr.debug();
    }

    public static void testSwapWithDiscadedCards() {
        System.out.println("=========== Test SWAP With Discarded Cards ===============");


        Gamestate gs = new Gamestate();
        gs.players.put("Tony", new Player(1, "Tony", gs.cardSuiteMgr));

        gs.players.get("Tony").drawCard();
        gs.players.get("Tony").drawCard();
        System.out.println("Before Discard: ");
        gs.players.get("Tony").debug();
        gs.cardSuiteMgr.debug();

        gs.players.get("Tony").discardCard(gs.players.get("Tony").getCardIndexes().get(0));
        gs.players.get("Tony").discardCard(gs.players.get("Tony").getCardIndexes().get(0));
        System.out.println("After Discard: ");
        gs.players.get("Tony").debug();
        gs.cardSuiteMgr.debug();

        gs.players.get("Tony").drawCard();
        gs.players.get("Tony").drawCard();
        gs.players.get("Tony").debug();
        gs.cardSuiteMgr.debug();

        int tonyIndex1 = gs.players.get("Tony").getCardIndexes().get(0);
        int tonyIndex2 = gs.players.get("Tony").getCardIndexes().get(1);

        int cardIndex1 = gs.cardSuiteMgr.getCardToIndexMap().get(gs.cardSuiteMgr.getDiscardedCards().get(0));
        int cardIndex2 = gs.cardSuiteMgr.getCardToIndexMap().get(gs.cardSuiteMgr.getDiscardedCards().get(1));

        gs.players.get("Tony").swapWithDiscardedCards(tonyIndex1, tonyIndex2, cardIndex1, cardIndex2);
        System.out.println("After Swap: ");
        gs.players.get("Tony").debug();
        gs.cardSuiteMgr.debug();
    }
}


