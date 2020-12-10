package msp.group3.caboclient;

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


}
