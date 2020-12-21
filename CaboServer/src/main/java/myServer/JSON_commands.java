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
    public static JSONObject answerServer(String msg) throws JSONException {

        return new JSONObject().put("Serverantwort", msg);
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
     * @param msg
     * @return
     * @throws JSONException
     */
    public static JSONObject newPlayer(String msg) throws JSONException {

        return new JSONObject().put("newPlayer", msg);
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
    public static JSONObject gameState(CardSuiteManager mgr) throws JsonProcessingException {
        JSONObject jmsg = new JSONObject();
        ObjectMapper objectMapper = new ObjectMapper();
        jmsg.put("gameState", objectMapper.writeValueAsString(mgr));
        return jmsg;
    }
}
