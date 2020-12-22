package myServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * this class contains methods to build all JSONobjects which are sent to the clients during the game
 * these methods are used in the class gamestate
 */
public class JSON_commands {

    /**
     * this is sent after client connected to server, but there are already too many players
     * @param msg
     * @return
     * @throws JSONException
     */
    public static JSONObject notAccepted(String msg) throws JSONException {

        return new JSONObject().put("notAccepted", msg);
    }

    public static JSONObject accepted(String msg) throws JSONException {

        return new JSONObject().put("accepted", msg);
    }
    /**
     * this is sent to the client when client can participate in the game
     * @param text
     * @return
     * @throws JSONException
     */
    public static JSONObject Hallo(String text) throws JSONException {

        JSONObject jhallo = new JSONObject();

        jhallo.put("Hallo", text);

        return jhallo;
    }


    /**
     * this method returns a serialized player object
     * it is sent after client had been added to the game
     * @param player
     * @return
     * @throws JSONException
     * @throws JsonProcessingException
     */
    public static JSONObject Welcome(Player player) throws JSONException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("Player", objectMapper.writeValueAsString(player));
        jmsg.put("Welcome", jsubmsg);

        return jmsg;
    }

    /**
     * this method returns a serialized player object
     * @return
     * @throws JSONException
     */
    public static JSONObject newPlayer(Player player) throws JSONException, JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("player", objectMapper.writeValueAsString(player));
        jmsg.put("newPlayer", jsubmsg);

        return jmsg;
    }

    /**
     * this is sent to the client when he chooses a user name which is already in use
     * @param name
     * @return
     * @throws JSONException
     */
    public static JSONObject usernameInUse(String name) throws JSONException {

        return new JSONObject().put("usernameInUse", name);
    }

    /**
     * this is sent when the state of the game changes
     * eg from matching to gamestart
     * @param status
     * @return
     * @throws JSONException
     */
    public static JSONObject statusupdateServer(String status) throws JSONException {

        return new JSONObject().put("statusupdateServer", status);
    }

    /**
     * players which have been connected before are sent
     * @param player
     * @return
     * @throws JsonProcessingException
     */
    public static JSONObject sendOtherPlayer(Player player) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("player", objectMapper.writeValueAsString(player));
        jmsg.put("sendOtherPlayer", jsubmsg);

        return jmsg;

    }

    public static JSONObject cardIndexes(Player self) throws JsonProcessingException {
        JSONObject jmsg = new JSONObject();
        ArrayList<Integer> cardIndexes = self.getCardIndexes();

        JSONArray array = new JSONArray();
        for(int i = 0; i < cardIndexes.size(); i++) {
            array.put(cardIndexes.get(i));
        }
        jmsg.put("cardIndexes", array);
        return jmsg;
    }
    public static JSONObject cards(Player self) throws JsonProcessingException {
        JSONObject jmsg = new JSONObject();
        ArrayList<Card> cards = self.getMyCards();

        JSONArray array = new JSONArray();
        for(int i = 0; i < cards.size(); i++) {
            array.put(cards.get(i));
        }
        jmsg.put("cardIndexes", array);
        return jmsg;
    }

    public static JSONObject peek(Player self, int index) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        Card card = self.peek(index);

        jmsg.put("peekedCard", objectMapper.writeValueAsString(card));
        return jmsg;
    }

    public static JSONObject spy(Player self, Player other, int index) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        Card card = self.spy(other, index);

        jmsg.put("spiedCard", objectMapper.writeValueAsString(card));
        return jmsg;
    }

    public static JSONObject swapWithOtherPlayer(Player self, Player other, int selfIndex, int otherIndex) throws JsonProcessingException {
        JSONObject jmsg = new JSONObject();
        self.swapWithOtherPlayer(other, selfIndex, otherIndex);

        ArrayList<Integer> cardIndexes1 = self.getCardIndexes();
        ArrayList<Integer> cardIndexes2 = other.getCardIndexes();

        JSONArray arr1 = new JSONArray();
        for (int i = 0; i < cardIndexes1.size(); i++) {
            arr1.put(cardIndexes1.get(i));
        }
        JSONArray arr2 = new JSONArray();
        for (int i = 0; i < cardIndexes2.size(); i++) {
            arr2.put(cardIndexes2.get(i));
        }
        jmsg.put(Integer.toString(self.getId()), arr1);
        jmsg.put(Integer.toString(other.getId()), arr2);
        return jmsg;
    }
    public static JSONObject swapWithOtherPlayer(Player self, Player other, Card selfCard, Card otherCard) throws JsonProcessingException {
        JSONObject jmsg = new JSONObject();
        self.swapWithOtherPlayer(other, selfCard, otherCard);

        ArrayList<Card> cards1 = self.getMyCards();
        ArrayList<Card> cards2 = other.getMyCards();

        JSONArray arr1 = new JSONArray();
        for (int i = 0; i < cards1.size(); i++) {
            arr1.put(cards1.get(i));
        }
        JSONArray arr2 = new JSONArray();
        for (int i = 0; i < cards2.size(); i++) {
            arr2.put(cards2.get(i));
        }
        jmsg.put(Integer.toString(self.getId()), arr1);
        jmsg.put(Integer.toString(other.getId()), arr2);
        return jmsg;
    }
    public static JSONObject swapWithAvailableCards(CardSuiteManager mgr, Player self,
                                                    int selfIndex, int otherIndex) throws JsonProcessingException {
        JSONObject jmsg = new JSONObject();
        self.swapWithAvailableCards(selfIndex, otherIndex);

        ArrayList<Integer> cardIndexes = self.getCardIndexes();

        JSONArray arr1 = new JSONArray();
        for (int i = 0; i < cardIndexes.size(); i++) {
            arr1.put(cardIndexes.get(i));
        }

        ArrayList<Integer> availableCardIndexes = mgr.getAvailableCardIndexes();
        JSONArray arr2 = new JSONArray();
        for (int i = 0; i < availableCardIndexes.size(); i++) {
            arr1.put(availableCardIndexes.get(i));
        }
        jmsg.put(Integer.toString(self.getId()), arr1);
        jmsg.put("availableCardIndexes", arr2);
        jmsg.put("self", arr1);

        return jmsg;
    }

    public static JSONObject swapWithAvailableCards(CardSuiteManager mgr, Player self,
                                                    Card selfCard, Card otherCard) throws JsonProcessingException {
        JSONObject jmsg = new JSONObject();
        self.swapWithAvailableCards(selfCard, otherCard);

        ArrayList<Card> cards = self.getMyCards();

        JSONArray arr1 = new JSONArray();
        for (int i = 0; i < cards.size(); i++) {
            arr1.put(cards.get(i));
        }

        ArrayList<Card> availableCards = mgr.getAvailableCards();
        JSONArray arr2 = new JSONArray();
        for (int i = 0; i < availableCards.size(); i++) {
            arr1.put(availableCards.get(i));
        }
        jmsg.put(Integer.toString(self.getId()), arr1);
        jmsg.put("availableCards", arr2);
        jmsg.put("self", arr1);

        return jmsg;
    }

    public static JSONObject swapWithDiscardedCards(CardSuiteManager mgr, Player self,
                                                   int selfIndex, int otherIndex) throws JsonProcessingException {
        JSONObject jmsg = new JSONObject();
        self.swapWithDiscardedCards(selfIndex, otherIndex);

        ArrayList<Integer> cardIndexes = self.getCardIndexes();

        JSONArray arr1 = new JSONArray();
        for (int i = 0; i < cardIndexes.size(); i++) {
            arr1.put(cardIndexes.get(i));
        }

        ArrayList<Card> discardedCards = mgr.getDiscardedCards();
        JSONArray arr2 = new JSONArray();
        for (int i = 0; i < discardedCards.size(); i++) {
            ObjectMapper mapper = new ObjectMapper();
            arr2.put(mapper.writeValueAsString(discardedCards.get(i)));
        }
        jmsg.put(Integer.toString(self.getId()), arr1);
        jmsg.put("discardedCards", arr2);
        jmsg.put("self", arr1);

        return jmsg;
    }

    public static JSONObject swapWithDiscardedCards(CardSuiteManager mgr, Player self,
                                                    Card selfCard, Card otherCard) throws JsonProcessingException {
        JSONObject jmsg = new JSONObject();
        self.swapWithDiscardedCards(selfCard, otherCard);

        ArrayList<Card> cards = self.getMyCards();

        JSONArray arr1 = new JSONArray();
        for (int i = 0; i < cards.size(); i++) {
            arr1.put(cards.get(i));
        }

        ArrayList<Card> discardedCards = mgr.getDiscardedCards();
        JSONArray arr2 = new JSONArray();
        for (int i = 0; i < discardedCards.size(); i++) {
            ObjectMapper mapper = new ObjectMapper();
            arr2.put(mapper.writeValueAsString(discardedCards.get(i)));
        }
        jmsg.put(Integer.toString(self.getId()), arr1);
        jmsg.put("discardedCards", arr2);
        jmsg.put("self", arr1);
        return jmsg;
    }

    public static JSONObject sendInitialCards(Player player) throws JsonProcessingException {
        player.getMyCards();
        ObjectMapper objectMapper = new ObjectMapper();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("player", objectMapper.writeValueAsString(player));
        jmsg.put("initialCards", jsubmsg);

        return jmsg;
    }

    public static JSONObject statusupdatePlayer(Player player) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("player", objectMapper.writeValueAsString(player));
        jmsg.put("statusupdatePlayer", jsubmsg);

        return jmsg;
    }

    public static JSONObject sendNextPlayer(int id) throws JSONException {
        return new JSONObject().put("nextPlayer", id);
    }

    public static JSONObject startGame(String text) throws JSONException {
        return new JSONObject().put("startGame", text);
    }

    public static JSONObject removePlayer(Player player) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("player", objectMapper.writeValueAsString(player));
        jmsg.put("removePlayer", jsubmsg);

        return jmsg;
    }

}
