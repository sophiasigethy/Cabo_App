package myServer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import nonapi.io.github.classgraph.json.JSONUtils;
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


// this class handles the connection between server and client and
//and forwards relevant information to the class gamestate
//see methods in gamestate

@Component
@PropertySource("classpath:application.properties")
public class SocketHandler extends TextWebSocketHandler {


    private ArrayList<Player> partyLeader = new ArrayList<>();
    private ArrayList<ArrayList<Player>> partyPeople = new ArrayList<>();

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
                //TODO Rework to player object
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
                //TODO Rework to sender, receiver object
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
                //TODO Rework to sender, receiver object
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
                    System.out.println(senderNick + " accepted FriendRequest from " + receiverNick);
                }
            }
            if (jsonObject.has("partyrequest2")) {
                JSONObject js = jsonObject.getJSONObject("partyrequest2");
                String json1 = js.get("sender").toString();
                String json2 = js.get("receiver").toString();

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Player sender = objectMapper.readValue(json1, Player.class);
                Player receiver = objectMapper.readValue(json2, Player.class);
                boolean isSenderInParty = isInParty(sender);
                boolean isReceiverInParty = isInParty(receiver);
                boolean isSenderLeader = isPartyLeader(sender);
                if (isReceiverInGame(receiver)) {
                    sendMessage(session, JSON_commands.partyRequestFailed("This player is already playing at the moment"));
                } else {
                    if (!isSenderInParty && !isReceiverInParty) {
                        sendMessage(getSessionByPlayerNick(receiver.getNick()), JSON_commands.sendPartyRequest2(sender));
                        return;
                    }
                    if (!isReceiverInParty && isSenderLeader) {
                        sendMessage(getSessionByPlayerNick(receiver.getNick()), JSON_commands.sendPartyRequest2(sender));
                        return;
                    }
                    if (isReceiverInParty) {
                        sendMessage(session, JSON_commands.partyRequestFailed(receiver.getNick() + " is already in another Party"));
                        return;

                    }
                    if (isSenderInParty && !isSenderLeader) {
                        sendMessage(session, JSON_commands.partyRequestFailed("You are already in a Party. Only the party Leader can invite other players"));
                        return;
                    }
                }


            }

           /* if (jsonObject.has("partyrequest")) {
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
            }*/
            if (jsonObject.has("partyaccepted")) {
                JSONObject js = jsonObject.getJSONObject("partyaccepted");
                /*String senderDbId = js.get("senderDbID").toString().replace("\"", "").replace("\\", "");
                String senderNick = js.get("senderNick").toString().replace("\"", "").replace("\\", "");
                int senderAvatarId = Integer.parseInt(js.get("senderAvatarID").toString().replace("\"", "").replace("\\", ""));
                String receiverDbId = js.get("receiverDbID").toString().replace("\"", "").replace("\\", "");
                String receiverNick = js.get("receiverNick").toString().replace("\"", "").replace("\\", "");
               *WebSocketSession receiverSession = getSessionFromNick(receiverNick);*/
                String json1 = js.get("sender").toString();
                String json2 = js.get("receiver").toString();

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Player sender = objectMapper.readValue(json1, Player.class);
                Player receiver = objectMapper.readValue(json2, Player.class);
                WebSocketSession receiverSession = getSessionFromNick(receiver.getNick());

                boolean isSenderInParty = isInParty(sender);
                boolean isReceiverInParty = isInParty(receiver);
                boolean isReceiverLeader = isPartyLeader(receiver);
                if (!isSenderInParty && !isReceiverInParty) {
                    partyLeader.add(receiver);
                    ArrayList<Player> newParty = new ArrayList<>();
                    newParty.add(receiver);
                    newParty.add(sender);
                    partyPeople.add(newParty);

                    if (receiverSession != null) {
                   /* sendMessage(receiverSession, JSON_commands.sendPartyAccepted(new Player(senderDbId, senderNick, senderAvatarId),
                        new Player(receiverDbId, receiverNick, 9)));
                    System.out.println(senderNick + " accepted PartyInvite from " + receiverNick);*/
                        sendPartyAcceptation(sender, receiver, receiverSession);

                    }
                }
                if (!isSenderInParty && isReceiverLeader) {
                    addPlayerInPartyOfLeader(receiver, sender);
                    if (receiverSession != null) {
                   /* sendMessage(receiverSession, JSON_commands.sendPartyAccepted(new Player(senderDbId, senderNick, senderAvatarId),
                        new Player(receiverDbId, receiverNick, 9)));
                    System.out.println(senderNick + " accepted PartyInvite from " + receiverNick);*/
                        sendPartyAcceptation(sender, receiver, receiverSession);
                    }
                }


            }

            if (jsonObject.has("startNewGame")) {
                JSONObject js = jsonObject.getJSONObject("startNewGame");
                ArrayList<String> partyPlayers = new ArrayList<>();
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                if (js.has("players")) {
                    String jsonString = js.get("players").toString();
                    @SuppressWarnings("unchecked")
                    List<String> party = mapper.readValue(jsonString, List.class);
                    partyPlayers.addAll(party);
                }
                if (!isAssignedToGame(getPlayerBySessionId(session.getId()))) {

                    if (partyPlayers.size() == 1) {
                        Gamestate gamestate = getNextFreeGame();
                        getPlayerBySessionId(session.getId()).setGamestate(gamestate);
                    } else {
                        Gamestate privateGamestate = getPrivateGamestate();
                        for (String playerNick : partyPlayers) {
                            Player connectedPlayer = returnConnectedPlayerByNick(playerNick);
                            if (connectedPlayer != null) {
                                connectedPlayer.setGamestate(privateGamestate);
                            }
                            WebSocketSession webSocketSession = getSessionByPlayerNick(playerNick);
                            if (webSocketSession != null) {
                                sendMessage(webSocketSession, JSON_commands.startPrivateParty());
                            }
                        }

                    }
                }


            }
        }
        if (jsonObject.has("noAccount")) {
            Player newPlayer = new Player();
            newPlayer.setNoAccount(true);
            connectedPlayers.put(session.getId(), newPlayer);
            Gamestate gamestate = getNextFreeGame();
            getPlayerBySessionId(session.getId()).setGamestate(gamestate);
            sendMessage(session, JSON_commands.allowedToMove());
        }
        if (jsonObject.has("onlineStatusOfNewFriend")) {
            String nick = jsonObject.get("onlineStatusOfNewFriend").toString();
            if (isOnline(nick)){
                sendMessage(session, JSON_commands.sendPlayerOnlineStatus(true, getPlayerByNick(nick)));
            }
        }
    }

    private void sendPartyAcceptation(Player sender, Player receiver, WebSocketSession receiverSession) throws IOException {
        sendMessage(receiverSession, JSON_commands.sendPartyAccepted(sender, receiver));
        System.out.println(sender.getNick() + " accepted PartyInvite from " + receiver.getNick());
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
        Player logoutPlayer = connectedPlayers.get(session.getId());
        connectedPlayers.remove(session.getId());
        sessions.remove(session);
        for (String storedSession : connectedPlayers.keySet()) {
            // Other people who are online, want to know, that I am offline now
            WebSocketSession sess = getSessionFromString(storedSession);
            if (sess != null)
                sendMessage(sess, JSON_commands.sendPlayerOnlineStatus(false, logoutPlayer));
        }
        //   System.out.println("User " + logoutPlayer.getNick() + " disconnected");
        //gamestate.afterConnectionClosed(session);

        if (isPartyLeader(logoutPlayer)) {
            removePartyLeader(logoutPlayer);
            deletePartyofPartyLeader(logoutPlayer);

        }
        if (isInParty(logoutPlayer)) {
            removePlayerOfParty(logoutPlayer);
        }

    }


    public void sendMessage(WebSocketSession webSocketSession, JSONObject jsonMsg) throws IOException {
        //TODO TEST THIS SOLUTION
        if (webSocketSession != null) {
            try {
                webSocketSession.sendMessage(new TextMessage(jsonMsg.toString()));
            } catch (Exception ex) {
                synchronized (sessions) {
                    sessions.remove(webSocketSession);
                }
            }
        }
    }

    /**
     * @return a GameState in MATCHING state
     */
    public Gamestate getNextFreeGame() {
        for (Gamestate game : games) {
            if (game.getState().equals(TypeDefs.MATCHING) && !game.isPrivateParty()) {
                return game;
            }
        }
        return createNewGamestate();
    }


    public Gamestate createNewGamestate() {
        counterGameStateId++;
        Gamestate newGamestate = new Gamestate(this);
        newGamestate.setGamestateID(counterGameStateId);
        games.add(newGamestate);
        return newGamestate;
    }

    public Gamestate getPrivateGamestate() {
        Gamestate privateGamestate = createNewGamestate();
        privateGamestate.setPrivateParty(true);
        return privateGamestate;
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

    public Map<String, Player> getConnectedPlayers() {
        return connectedPlayers;
    }

    public void setConnectedPlayers(Map<String, Player> connectedPlayers) {
        this.connectedPlayers = connectedPlayers;
    }

    public WebSocketSession getSessionByPlayerNick(String nick) {
        for (Map.Entry<String, Player> entry : connectedPlayers.entrySet()) {
            String key = entry.getKey();
            Player player = entry.getValue();
            if (player.getNick().equalsIgnoreCase(nick)) {
                return getSessionBySessionId(key);
            }

        }
        return null;
    }

    public WebSocketSession getSessionBySessionId(String id) {
        for (WebSocketSession session : sessions) {
            if (id.equalsIgnoreCase(session.getId())) {
                return session;
            }
        }
        return null;
    }

    public Player returnConnectedPlayerByNick(String nick) {
        for (Player connectedPlayer : connectedPlayers.values()) {
            if (connectedPlayer.getNick().equalsIgnoreCase(nick)) {
                return connectedPlayer;
            }
        }
        return null;
    }

    public boolean isPartyLeader(Player player) {
        for (Player leader : partyLeader) {
            if (leader.getNick().equalsIgnoreCase(player.getNick())) {
                return true;
            }
        }
        return false;
    }

    public boolean isInParty(Player player) {
        for (ArrayList<Player> partys : partyPeople) {
            for (Player leader : partys) {
                if (leader.getNick().equalsIgnoreCase(player.getNick())) {
                    return true;
                }
            }
        }
        return false;
    }

    public Player returnPartyLeaderOfPlayer(Player player) {
        for (int i = 0; i < partyPeople.size(); i++) {
            for (Player leader : partyPeople.get(i)) {
                if (leader.getNick().equalsIgnoreCase(player.getNick())) {
                    return partyPeople.get(i).get(0);
                }
            }
        }
        return null;
    }

    public void addPlayerInPartyOfLeader(Player leader, Player newPlayer) {
        for (int i = 0; i < partyPeople.size(); i++) {
            ArrayList<Player> currentList = partyPeople.get(i);
            for (int j = 0; j < currentList.size(); j++) {
                if (currentList.get(j).getNick().equalsIgnoreCase(leader.getNick())) {
                    partyPeople.get(i).add(newPlayer);
                }
            }
        }
    }

    public void deletePartyofPartyLeader(Player leader) throws IOException {
        for (int i = 0; i < partyPeople.size(); i++) {
            for (Player player : partyPeople.get(i)) {
                if (player.getNick().equalsIgnoreCase(leader.getNick())) {
                    ArrayList<Player> currentParty = partyPeople.get(i);
                    partyPeople.remove(currentParty);
                    sendRemovedLeader(currentParty, leader);
                }
            }
        }
    }

    public void removePlayerOfParty(Player logout) throws IOException {
        for (int i = 0; i < partyPeople.size(); i++) {
            ArrayList<Player> currentParty = partyPeople.get(i);
            for (int j = 0; j < currentParty.size(); j++) {
                if (currentParty.get(j).getNick().equalsIgnoreCase(logout.getNick())) {
                    removePlayerofParty(currentParty, logout);
                    sendRemovedPartyPlayer(currentParty, logout);
                }
            }
        }
    }

    public void sendRemovedPartyPlayer(ArrayList<Player> otherPlayers, Player logoutPlayer) throws IOException {
        for (Player player : otherPlayers) {
            if (!player.getNick().equalsIgnoreCase(logoutPlayer.getNick()))
                this.sendMessage(getSessionByPlayerNick(player.getNick()), JSON_commands.playerRemovedFromParty(logoutPlayer));
        }
    }

    public void sendRemovedLeader(ArrayList<Player> otherPlayers, Player leader) throws IOException {
        for (Player player : otherPlayers) {
            if (!player.getNick().equalsIgnoreCase(leader.getNick()))
                sendMessage(getSessionByPlayerNick(player.getNick()), JSON_commands.leaderRemovedFromParty(leader));
        }
    }

    public void removePartyLeader(Player leader) {
        for (int i = 0; i < partyLeader.size(); i++) {
            if (partyLeader.get(i).getNick().equalsIgnoreCase(leader.getNick())) {
                partyLeader.remove(i);
            }
        }
    }

    public void removePlayerofParty(ArrayList<Player> party, Player partyPLayer) {
        for (int i = 0; i < party.size(); i++) {
            if (party.get(i).getNick().equalsIgnoreCase(partyPLayer.getNick())) {
                party.remove(i);
            }
        }
    }

    public Player getPlayerByNick(String nick) {
        for (Player connectedPlayer : connectedPlayers.values()) {
            if (connectedPlayer.getNick().equalsIgnoreCase(nick)) {
                return connectedPlayer;
            }
        }
        return null;
    }

    public boolean isReceiverInGame(Player player) {
        Player receiver = getPlayerByNick(player.getNick());
        if (receiver != null) {
            if (receiver.getGamestate() != null) {
                return true;
            }
        }
        return false;
    }

    public void removeGamestate(Gamestate gamestate) {
        for (int i = 0; i < games.size(); i++) {
            if (games.get(i).getGamestateID() == gamestate.getGamestateID()) {
                games.remove(i);
            }
        }

    }

    public boolean isOnline(String nick) {
        for (Player player : connectedPlayers.values()) {
            if (player != null) {
                if (player.getNick().equalsIgnoreCase(nick)) {
                    return true;
                }
            }
        }
        return false;
    }
}
