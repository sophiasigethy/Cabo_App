package msp.group3.caboclient;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 this class contains methods to build all JSONobjects which are sent to the server during the game
 * these methods are used in the MainActivity class
 */

public class JSON_commands {

    /**
     * this methods return a JSONObject containing the user user name which the clients would like to have
     * @param text
     * @return
     * @throws JSONException
     */
    public static JSONObject Username(String text) throws JSONException {

        JSONObject jusername = new JSONObject();
        jusername.put("username", text);
        return jusername;
    }

    /**
     * this method returns a JSONObject containing a chat message the client would like to send
     * @param message
     * @return
     * @throws JSONException
     */
    public static JSONObject chatMessage(String message) throws JSONException {
        return new JSONObject().put("chatMessage", message);
    }

    public static JSONObject statusupdate(String status) throws JSONException {
        return new JSONObject().put("statusupdate", status);
    }

    public static JSONObject sendWelcomeMessage(String message) throws JSONException {
        return new JSONObject().put("welcomeMessage", message);
    }

    public static JSONObject startGameForAll(String message) throws JSONException {
        return new JSONObject().put("startGameForAll", message);
    }

    public static JSONObject askForInitialSettings(String message) throws JSONException {
        return new JSONObject().put("askForInitialSettings", message);
    }

    public static JSONObject sendMemorized(String message) throws JSONException {
        return new JSONObject().put("memorizedCards", message);
    }
    public static JSONObject sendPickCard(String message) throws JSONException {
        return new JSONObject().put("pickCard", message);

    }


    public static JSONObject swapPickedCardWithOwnCards(Card card) throws JSONException {
        Gson gson= new Gson();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("card", gson.toJson(card));
        jmsg.put("swapPickedCardWithOwnCards", jsubmsg);

        return jmsg;

    }

    public static JSONObject playPickedCard(Card card) throws JSONException {
        Gson gson= new Gson();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("card", gson.toJson(card));
        jmsg.put("playPickedCard", jsubmsg);

        return jmsg;
    }

    public static JSONObject useFunctionalityPeek(Card card) throws JSONException {
        Gson gson= new Gson();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("card", gson.toJson(card));
        jmsg.put("useFunctionalityPeek", jsubmsg);

        return jmsg;
    }
    public static JSONObject useFunctionalitySpy(Card card, Player player) throws JSONException {
        Gson gson= new Gson();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("card", gson.toJson(card));
        jsubmsg.put("spyedPlayer", gson.toJson(player));
        jmsg.put("useFunctionalitySpy", jsubmsg);

        return jmsg;
    }

    public static JSONObject useFunctionalitySwap(Card card1, Player player1, Card card2, Player player2) throws JSONException {
        Gson gson= new Gson();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("card1", gson.toJson(card1));
        jsubmsg.put("player1", gson.toJson(player1));
        jsubmsg.put("card2", gson.toJson(card2));
        jsubmsg.put("player2", gson.toJson(player2));
        jmsg.put("useFunctionalitySwap", jsubmsg);

        return jmsg;
    }

    public static JSONObject sendFinishMove(String message) throws JSONException {
        return new JSONObject().put("finishMove", message);
    }

    public static JSONObject sendCabo(String message) throws JSONException {
        return new JSONObject().put("cabo", message);
    }
}
