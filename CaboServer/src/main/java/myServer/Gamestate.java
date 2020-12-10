package myServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class Gamestate {

    // contains websocketsession-id and the associated player object
    public HashMap<String, Player> players = new HashMap<String, Player>();
    private final int MAX_PLAYER = 4;
    // determines how many players are already registered
    private int countPlayer = 0;
    //status of the game- see Type Defs for all 3 state
    private String state = TypeDefs.MATCHING;

    private final static int DEAL_CARD_NUMBER_AT_BEGINNING = 4;
    public CardSuiteManager cardSuiteMgr = new CardSuiteManager();

    //manages the connection
    private SocketHandler socketHandler;
    // contains all connected clients
    private List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    //client who sent something most recently
    private WebSocketSession currentSession;

    public Gamestate() {}
    public Gamestate(SocketHandler socketHandler) {
        this.socketHandler = socketHandler;
    }

    /**
     * this method handles how to proceed when a client connects
     * -> if there are not too many players yet (MAX_PLAYER), the player will be registered, otherwise not
     * @param session
     * @throws IOException
     */
    public void connectionEstablished(WebSocketSession session) throws IOException {

        if (countPlayer == MAX_PLAYER) {
            String msg = "I'm sorry. You are too late. We've been already " + MAX_PLAYER + " players";
            socketHandler.sendMessage(session, JSON_commands.answerServer(msg));
        } else {
            String text = "Welcome to the game. Please state your username";
            socketHandler.sendMessage(session, JSON_commands.Hallo(text));
            sessions.add(session);
        }
    }

    /**
     * this method handles how to proceed when a client disconnects
     * Player is deleted from Hashmap players and associated session is deleted from sessions
     * @param session
     */
    public void afterConnectionClosed(WebSocketSession session) {
        sessions.remove(session);
        if (countPlayer != 0) {
            countPlayer--;
            players.remove(session.getId());
        }
    }

    /**
     * this method handles how to proceed when a client sends something to server:
     * every sent message arrives right here
     * by using the key of the jsonObject the server knows what has been sent and how to deal with it
     * @param session
     * @param message
     * @throws IOException
     */
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        currentSession = session;
        JSONObject jsonObject = getMessage(message.getPayload());

        //client sent the username he would like to have
        if (jsonObject.has("username")) {
            String name = jsonObject.get("username").toString();
            if (isExist(name)) {
                //sends username is already in use
                socketHandler.sendMessage(session, JSON_commands.usernameInUse(name));
            } else {
                addPlayer(session, name);
                if (isMaxPlayer()) {
                    //sends gamestart and players start to play cabo
                    sendToAll(JSON_commands.statusupdateServer(state));
                } else {
                    //send status update to inform client that system is waiting for other players
                    socketHandler.sendMessage(session, JSON_commands.statusupdateServer(state));
                    sendOtherPlayers();
                }
            }
        }
        // client sent chat message
        if(jsonObject.has("chatMessage")) {
            sendToAll(jsonObject);
        }
    }


    private JSONObject getMessage(String message) throws JSONException {
        return new JSONObject(message);
    }

    /**
     * this methods adds a player to the game
     * ans informs all other players about it
     * @param webSocketSession
     * @param name
     * @throws IOException
     */
    private void addPlayer(WebSocketSession webSocketSession, String name) throws IOException {
        this.countPlayer++;
        Player mitspieler = new Player(generateId(), name, cardSuiteMgr);
        this.players.put(webSocketSession.getId(), mitspieler);
        //player is informed that he can join the game
        socketHandler.sendMessage(webSocketSession, JSON_commands.Welcome(mitspieler));
        String message = mitspieler.getName() + " joined the game";
        informOtherPlayers(JSON_commands.newPlayer(message));
    }

    /**
     * this method informs all players (besides the one who sent it) about an event
     * @param jsonObject
     * @throws IOException
     */
    public void informOtherPlayers(JSONObject jsonObject) throws IOException {
        for (int i = 0; i < sessions.size(); i++) {
            if (!sessions.get(i).getId().equalsIgnoreCase(currentSession.getId())) {
                socketHandler.sendMessage(sessions.get(i), jsonObject);
            }
        }
    }

    /**
     * this method informs ALL players about an event
     * @param jsonObject
     * @throws IOException
     */
    public void sendToAll(JSONObject jsonObject)throws IOException{
        for (WebSocketSession webSocketSession: sessions){
            socketHandler.sendMessage(webSocketSession, jsonObject);
        }
    }

    /**
     * this method checks if a username already exists
     * @param name
     * @return
     */
    public boolean isExist(String name) {
        if (players != null) {
            for (Player player : players.values()) {
                if (player.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * this method checks if there are alredy MAX_PLAYERS connected
     * if yes: state changes to Gamestart and all clients are informed about it and start to play
     * if no: clients are informed that they have to wait
     * @return
     */
    public boolean isMaxPlayer() {
        if (players.size() == MAX_PLAYER) {
            state = TypeDefs.GAMESTART;
            return true;
        }
        return false;
    }

    /**
     * this method generates an id between 1 and MAX_PLAYERS
     * -> so later it is easy to check who's turn it is
     * @return
     */
    public int generateId() {
        for (int i = 1; i < MAX_PLAYER + 1; i++) {
            if (!isIdAssigned(i)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * this method checks if an id is already assigned
     * it's an helper method for generateId()
     * @param id
     * @return
     */
    public boolean isIdAssigned(int id) {
        if (players != null) {
            for (Player player : players.values()) {
                if (player.getId() == id) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * this method sends a player who has just connected, who else has already been connected
     * @throws IOException
     */
    public void sendOtherPlayers() throws IOException {
        for (Player player : players.values()) {
            socketHandler.sendMessage(currentSession, JSON_commands.sendOtherPlayer(player));
        }
    }

    /**
     * Distribute cards to all participated players;
     */
    void distributeCardAtBeginning() {
        players.forEach((k, player) -> {
            for (int i = 0; i < DEAL_CARD_NUMBER_AT_BEGINNING; i ++) {
                player.drawCard();
            }
        });
    }
    /**
     *
     */
}




