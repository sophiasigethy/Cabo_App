package myServer.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import myServer.Model.Card;
import myServer.Model.Player;
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
     *
     * @param msg
     * @return
     * @throws JSONException
     */
    public static JSONObject connectionNotAccepted(String msg) throws JSONException {
        return new JSONObject().put("connectionNotAccepted", msg);
    }

    public static JSONObject connectionAccepted(String msg) throws JSONException {
        return new JSONObject().put("connectionAccepted", msg);
    }

    /**
     * this is sent to the client when client can participate in the game
     *
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
     *
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
     *
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
     *
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
     *
     * @param status
     * @return
     * @throws JSONException
     */
    public static JSONObject statusupdateServer(String status) throws JSONException {
        return new JSONObject().put("statusupdateServer", status);
    }

    /**
     * players which have been connected before are sent
     *
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

    public static JSONObject sendInitialME(Player player) throws JsonProcessingException {
        player.getMyCards();
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("me", objectMapper.writeValueAsString(player));
        jmsg.put("initialMe", jsubmsg);
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


    public static JSONObject sendMAXPlayer(int countPlayer) throws JSONException {
        return new JSONObject().put("sendMAXPlayer", countPlayer);
    }


    public static JSONObject sendFirstCard(Card card) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("card", objectMapper.writeValueAsString(card));
        jmsg.put("pickedCard", jsubmsg);
        return jmsg;

    }

    public static JSONObject sendPlayedCard(Card card) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("card", objectMapper.writeValueAsString(card));
        jmsg.put("playedCard", jsubmsg);
        return jmsg;

    }

    public static JSONObject sendDiscardedCard(Card card) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("card", objectMapper.writeValueAsString(card));
        jmsg.put("discardedCard", jsubmsg);
        return jmsg;
    }

    public static JSONObject sendUpdatePlayer(Player player) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("player", objectMapper.writeValueAsString(player));
        jmsg.put("updatePlayer", jsubmsg);
        return jmsg;
    }

    public static JSONObject sendScores(Player player) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("player", objectMapper.writeValueAsString(player));
        jmsg.put("score", jsubmsg);
        return jmsg;
    }

    public static JSONObject useFunctionalityPeek(Card card) throws JSONException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("card", objectMapper.writeValueAsString(card));
        jmsg.put("useFunctionalityPeek", jsubmsg);
        return jmsg;
    }

    public static JSONObject useFunctionalitySpy(Card card, Player player) throws JSONException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("card", objectMapper.writeValueAsString(card));
        jsubmsg.put("spyedPlayer", objectMapper.writeValueAsString(player));
        jmsg.put("useFunctionalitySpy", jsubmsg);
        return jmsg;
    }

    public static JSONObject useFunctionalitySwap(Card card1, Player player1, Card card2, Player player2) throws JSONException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("card1", objectMapper.writeValueAsString(card1));
        jsubmsg.put("player1", objectMapper.writeValueAsString(player1));
        jsubmsg.put("card2", objectMapper.writeValueAsString(card2));
        jsubmsg.put("player2", objectMapper.writeValueAsString(player2));
        jmsg.put("useFunctionalitySwap", jsubmsg);
        return jmsg;
    }

    public static JSONObject sendFriendRequest(Player sender, Player receiver) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("sender", objectMapper.writeValueAsString(sender));
        jsubmsg.put("receiver", objectMapper.writeValueAsString(receiver));
        jmsg.put("friendrequest", jsubmsg);
        return jmsg;
    }

    public static JSONObject sendFriendAccepted(Player sender, Player receiver) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("sender", objectMapper.writeValueAsString(sender));
        jsubmsg.put("receiver", objectMapper.writeValueAsString(receiver));
        jmsg.put("friendAccepted", jsubmsg);
        return jmsg;
    }

    public static JSONObject sendPartyRequest(Player sender) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("sender", objectMapper.writeValueAsString(sender));
        jmsg.put("partyrequest", jsubmsg);
        return jmsg;
    }

    public static JSONObject sendPartyAccepted(Player sender, Player receiver) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("sender", objectMapper.writeValueAsString(sender));
        jsubmsg.put("receiver", objectMapper.writeValueAsString(receiver));
        jmsg.put("partyaccepted", jsubmsg);
        return jmsg;
    }

    public static JSONObject sendPlayerOnlineStatus(Boolean isOnline, Player player) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("isOnline", isOnline);
        jsubmsg.put("player", objectMapper.writeValueAsString(player));
        jmsg.put("onlinestatus", jsubmsg);
        return jmsg;
    }

    public static JSONObject sendInitialOthers(ArrayList<Player> players) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("players", objectMapper.writeValueAsString(players));
        jmsg.put("initialOtherPlayer", jsubmsg);
        return jmsg;
    }

    public static JSONObject sendInitialOthers(Player player) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("otherPlayer", objectMapper.writeValueAsString(player));
        jmsg.put("initialOtherPlayer", jsubmsg);
        return jmsg;
    }

    public static JSONObject picturePlayer(Player player) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("player", objectMapper.writeValueAsString(player));
        jmsg.put("picture", jsubmsg);
        return jmsg;
    }

    public static JSONObject smileyPlayer(Player player) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("player", objectMapper.writeValueAsString(player));
        jmsg.put("smiley", jsubmsg);
        return jmsg;
    }

    public static JSONObject calledCabo(Player player) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("player", objectMapper.writeValueAsString(player));
        jmsg.put("calledCabo", jsubmsg);
        return jmsg;
    }

    public static JSONObject startPrivateParty() throws JSONException {
        return new JSONObject().put("startPrivateParty", "startPrivateParty");
    }

    public static JSONObject allowedToMove() throws JSONException {
        return new JSONObject().put("allowedToMove", "");
    }

    public static JSONObject noStartYet() throws JSONException {
        return new JSONObject().put("noStartYet", "");
    }

    public static JSONObject informOthersAboutInvitation(Player player) throws JSONException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("player", objectMapper.writeValueAsString(player));
        jmsg.put("informOthersAboutInvitation", jsubmsg);
        return jmsg;
    }


    public static JSONObject partyRequestFailed(String message) throws JSONException {
        return new JSONObject().put("partyRequestFailed", message);
    }

    public static JSONObject playerRemovedFromParty(Player player) throws JSONException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        try {
            jsubmsg.put("player", objectMapper.writeValueAsString(player));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        jmsg.put("playerRemovedFromParty", jsubmsg);
        return jmsg;
    }

    public static JSONObject leaderRemovedFromParty(Player leader) throws JSONException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("player", objectMapper.writeValueAsString(leader));
        jmsg.put("leaderRemovedFromParty", jsubmsg);
        return jmsg;
    }

    public static JSONObject sendEndGame(Player player) throws JSONException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("player", objectMapper.writeValueAsString(player));
        jmsg.put("endGame", jsubmsg);
        return jmsg;
    }

    public static JSONObject sendMaxPoints(int maxpoints) throws JSONException {
        return new JSONObject().put("maxPoints", maxpoints);
    }


    //KI messages
    public static JSONObject sendPickCardKI(String message) throws JSONException {
        return new JSONObject().put("pickCard", message);
    }


    public static JSONObject swapPickedCardWithOwnCardsKI(Card card) throws JSONException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("card", objectMapper.writeValueAsString(card));
        jmsg.put("swapPickedCardWithOwnCards", jsubmsg);
        return jmsg;
    }

    public static JSONObject playPickedCardKI() throws JSONException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jmsg.put("playPickedCard", jsubmsg);
        return jmsg;
    }

    public static JSONObject sendFinishMoveKI(String message) throws JSONException {
        return new JSONObject().put("finishMove", message);
    }


    public static JSONObject useFunctionalityPeekKI(Card card) throws JSONException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("card", objectMapper.writeValueAsString(card));
        jmsg.put("useFunctionalityPeek", jsubmsg);
        return jmsg;
    }

    public static JSONObject useFunctionalitySpyKI(Card card, Player player) throws JSONException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("card", objectMapper.writeValueAsString(card));
        jsubmsg.put("spyedPlayer", objectMapper.writeValueAsString(player));
        jmsg.put("useFunctionalitySpy", jsubmsg);
        return jmsg;
    }

    public static JSONObject useFunctionalitySwapKI(Card card1, Player player1, Card card2, Player player2) throws JSONException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();
        jsubmsg.put("card1", objectMapper.writeValueAsString(card1));
        jsubmsg.put("player1", objectMapper.writeValueAsString(player1));
        jsubmsg.put("card2", objectMapper.writeValueAsString(card2));
        jsubmsg.put("player2", objectMapper.writeValueAsString(player2));
        jmsg.put("useFunctionalitySwap", jsubmsg);
        return jmsg;
    }

}
