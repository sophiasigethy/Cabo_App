package myServer;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


// this class handles the connection between server and client and
//and forwards relevant information to the class gamestate
//see methods in gamestate

@Component
@PropertySource("classpath:application.properties")
public class SocketHandler extends TextWebSocketHandler {

    private Gamestate gamestate = null;
    private Map<String, String> usernames = new HashMap<String, String>();
    private final String serverName = "Server";
    private String mMessage = "";
    private String mWho = "";
    private Map<String, Player> connectedPlayers = new HashMap<String, Player>();
    private ArrayList<Gamestate> games = new ArrayList<>();
    private Set<WebSocketSession> sessions = new HashSet<>();
    private Map<String, ArrayList<Player>> pendingFriendRequests = new HashMap<>();
    private int counterGameStateId = 0;


    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
        throws InterruptedException, IOException {
        JSONObject jsonObject = getMessage(message.getPayload());
        Player currentSender = getPlayerBySessionId(session.getId());

        if (isAssignedToGame(getPlayerBySessionId(session.getId()))) {
            getPlayerBySessionId(session.getId()).getGamestate().handleTextMessage(session, message);
        } else {


            if (jsonObject.has("sendUserLogin")) {
                JSONObject js = jsonObject.getJSONObject("sendUserLogin");
                String dbId = js.get("senderDbID").toString().replace("\"", "").replace("\\", "");
                String nick = js.get("senderNick").toString().replace("\"", "").replace("\\", "");
                int avatarID = Integer.parseInt(js.get("senderAvatarID").toString().replace("\"", "").replace("\\", ""));
                Player player = new Player(dbId, nick, avatarID);
                connectedPlayers.put(session.getId(), player);
                checkPendingFriendRequests(session, dbId, nick);
                System.out.println("User " + nick + " connected");
                for (String storedSession : connectedPlayers.keySet()) {
                    if (session.getId().equals(storedSession)) {
                        // This is my session and I want to know, who is online
                        for (String sess : connectedPlayers.keySet()) {
                            if (sess.equals(session.getId()))
                                continue;
                            sendMessage(session, JSON_commands.sendPlayerOnlineStatus(true, connectedPlayers.get(sess)));
                        }
                    } else {
                        // Other people who are online, want to know, that I am online now
                        WebSocketSession sess = getSessionFromString(storedSession);
                        if (sess != null)
                            sendMessage(sess, JSON_commands.sendPlayerOnlineStatus(true, player));
                    }
                }
            }
            if (jsonObject.has("sendFriendRequest")) {
                JSONObject js = jsonObject.getJSONObject("sendFriendRequest");
                String senderDbId = js.get("senderDbID").toString().replace("\"", "").replace("\\", "");
                String senderNick = js.get("senderNick").toString().replace("\"", "").replace("\\", "");
                int senderAvatarId = Integer.parseInt(js.get("senderAvatarID").toString().replace("\"", "").replace("\\", ""));
                String receiverDbId = js.get("receiverDbID").toString().replace("\"", "").replace("\\", "");
                String receiverNick = js.get("receiverNick").toString().replace("\"", "").replace("\\", "");
                WebSocketSession receiverSession = getSessionFromNick(receiverNick);
                if (receiverSession == null) {
                    if (pendingFriendRequests.get(receiverDbId) == null) {
                        ArrayList<Player> requests = new ArrayList<>();
                        requests.add(new Player(senderDbId, senderNick, senderAvatarId));
                        pendingFriendRequests.put(receiverDbId, requests);
                    } else {
                        ArrayList<Player> requests = pendingFriendRequests.get(receiverDbId);
                        requests.add(new Player(senderDbId, senderNick, senderAvatarId));
                        pendingFriendRequests.put(receiverDbId, requests);
                    }
                } else {
                    sendMessage(receiverSession, JSON_commands.sendFriendRequest(new Player(senderDbId, senderNick, senderAvatarId),
                        new Player(receiverDbId, receiverNick, 9)));
                    System.out.println("Friendrequest sent from " + senderNick + " to " + receiverNick);
                }
            }

            if (jsonObject.has("sendFriendAccepted")) {
                JSONObject js = jsonObject.getJSONObject("sendFriendAccepted");
                String senderDbId = js.get("senderDbID").toString().replace("\"", "").replace("\\", "");
                String senderNick = js.get("senderNick").toString().replace("\"", "").replace("\\", "");
                int senderAvatarId = Integer.parseInt(js.get("senderAvatarID").toString().replace("\"", "").replace("\\", ""));
                String receiverDbId = js.get("receiverDbID").toString().replace("\"", "").replace("\\", "");
                String receiverNick = js.get("receiverNick").toString().replace("\"", "").replace("\\", "");
                WebSocketSession receiverSession = getSessionFromNick(receiverNick);
                if (receiverSession != null) {
                    sendMessage(receiverSession, JSON_commands.sendFriendAccepted(new Player(senderDbId, senderNick, senderAvatarId),
                        new Player(receiverDbId, receiverNick, 9)));
                    System.out.println(senderNick + "accepted FriendRequest from " + receiverNick);
                }
            }

            if (jsonObject.has("partyrequest")) {
                JSONObject js = jsonObject.getJSONObject("partyrequest");
                String senderDbId = js.get("senderDbID").toString().replace("\"", "").replace("\\", "");
                String senderNick = js.get("senderNick").toString().replace("\"", "").replace("\\", "");
                int senderAvatarId = Integer.parseInt(js.get("senderAvatarID").toString().replace("\"", "").replace("\\", ""));
                String receiverDbId = js.get("receiverDbID").toString().replace("\"", "").replace("\\", "");
                String receiverNick = js.get("receiverNick").toString().replace("\"", "").replace("\\", "");
                WebSocketSession receiverSession = getSessionFromNick(receiverNick);
                if (receiverSession != null) {
                    sendMessage(receiverSession, JSON_commands.sendPartyRequest(new Player(senderDbId, senderNick, senderAvatarId),
                        new Player(receiverDbId, receiverNick, 9)));
                    System.out.println("Partyrequest sent from " + senderNick + " to " + receiverNick);
                }
            }
            if (jsonObject.has("partyaccepted")) {
                JSONObject js = jsonObject.getJSONObject("partyaccepted");
                String senderDbId = js.get("senderDbID").toString().replace("\"", "").replace("\\", "");
                String senderNick = js.get("senderNick").toString().replace("\"", "").replace("\\", "");
                int senderAvatarId = Integer.parseInt(js.get("senderAvatarID").toString().replace("\"", "").replace("\\", ""));
                String receiverDbId = js.get("receiverDbID").toString().replace("\"", "").replace("\\", "");
                String receiverNick = js.get("receiverNick").toString().replace("\"", "").replace("\\", "");
                WebSocketSession receiverSession = getSessionFromNick(receiverNick);
                if (receiverSession != null) {
                    sendMessage(receiverSession, JSON_commands.sendPartyAccepted(new Player(senderDbId, senderNick, senderAvatarId),
                        new Player(receiverDbId, receiverNick, 9)));
                    System.out.println(senderNick + " accepted PartyInvite from " + receiverNick);
                }
            }

            if (jsonObject.has("startNewGame")) {
                Gamestate gamestate = getNextFreeGame();
                gamestate.setGamestateID(counterGameStateId);
                counterGameStateId++;
                games.add(gamestate);
                getPlayerBySessionId(session.getId()).setGamestate(gamestate);
            }
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        /*
        if (gamestate == null)
            gamestate = new Gamestate(this);
        gamestate.connectionEstablished(session);
         */
        sessions.add(session);
        sendMessage(session, JSON_commands.connectionAccepted("Online"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closedStatus) throws Exception {
        if (isAssignedToGame(getPlayerBySessionId(session.getId()))) {
            getPlayerBySessionId(session.getId()).getGamestate().afterConnectionClosed(session);
        }
        Player logoutPlayer = connectedPlayers.get(session);
        for (WebSocketSession sess : sessions) {
            if (sess.equals(session))
                continue;

        }
        connectedPlayers.remove(session.getId());
        sessions.remove(session);
        for (String storedSession : connectedPlayers.keySet()) {
            // Other people who are online, want to know, that I am offline now
            WebSocketSession sess = getSessionFromString(storedSession);
            if (sess != null)
                sendMessage(sess, JSON_commands.sendPlayerOnlineStatus(false, logoutPlayer));
        }
        //gamestate.afterConnectionClosed(session);


    }


    public void sendMessage(WebSocketSession webSocketSession, JSONObject jsonMsg) throws IOException {
        webSocketSession.sendMessage(new TextMessage(jsonMsg.toString()));
    }

    public Gamestate getNextFreeGame() {
        /**
         * @return a GameState in MATCHING state
         * */
        for (Gamestate game : games) {
            if (game.getState().equals(TypeDefs.MATCHING)) {
                return game;
            }
        }
        return new Gamestate(this);
    }

    private WebSocketSession getSessionFromNick(String nick) {
        Iterator it = connectedPlayers.entrySet().iterator();
        String session = "";
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Player player = (Player) pair.getValue();
            if (player.getNick().equals(nick)) {
                session = pair.getKey().toString();
            }
        }
        for (WebSocketSession ws : sessions) {
            if (ws.getId().equals(session)) {
                return ws;
            }
        }
        return null;
    }

    private WebSocketSession getSessionFromString(String sess) {
        for (WebSocketSession session : sessions) {
            if (session.getId().equals(sess)) {
                return session;
            }
        }
        return null;
    }

    private void sendOnlineStatus() {

    }

    private JSONObject getMessage(String message) throws JSONException {
        return new JSONObject(message);
    }

    private void checkPendingFriendRequests(WebSocketSession receiverSession, String receiverDbId, String receiverNick) {
        if (pendingFriendRequests.get(receiverDbId) != null) {
            ArrayList<Player> requests = pendingFriendRequests.get(receiverDbId);
            for (Player sender : requests) {
                try {
                    sendMessage(receiverSession, JSON_commands.sendFriendRequest(sender,
                        new Player(receiverDbId, receiverNick, 9)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            pendingFriendRequests.remove(receiverDbId);
        }
    }


    public Player getPlayerBySessionId(String sessionId) {
        for (Map.Entry<String, Player> entry : connectedPlayers.entrySet()) {
            String key = entry.getKey();
            Player player = entry.getValue();
            if (key.equalsIgnoreCase(sessionId)) {
                return player;
            }

        }
        return null;
    }

    public boolean isAssignedToGame(Player player) {
        if (player != null) {
            if (player.getGamestate() != null) {
                return true;
            }
        }
        return false;
    }
}
