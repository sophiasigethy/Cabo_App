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

    public static JSONObject sendInitialOthers(Player player) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        JSONObject jmsg = new JSONObject();
        JSONObject jsubmsg = new JSONObject();

        jsubmsg.put("otherPlayer", objectMapper.writeValueAsString(player));
        jmsg.put("initialOtherPlayer", jsubmsg);

        return jmsg;
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

}
