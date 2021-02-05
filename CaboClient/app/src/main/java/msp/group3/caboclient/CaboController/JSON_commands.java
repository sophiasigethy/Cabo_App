package msp.group3.caboclient.CaboController;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import msp.group3.caboclient.CaboModel.Card;
import msp.group3.caboclient.CaboModel.Player;

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
    public static JSONObject chatMessage(String message, Player sender) throws JSONException {

        Gson gson= new Gson();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("sender", gson.toJson(sender));
        jsubmsg.put("message", message);
        jmsg.put("chatMessage", jsubmsg);

        return jmsg;
    }

    public static JSONObject statusupdate(String status) throws JSONException {
        return new JSONObject().put("statusupdate", status);
    }

    public static JSONObject sendWelcomeMessage(String message) throws JSONException {
        return new JSONObject().put("welcomeMessage", message);
    }

    public static JSONObject sendUserLogin(Player player) throws JSONException {
        Gson gson= new Gson();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("sender", gson.toJson(player));
        jmsg.put("sendUserLogin", jsubmsg);

        return jmsg;
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

    public static JSONObject playPickedCard() throws JSONException {
        Gson gson= new Gson();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

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

    public static JSONObject sendPicture(String picture) throws JSONException {
        return new JSONObject().put("picture", picture);
    }
    public static JSONObject sendSmiley(String smiley) throws JSONException {
        return new JSONObject().put("smiley",smiley);
    }

    public static JSONObject sendFriendRequest(Player sender, Player receiver) throws JSONException {
        Gson gson= new Gson();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("sender", gson.toJson(sender));
        jsubmsg.put("receiver", gson.toJson(receiver));
        jmsg.put("sendFriendRequest", jsubmsg);

        return jmsg;
    }

    public static JSONObject sendFriendRequestAccepted(Player sender, Player receiver) throws JSONException {
        Gson gson= new Gson();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("sender", gson.toJson(sender));
        jsubmsg.put("receiver", gson.toJson(receiver));
        jmsg.put("sendFriendAccepted", jsubmsg);

        return jmsg;
    }

    public static JSONObject sendPartyRequest(Player sender, Player receiver) throws JSONException {
        Gson gson= new Gson();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("sender", gson.toJson(sender));
        jsubmsg.put("receiver", gson.toJson(receiver));
        jmsg.put("partyrequest", jsubmsg);

        return jmsg;

    }

    public static JSONObject sendPartyAccepted(Player sender, Player receiver) throws JSONException {
        Gson gson= new Gson();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("sender", gson.toJson(sender));
        jsubmsg.put("receiver", gson.toJson(receiver));
        jmsg.put("partyaccepted", jsubmsg);

        return jmsg;
    }

    public static JSONObject sendPartyLeft(Player sender, Player receiver) throws JSONException {
        Gson gson= new Gson();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("sender", gson.toJson(sender));
        jsubmsg.put("receiver", gson.toJson(receiver));
        jmsg.put("partyleft", jsubmsg);

        return jmsg;
    }

    public static JSONObject sendLogout() throws JSONException {
        return new JSONObject().put("logout", "");
    }

    public static JSONObject sendStartNewGame(ArrayList<String> players) throws JSONException {
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        Gson gson= new Gson();

        jsubmsg.put("players",  gson.toJson(players));
        jmsg.put("startNewGame", jsubmsg);

        return jmsg;
    }
    public static JSONObject noAccount() throws JSONException {
        return new JSONObject().put("noAccount", "noAccount");
    }
    public static JSONObject askForStart() throws JSONException {
        return new JSONObject().put("askForStart", "");
    }

    public static JSONObject leaveGame() throws JSONException {
        return new JSONObject().put("leaveGame", "");
    }
    public static JSONObject leaveWaitingRoom() throws JSONException {
        return new JSONObject().put("leaveWaitingRoom", "");
    }

    public static JSONObject sendMaxPoints(int maxpoints) throws JSONException {
        return new JSONObject().put("maxPoints", maxpoints);
    }

    public static JSONObject getOnlinestatusOfNewFriend(String nick) throws JSONException {
        return new JSONObject().put("onlineStatusOfNewFriend", nick);
    }
}
