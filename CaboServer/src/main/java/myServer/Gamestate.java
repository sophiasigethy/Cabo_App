package myServer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

//import javax.xml.soap.Text;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Gamestate {

    // contains websocketsession-id and the associated player object
    public HashMap<String, Player> players = new HashMap<String, Player>();

    // private ArrayList<Player> privatePartyPlayers= new ArrayList<>();
    private int MAX_PLAYER = 4;
    private int test = 0;
    // determines how many players are already registered
    private int countPlayer = 0;


    private int maxPoints = 100;

    private boolean privateParty = false;

    private boolean playWithKI = false;
    private JSONObject KIJsonObject;

    private int gamestateID = 0;
    //status of the game- see Type Defs for all 3 state
    private String state = TypeDefs.MATCHING;
    private int lastDrawnPlayerId = 0;

    // public CardSuiteManager cardSuiteMgr = null;

    //manages the connection
    private SocketHandler socketHandler;
    // contains all connected clients
    private List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    //client who sent something most recently
    private WebSocketSession currentSession;

    private int currentPlayerId = 0;
    private Card currentPickedCard = null;
    private int initialSetUp = 0;

    public Gamestate(SocketHandler socketHandler) {
        // this.cardSuiteMgr = new CardSuiteManager();
        this.generateCards(true);
        this.socketHandler = socketHandler;
    }

    public Gamestate(SocketHandler socketHandler, boolean shouldShuffle) {
        this.generateCards(false);
        this.socketHandler = socketHandler;
    }

    /**
     * this method handles how to proceed when a client connects
     * -> if there are not too many players yet (MAX_PLAYER), the player will be registered, otherwise not
     *
     * @param session
     * @throws IOException
     */
    public void connectionEstablished(WebSocketSession session) throws IOException {

        if (countPlayer == MAX_PLAYER) {
            String msg = "I'm sorry. You are too late. We've been already " + MAX_PLAYER + " players";
            socketHandler.sendMessage(session, JSON_commands.connectionNotAccepted(msg));
        } else {
            //String text = "Welcome to the game. Please state your username";
            //socketHandler.sendMessage(session, JSON_commands.Hallo(text));
            socketHandler.sendMessage(session, JSON_commands.connectionAccepted("accepted"));
            sessions.add(session);
        }
    }

    /**
     * this method handles how to proceed when a client disconnects
     * Player is deleted from Hashmap players and associated session is deleted from sessions
     *
     * @param session
     */
    public void afterConnectionClosed(WebSocketSession session) {
        sessions.remove(session);
        if (countPlayer != 0) {
            countPlayer--;
            Player disconnectedPlayer = getPlayerBySessionId(session.getId());
            players.remove(session.getId());


            try {
                sendToAll(JSON_commands.removePlayer(disconnectedPlayer));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*if (state.equalsIgnoreCase(TypeDefs.GAMESTART)) {
            for (Player player : players.values()) {
                for (Player socketPlayer : socketHandler.getConnectedPlayers().values()) {
                    if (player.getNick().equalsIgnoreCase(socketPlayer.getNick())) {
                        socketPlayer.setGamestate(null);
                    }
                }
            }
        }*/
        if (players.size() == 0) {
            socketHandler.removeGamestate(this);
        }
        if (players.size() == 1 && playWithKI) {
            socketHandler.removeGamestate(this);
        }


    }

    /**
     * this method handles how to proceed when a client sends something to server:
     * every sent message arrives right here
     * by using the key of the jsonObject the server knows what has been sent and how to deal with it
     *
     * @param session
     * @param message
     * @throws IOException
     */
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        currentSession = session;
        boolean authorised = false;
        Player currPlayer = null;
        JSONObject jsonObject = null;
        if (message == null) {
            jsonObject = KIJsonObject;
        } else {
            jsonObject = getMessage(message.getPayload());
        }


        if (jsonObject.has("welcomeMessage")) {
            String text = "Welcome to the game. Please state your username";
            socketHandler.sendMessage(session, JSON_commands.Hallo(text));
            if (!sessionAlreadyAdded(session)) {
                sessions.add(session);
            }
        }
        //client sent the username he would like to have
        if (jsonObject.has("username")) {
            // if (isMaxPlayer()) {
            if (players.size() == 4) {
                String msg = "Sorry enough players have registered in the meantime. You can no longer join this game.";
                socketHandler.sendMessage(session, JSON_commands.connectionNotAccepted(msg));
            } else {
                String name = jsonObject.get("username").toString();
                if (isExist(name)) {
                    //sends username is already in use
                    socketHandler.sendMessage(session, JSON_commands.usernameInUse(name));
                } else {
                    if (!state.equalsIgnoreCase(TypeDefs.MATCHING)) {
                        assignToNewGamestate(session, message);

                    } else {
                        addPlayer(session, name);
                        sendOtherPlayers();
                        if (isMaxPlayer()) {
                            sendToAll(JSON_commands.statusupdateServer(state));
                        } else {
                            socketHandler.sendMessage(session, JSON_commands.statusupdateServer(state));
                        }
                    }
                }
            }
        }
        // client sent chat message
        if (jsonObject.has("chatMessage")) {
            sendToAll(jsonObject);
        }
        if (jsonObject.has("askForStart")) {
            /*if (players.size()>1){
                startGame();
            }else{
                socketHandler.sendMessage(session, JSON_commands.noStartYet());
            }*/
            if (players.size() > 1) {
                startGame();
            } else {
                if (players.size() == 1) {
                    addKI("KI");
                    state = TypeDefs.GAMESTART;

                    startGame();
                } else {
                    socketHandler.sendMessage(session, JSON_commands.noStartYet());
                }

            }
        }


        if (jsonObject.has("startGameForAll")) {
            startGame();

        }
        if (jsonObject.has("askForInitialSettings")) {
            socketHandler.sendMessage(session, JSON_commands.sendMAXPlayer(MAX_PLAYER));
            initialSetUp++;
            if (initialSetUp == MAX_PLAYER) {
                startRound();
            }
        }

        if (jsonObject.has("memorizedCards")) {
            Player player = getPlayerBySessionId(session.getId());
            if (!(player.getStatus().equalsIgnoreCase(TypeDefs.waiting) || player.getStatus().equalsIgnoreCase(TypeDefs.playing))) {
                getPlayerBySessionId(session.getId()).setStatus(TypeDefs.readyForGamestart);
            }
            if (checkIfEveryoneReady()) {
                //send which player's turn it is
                currentPlayerId = getFirstPlayer();
                updatePlayerStatus();
                sendStatusupdateOfAllPlayer();
                //sendStatusupdatePlayer();
                sendNextPlayer();
            }
        }

        if (jsonObject.has("pickCard")) {

            if (session != null) {
                authorised = checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()));
                currPlayer = getPlayerBySessionId(session.getId());
            } else {
                authorised = KIsTurn();
                currPlayer = returnKI();
            }
            if (authorised) {

                if (currPlayer != null) {
                    if (availableCards.size() != 0) {
                        currentPickedCard = availableCards.get(0);
                        //currentPickedCard = new Card(11, "", "");

                    } else {
                        mixCards();
                        currentPickedCard = availableCards.get(0);
                    }
                    lastDrawnPlayerId = currPlayer.getId();

                    socketHandler.sendMessage(session, JSON_commands.sendFirstCard(currentPickedCard));
                    if (KIsTurn()) {
                        handleKI("decideForMove");
                    }
                }
            }
        }

        if (jsonObject.has("swapPickedCardWithOwnCards")) {
            if (session != null) {
                authorised = checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()));
                currPlayer = getPlayerBySessionId(session.getId());
            } else {
                authorised = KIsTurn();
                currPlayer = returnKI();
            }
            if (authorised) {
                //if (checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()))) {
                JSONObject js = jsonObject.getJSONObject("swapPickedCardWithOwnCards");
                String json = js.get("card").toString();
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Card ownCard = objectMapper.readValue(json, Card.class);
                //Player currentPlayer = getPlayerBySessionId(session.getId());

                //currentPlayer.swapWithOwnCard(ownCard, currentPickedCard);
                currPlayer.swapWithOwnCard(ownCard, currentPickedCard);
                sendToAll(JSON_commands.sendDiscardedCard(ownCard));
                //sendToAll(JSON_commands.sendUpdatePlayer(currentPlayer));
                sendToAll(JSON_commands.sendUpdatePlayer(currPlayer));
            }
            if (session != null && playWithKI) {
                updateKnownListsOfKIaferRealPlayerMove();
            }
        }

        if (jsonObject.has("playPickedCard")) {
            if (session != null) {
                authorised = checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()));
                currPlayer = getPlayerBySessionId(session.getId());
            } else {
                authorised = KIsTurn();
                currPlayer = returnKI();
            }
            if (authorised) {
                //if (checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()))) {
                takeFirstCardFromAvailableCards();
                if (availableCards.size() != 0) {
                    availableCards.remove(0);
                }
                playedCards.add(currentPickedCard);
                sendToAll(JSON_commands.sendPlayedCard(currentPickedCard));
            }
        }

        if (jsonObject.has("useFunctionalityPeek")) {
            if (session != null) {
                authorised = checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()));
                currPlayer = getPlayerBySessionId(session.getId());
            } else {
                authorised = KIsTurn();
                currPlayer = returnKI();
            }
            if (authorised) {
                JSONObject js = jsonObject.getJSONObject("useFunctionalityPeek");
                String json = js.get("card").toString();
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Card card = objectMapper.readValue(json, Card.class);

                sendToAll(JSON_commands.useFunctionalityPeek(card));
                //Player currentPlayer = getPlayerBySessionId(session.getId());

            }
        }
        if (jsonObject.has("useFunctionalitySpy")) {
            // if (checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()))) {
            if (session != null) {
                authorised = checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()));
                currPlayer = getPlayerBySessionId(session.getId());
            } else {
                authorised = KIsTurn();
                currPlayer = returnKI();
            }
            if (authorised) {
                JSONObject js = jsonObject.getJSONObject("useFunctionalitySpy");
                String json1 = js.get("card").toString();
                String json3 = js.get("spyedPlayer").toString();

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Card card = objectMapper.readValue(json1, Card.class);
                Player spyedPlayer = objectMapper.readValue(json3, Player.class);


                sendToAll(JSON_commands.useFunctionalitySpy(card, spyedPlayer));
                //Player currentPlayer = getPlayerBySessionId(session.getId());

            }
        }
        if (jsonObject.has("useFunctionalitySwap")) {
            // if (checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()))) {
            if (session != null) {
                authorised = checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()));
                currPlayer = getPlayerBySessionId(session.getId());
            } else {
                authorised = KIsTurn();
                currPlayer = returnKI();
            }
            if (authorised) {
                JSONObject js = jsonObject.getJSONObject("useFunctionalitySwap");
                String json = js.get("card1").toString();
                String json2 = js.get("card2").toString();
                String json3 = js.get("player1").toString();
                String json4 = js.get("player2").toString();
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Card card1 = objectMapper.readValue(json, Card.class);
                Card card2 = objectMapper.readValue(json2, Card.class);
                Player player1 = objectMapper.readValue(json3, Player.class);
                Player player2 = objectMapper.readValue(json4, Player.class);

                if ((!getPlayerById(player1.getId()).getCalledCabo()) && (!getPlayerById(player2.getId()).getCalledCabo())) {
                    getPlayerById(player1.getId()).swapWithOtherPlayer(getPlayerById(player2.getId()), card1, card2);
                }

                sendToAll(JSON_commands.sendUpdatePlayer(getPlayerById(player1.getId())));
                sendToAll(JSON_commands.sendUpdatePlayer(getPlayerById(player2.getId())));
                sendToAll(JSON_commands.useFunctionalitySwap(card1, player1, card2, player2));

                if (session != null && playWithKI) {
                    updateKnownListsOfKIaferRealPlayerMove();
                }
            }
        }

        if (jsonObject.has("finishMove")) {
            Player player = null;
            if (session != null) {
                authorised = checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()));
                player = getPlayerBySessionId(session.getId());
            } else {
                authorised = KIsTurn();
                player = returnKI();
            }
            if (authorised) {
                if (hasPlayerDrawn(player.getId())) {
                    finishMove();
                    // sendStatusupdatePlayer();
                    if (getPlayerById(currentPlayerId).getCalledCabo()) {
                        finishRound();
                    } else {
                        sendStatusupdateOfAllPlayer();
                        // sendToAll(JSON_commands.sendNextPlayer(currentPlayerId));
                        sendNextPlayer();
                    }

                }

            }

        }

        if (jsonObject.has("cabo")) {
            if (session != null) {
                authorised = checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()));
                currPlayer = getPlayerBySessionId(session.getId());
            } else {
                authorised = KIsTurn();
                currPlayer = returnKI();
            }
            if (authorised) {
                //Player currentPlayer = getPlayerBySessionId(session.getId());
                //currentPlayer.setCalledCabo(true);
                currPlayer.setCalledCabo(true);
                //lastDrawnPlayerId = currentPlayer.getId();
                lastDrawnPlayerId = currPlayer.getId();
                //sendToAll(JSON_commands.calledCabo(currentPlayer));}
                sendToAll(JSON_commands.calledCabo(currPlayer));
            }

        }
        if (jsonObject.has("picture")) {
            String picture = jsonObject.get("picture").toString();
            Player currentPlayer = getPlayerBySessionId(session.getId());
            currentPlayer.setPicture(picture);
            sendPictureOfOnePlayertoAll(currentPlayer);
        }
        if (jsonObject.has("smiley")) {
            String smiley = jsonObject.get("smiley").toString();
            Player currentPlayer = getPlayerBySessionId(session.getId());
            currentPlayer.setSmiley(smiley);
            sendSmileyOfOnePlayerToAll(currentPlayer);
        }
        if (jsonObject.has("maxPoints")) {
            maxPoints = (int) jsonObject.get("sendMAXPlayer");
            sendToAll(JSON_commands.sendMaxPoints(maxPoints));
        }
        if (jsonObject.has("leaveGame")) {
            // Player player = getPlayerBySessionId(session.getId());
            if (session != null) {
                currPlayer = getPlayerBySessionId(session.getId());
            } else {
                currPlayer = returnKI();
            }

            socketHandler.getPlayerBySessionId(session.getId()).setGamestate(null);
            socketHandler.getConnectedPlayers().remove(session.getId());
            socketHandler.getSessions().remove(session);

            if (privateParty) {
                currPlayer.setNick(currPlayer.getName());
                if (socketHandler.isPartyLeader(currPlayer)) {
                    socketHandler.removePartyLeader(currPlayer);
                    socketHandler.deletePartyofPartyLeader(currPlayer);
                } else {
                    socketHandler.removePlayerOfParty(currPlayer);
                }
            }
            afterConnectionClosed(session);

        }
        if (jsonObject.has("leaveWaitingRoom")) {
            if (session != null) {
                leaveWaitingRoom(session);
            }

        }
    }


    private void startGame() throws IOException {
        MAX_PLAYER = players.size();
        state = TypeDefs.GAMESTART;
        sendToAll(JSON_commands.statusupdateServer(state));
        sendToAll(JSON_commands.startGame("start"));
    }


    private void startRound() throws IOException {
        availableCards = null;
        playedCards = null;
        discardedCards = null;
        generateCards(true);
        distributeCardsAtBeginning();

        for (Player player : players.values()) {
            sendInitialSetUp(player);
        }

        if (playWithKI) {
            Player KI = returnKI();

            if (KI != null) {
                KI.getKnownCards().clear();
                KI.getKnownCardsOfOther().clear();
                KI.getKnownCards().add(KI.getCards().get(0));
                KI.getKnownCards().add(KI.getCards().get(1));
                KI.setStatus(TypeDefs.readyForGamestart);
            }
        }
    }


    private JSONObject getMessage(String message) throws JSONException {
        return new JSONObject(message);
    }

    /**
     * this methods adds a player to the game
     * ans informs all other players about it
     *
     * @param webSocketSession
     * @param name
     * @throws IOException
     */
    private void addPlayer(WebSocketSession webSocketSession, String name) throws IOException {
        this.countPlayer++;
        // Player newPlayer = new Player(generateId(), name, cardSuiteMgr);
        Player newPlayer = new Player(generateId(), name, this);
        newPlayer.setNick(name);
        this.players.put(webSocketSession.getId(), newPlayer);
        //player is informed that he can join the game
        saveAvatar(newPlayer);
        socketHandler.sendMessage(webSocketSession, JSON_commands.Welcome(newPlayer));
        informOtherPlayers(JSON_commands.newPlayer(newPlayer));
    }

    private void addKI(String name) throws IOException {
        playWithKI = true;
        this.countPlayer++;
        // Player newPlayer = new Player(generateId(), name, cardSuiteMgr);
        Player newPlayer = new Player(generateId(), name, this);
        this.players.put("KI", newPlayer);
        sessions.add(null);
        //player is informed that he can join the game
        newPlayer.setAvatarID(1);
        newPlayer.setKI(true);
        initialSetUp++;
        newPlayer.setStatus(TypeDefs.readyForGamestart);
    }

    /**
     * this method informs all players (besides the one who sent it) about an event
     *
     * @param jsonObject
     * @throws IOException
     */
    public void informOtherPlayers(JSONObject jsonObject) throws IOException {
        for (Map.Entry<String, Player> entry : players.entrySet()) {
            String key = entry.getKey();
            if (!key.equalsIgnoreCase(currentSession.getId())) {
                socketHandler.sendMessage(getSessionBySessionId(key), jsonObject);
            }

        }
    }

    public void assignToNewGamestate(WebSocketSession session, TextMessage message) throws IOException {
        if (session != null) {
            Player player = socketHandler.getPlayerBySessionId(session.getId());
            player.setGamestate(socketHandler.getNextFreeGame());
            if (!player.getGamestate().sessionAlreadyAdded(session)) {
                player.getGamestate().sessions.add(session);
            }
            String stateNewGamestate = player.getGamestate().state;
            player.getGamestate().socketHandler.sendMessage(session, JSON_commands.statusupdateServer(stateNewGamestate));
            player.getGamestate().handleTextMessage(session, message);
            sessions.remove(session);
        }
    }

    public void leaveWaitingRoom(WebSocketSession session) throws IOException {
        Player socketPlayer = socketHandler.getPlayerBySessionId(session.getId());
        socketPlayer.setGamestate(null);
        socketHandler.getConnectedPlayers().remove(session.getId());
        socketHandler.getSessions().remove(session);
       // socketHandler.getSessions().remove(session);

        //sessions.remove(session);
        //players.remove(session.getId());


        if (privateParty) {

            if (socketHandler.isPartyLeader(socketPlayer)) {
                socketHandler.removePartyLeader(socketPlayer);
                socketHandler.deletePartyofPartyLeader(socketPlayer);
            } else {
                socketHandler.removePlayerOfParty(socketPlayer);
            }
        }
        afterConnectionClosed(session);
    }


    /**
     * this method informs ALL players about an event
     *
     * @param jsonObject
     * @throws IOException
     */
    public void sendToAll(JSONObject jsonObject) throws IOException {
        for (WebSocketSession webSocketSession : sessions) {
            if (webSocketSession != null) {
                socketHandler.sendMessage(webSocketSession, jsonObject);
            }
        }
    }


    /**
     * this method checks if a username already exists
     *
     * @param name
     * @return
     */
    public boolean isExist(String name) {
        if (players != null) {
            for (Player player : players.values()) {
                if (player.getNick() != null) {
                    if (player.getNick().equalsIgnoreCase(name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void sendToAllButME(JSONObject jsonObject, WebSocketSession mySession) throws IOException {
        for (Map.Entry<String, Player> entry : players.entrySet()) {
            String key = entry.getKey();
            if (!key.equalsIgnoreCase(mySession.getId())) {
                socketHandler.sendMessage(getSessionBySessionId(key), jsonObject);
            }

        }
    }


    /**
     * this method checks if there are alredy MAX_PLAYERS connected
     * if yes: state changes to Gamestart and all clients are informed about it and start to play
     * if no: clients are informed that they have to wait
     *
     * @return
     */
    public boolean isMaxPlayer() {
        //if (players.size() == MAX_PLAYER) {
        if (players.size() == 4) {
            state = TypeDefs.GAMESTART;
            return true;
        }
        return false;
    }

    /**
     * this method generates an id between 1 and MAX_PLAYERS
     * -> so later it is easy to check who's turn it is
     *
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
     *
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
     *
     * @throws IOException
     */
    public void sendOtherPlayers() throws IOException {
        for (Player player : players.values()) {
            socketHandler.sendMessage(currentSession, JSON_commands.sendOtherPlayer(player));
        }
    }

    /**
     * this methods sends the initial Settings to all players at the very beginning of the game
     *
     * @throws IOException
     */
    public void sendInitialPlayerSettings() throws IOException {
        /*for (Map.Entry<String, Player> entry : players.entrySet()) {
            String key = entry.getKey();
            Player player = entry.getValue();
            //player.updateCardList();
            socketHandler.sendMessage(getSessionBySessionId(key), JSON_commands.sendInitialME(player));
            sendToAll(JSON_commands.sendInitialOthers(player));
        }*/

        for (Player player : players.values()) {
            sendToAll(JSON_commands.sendInitialOthers(player));
        }
    }

    public void sendInitialSetUp(Player player) throws IOException {
        ArrayList<Player> list = new ArrayList<>();
        for (Player other : players.values()) {
            if (player.getId() != other.getId()) {
                list.add(other);
            } else {
                WebSocketSession webSocketSession = getSessionByPlayerID(player.getId());
                if (webSocketSession != null) {
                    socketHandler.sendMessage(getSessionByPlayerID(player.getId()), JSON_commands.sendInitialME(player));
                }
            }
        }
        WebSocketSession session = getSessionByPlayerID(player.getId());
        if (session != null) {
            socketHandler.sendMessage(session, JSON_commands.sendInitialOthers(list));
        }
    }

    public WebSocketSession getSessionByPlayerID(int playerID) {
        for (Map.Entry<String, Player> entry : players.entrySet()) {
            String key = entry.getKey();
            Player player = entry.getValue();
            if (player.getId() == playerID) {
                return getSessionBySessionId(key);
            }

        }
        return null;
    }


    /**
     * this method returns the associated session to a specific sessionId
     *
     * @param id
     * @return
     */
    public WebSocketSession getSessionBySessionId(String id) {
        for (WebSocketSession session : sessions) {
            if (session != null) {
                if (id.equalsIgnoreCase(session.getId())) {
                    return session;
                }
            }
        }
        return null;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    /**
     * this method returns the associated player to a specific sessionId
     *
     * @param sessionId
     * @return
     */
    public Player getPlayerBySessionId(String sessionId) {
        for (Map.Entry<String, Player> entry : players.entrySet()) {
            String key = entry.getKey();
            Player player = entry.getValue();
            if (key.equalsIgnoreCase(sessionId)) {
                return player;
            }

        }
        return null;
    }

    /**
     * this method returns the associated player to a specific playerId
     *
     * @param id
     * @return
     */
    public Player getPlayerById(int id) {
        for (Player player : players.values()) {
            if (player.getId() == id) {
                return player;
            }
        }
        return null;
    }

    /**
     * this method determines the first player at the beginning of the game
     *
     * @return
     */
    public int getFirstPlayer() {
        Random random = new Random();
        int start = random.nextInt(MAX_PLAYER) + 1;
        //int start = random.ints(1,(MAX_PLAYER+1)).findFirst().getAsInt();;

        if (start == 0) {
            return getFirstPlayer();
        }
        currentPlayerId = start;
        return start;
    }

    /**
     * this method sends an update when the status changes
     *
     * @throws IOException
     */
    public void sendStatusupdatePlayer() throws IOException {
        for (WebSocketSession session : sessions) {
            socketHandler.sendMessage(session, JSON_commands.statusupdatePlayer(getPlayerBySessionId(session.getId())));
        }
    }

    public void sendStatusupdateOfAllPlayer() throws IOException {
        for (WebSocketSession session : sessions) {
            for (Player player : players.values()) {
                socketHandler.sendMessage(session, JSON_commands.statusupdatePlayer(player));
            }
        }
    }

    public void sendPictureOfOnePlayertoAll(Player player) throws IOException {
        for (WebSocketSession session : sessions) {
            socketHandler.sendMessage(session, JSON_commands.picturePlayer(player));
        }
    }

    public void sendSmileyOfOnePlayerToAll(Player player) throws IOException {
        for (WebSocketSession session : sessions) {
            socketHandler.sendMessage(session, JSON_commands.smileyPlayer(player));
        }
    }

    /**
     * this method sends the player who's next in line to play
     *
     * @throws IOException
     */
    public void sendNextPlayer() throws IOException {
        for (WebSocketSession session : sessions) {
            if (session == null && playWithKI) {
                handleKI("nextPlayer");
            } else {
                socketHandler.sendMessage(session, JSON_commands.sendNextPlayer(currentPlayerId));
            }
        }
    }

    /**
     * this method updates the player status
     */
    public void updatePlayerStatus() {
        for (Player player : players.values()) {
            if (player.getId() == currentPlayerId) {
                player.setStatus(TypeDefs.playing);
            } else {
                player.setStatus(TypeDefs.waiting);
            }
        }
    }

    /**
     * this method checks if everyone has looked up the initial 2 cards and is ready to play
     *
     * @return
     */
    public boolean checkIfEveryoneReady() {
        for (Player player : players.values()) {
            if (!player.getStatus().equalsIgnoreCase(TypeDefs.readyForGamestart)) {
                return false;
            }
        }
        return true;
    }

    /**
     * this player checks if a player is really on the turn
     *
     * @param player
     * @return
     */
    public boolean checkIfPlayerIsAuthorised(Player player) {
        if (player.getStatus().equalsIgnoreCase(TypeDefs.playing)) {
            return true;
        }
        return false;
    }

    /**
     * this method handles what happens when a player finishes his move
     *
     * @throws IOException
     */
    public void finishMove() throws IOException {
        currentPlayerId = getNextPlayerId();
        updatePlayerStatus();

    }

    private void finishRound() throws IOException {
        //calcScores();
        for (Player player : players.values()) {
            if (player != null) {
                player.calculatePoints();
            }
        }
        for (Player player : players.values()) {
            sendToAll(JSON_commands.sendScores(player));
        }


        if (terminated) {
            sendToAll(JSON_commands.sendEndGame(getWinner()));
            state = TypeDefs.GAMEEND;
            //TODO send Game End
            //remove this object in sockethandler
        } else {
            for (Player player : players.values()) {
                player.setStatus(TypeDefs.MATCHING);
            }
            startRound();
        }
    }

    /**
     * this method returns an id which is the next in line to play
     *
     * @return
     */
    public int getNextPlayerId() {
        int oldId = currentPlayerId;
        if (oldId == MAX_PLAYER) {
            return 1;
        } else {
            oldId++;
            return oldId;
        }
    }

    public boolean hasPlayerDrawn(int id) {
        if (lastDrawnPlayerId == id) {
            return true;
        } else {
            return false;
        }
    }

    public void mixCards() {
        availableCards = playedCards;
        for (Player player : players.values()) {
            for (int i = 0; i < player.getCards().size(); i++) {
                if (availableCards.get(i).equalsCard(player.getCards().get(i))) {
                    availableCards.remove(i);
                    i++;
                }
            }
        }
    }


    public int getGamestateID() {
        return gamestateID;
    }

    public void setGamestateID(int gamestateID) {
        this.gamestateID = gamestateID;
    }

    public boolean sessionAlreadyAdded(WebSocketSession newSession) {
        for (WebSocketSession session : sessions) {
            if (session != null) {
                if (session.getId() == newSession.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    public void saveAvatar(Player player) {
        for (Player socketHandlerPlayer : socketHandler.getConnectedPlayers().values()) {
            String nick = socketHandlerPlayer.getNick();
            if (nick != null) {
                if (socketHandlerPlayer.getNick().equalsIgnoreCase(player.getName())) {
                    player.setAvatarID(socketHandlerPlayer.getAvatarID());
                }
            } else {
                player.setAvatarID(1);
            }

        }
    }

    public boolean isPrivateParty() {
        return privateParty;
    }

    public void setPrivateParty(boolean privateParty) {
        this.privateParty = privateParty;
    }

    public Player getWinner() {
        ArrayList<Integer> scores = new ArrayList<Integer>();
        for (Player player : players.values()) {
            scores.add(player.getScore());
        }
        if (scores != null) {
            Collections.sort(scores);
            for (Player player : players.values()) {
                if (player.getScore() == scores.get(0)) {
                    return player;
                }
            }
        }
        return null;
    }

    public void handleKI(String action) throws IOException {
        Player me = players.get("KI");
        if (action.equalsIgnoreCase("nextPlayer")) {
            if (currentPlayerId == me.getId()) {
                KIJsonObject = JSON_commands.sendPickCardKI("");
                try {
                    handleTextMessage(null, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if (action.equalsIgnoreCase("decideForMove")) {

            pause(2);
            if (currentPickedCard.getValue() >= 7 && currentPickedCard.getValue() != 13) {
                KIJsonObject = JSON_commands.playPickedCardKI();
                try {
                    handleTextMessage(null, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                pause(2);
                if (!getRealPlayer().getCalledCabo()) {
                    KIJsonObject = decideForMove(me);
                    try {
                        handleTextMessage(null, null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    pause(3);
                }

                finishKIMove();

            } else {
                KIJsonObject = decideForMove(me);
                try {
                    handleTextMessage(null, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                pause(2);
                finishKIMove();
            }
        }
    }

    public boolean KIsTurn() {
        for (Player player : players.values()) {
            if (player.isKI()) {
                if (currentPlayerId == player.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    public Player returnKI() {
        for (Player player : players.values()) {
            if (player.isKI()) {
                return player;
            }
        }
        return null;
    }

    public JSONObject decideForMove(Player playerKI) throws JsonProcessingException {
        JSONObject jsonObject = null;
        switch (currentPickedCard.getValue()) {
            case -1:
                jsonObject = JSON_commands.swapPickedCardWithOwnCardsKI(returnHighestKnownCard(playerKI));
                updateKnownCardsOfKI(playerKI);
                break;
            case 0:
                jsonObject = JSON_commands.swapPickedCardWithOwnCardsKI(returnHighestKnownCard(playerKI));
                updateKnownCardsOfKI(playerKI);
                break;
            case 1:
                jsonObject = JSON_commands.swapPickedCardWithOwnCardsKI(returnHighestKnownCard(playerKI));
                updateKnownCardsOfKI(playerKI);
                break;
            case 2:
                jsonObject = JSON_commands.swapPickedCardWithOwnCardsKI(returnHighestKnownCard(playerKI));
                updateKnownCardsOfKI(playerKI);
                break;
            case 3:
                jsonObject = JSON_commands.swapPickedCardWithOwnCardsKI(returnHighestKnownCard(playerKI));
                updateKnownCardsOfKI(playerKI);
                break;
            case 4:
                jsonObject = JSON_commands.swapPickedCardWithOwnCardsKI(returnHighestKnownCard(playerKI));
                updateKnownCardsOfKI(playerKI);
                break;
            case 5:
                jsonObject = JSON_commands.playPickedCardKI();
                break;
            case 6:
                jsonObject = JSON_commands.playPickedCardKI();
                break;
            case 7:
                jsonObject = JSON_commands.useFunctionalityPeekKI(returnPeekCardForKI(playerKI));
                break;
            case 8:
                jsonObject = JSON_commands.useFunctionalityPeekKI(returnPeekCardForKI(playerKI));
                break;
            case 9:
                jsonObject = JSON_commands.useFunctionalitySpyKI(returnSpyCardForKI(playerKI), getRealPlayer());
                break;
            case 10:
                jsonObject = JSON_commands.useFunctionalitySpyKI(returnSpyCardForKI(playerKI), getRealPlayer());
                break;
            case 11:
                Card highestKnownCard = returnHighestKnownCard(playerKI);
                Card lowestKnownCardOfOther = returnLowestKnownCardOfOtherPlayer(playerKI);
                Player other = getRealPlayer();
                jsonObject = JSON_commands.useFunctionalitySwap(highestKnownCard, playerKI, lowestKnownCardOfOther, other);
                updateKnownListsAfterSwapping(playerKI, highestKnownCard, lowestKnownCardOfOther);
                break;
            case 12:
                Card highestKnownCard2 = returnHighestKnownCard(playerKI);
                Card lowestKnownCardOfOther2 = returnLowestKnownCardOfOtherPlayer(playerKI);
                Player other2 = getRealPlayer();
                jsonObject = JSON_commands.useFunctionalitySwap(highestKnownCard2, playerKI, lowestKnownCardOfOther2, other2);
                updateKnownListsAfterSwapping(playerKI, highestKnownCard2, lowestKnownCardOfOther2);
                break;
            case 13:
                jsonObject = JSON_commands.playPickedCardKI();
                break;
            default:
                jsonObject = JSON_commands.swapPickedCardWithOwnCardsKI(returnHighestKnownCard(playerKI));
                break;

        }

        return jsonObject;
    }

    public void removeCardofList(ArrayList<Card> cardList, Card removedCard) {
        if (cardList != null) {
            for (int i = 0; i < cardList.size(); i++) {
                if (cardList.get(i).equalsCard(removedCard)) {
                    cardList.remove(i);
                }
            }
        }
    }

    public void updateKnownCardsOfKI(Player playerKI) {
        removeCardofList(playerKI.getKnownCards(), returnHighestKnownCard(playerKI));
        playerKI.getKnownCards().add(currentPickedCard);
    }

    public void updateKnownListsAfterSwapping(Player playerKI, Card highestCard, Card lowestCard) {
        if (playerKI != null && highestCard != null && lowestCard != null && playerKI.getKnownCardsOfOther() != null && playerKI.getKnownCardsOfOther() != null) {
            for (int i = 0; i < playerKI.getKnownCards().size(); i++) {
                if (playerKI.getKnownCards().get(i).equalsCard(highestCard)) {
                    playerKI.getKnownCards().remove(i);
                    playerKI.getKnownCards().add(i, lowestCard);
                }
            }
            for (int i = 0; i < playerKI.getKnownCardsOfOther().size(); i++) {
                if (playerKI.getKnownCardsOfOther().get(i).equalsCard(lowestCard)) {
                    playerKI.getKnownCardsOfOther().remove(i);
                    playerKI.getKnownCardsOfOther().add(i, highestCard);
                }
            }
        }
    }

    public void pause(double seconds) {
        try {
            Thread.sleep((long) (seconds * 1000));
        } catch (InterruptedException e) {
        }
    }

    public Card returnHighestKnownCard(Player player) {
        ArrayList<Integer> cardValues = new ArrayList<>();
        if (player.getKnownCards() != null) {
            for (Card card : player.getKnownCards()) {
                cardValues.add(card.getValue());
            }
        }
        Collections.sort(cardValues);
        if (cardValues != null && cardValues.size() != 0) {
            int highestValue = cardValues.get(cardValues.size() - 1);
            for (Card card : player.getKnownCards()) {
                if (card.getValue() == highestValue) {
                    return card;
                }
            }
        }
        return player.getCards().get(0);
    }

    public Card returnLowestKnownCardOfOtherPlayer(Player ki) {
        ArrayList<Integer> cardValues = new ArrayList<>();
        if (ki.getKnownCardsOfOther() != null) {
            for (Card card : ki.getKnownCardsOfOther()) {
                cardValues.add(card.getValue());
            }
        }
        Collections.sort(cardValues);
        if (cardValues != null && cardValues.size() != 0) {
            int lowestValue = cardValues.get(0);
            for (Card card : ki.getKnownCardsOfOther()) {
                if (card.getValue() == lowestValue) {
                    return card;
                }
            }
        }
        return ki.getCards().get(0);
    }

    public void finishKIMove() throws IOException {
        JSONObject jsonObject = JSON_commands.sendFinishMoveKI("");
        KIJsonObject = jsonObject;
        handleTextMessage(null, null);
    }

    public Player getRealPlayer() {
        for (Player player : players.values()) {
            if (!player.isKI()) {
                return player;
            }
        }
        //TODO end game wenn null
        return null;
    }

    public Card returnPeekCardForKI(Player ki) {
        if (ki.getCards() != null && ki.getCards().size() == 4) {
            if (ki.getKnownCards().size() == 4) {
                return ki.getCards().get(0);
            }
            if (ki.getKnownCards().size() == 3) {
                ki.getKnownCards().add(ki.getCards().get(3));
                return ki.getCards().get(3);
            }
            if (ki.getKnownCards().size() == 2) {
                ki.getKnownCards().add(ki.getCards().get(2));
                return ki.getCards().get(2);
            }
            if (ki.getKnownCards().size() == 1) {
                ki.getKnownCards().add(ki.getCards().get(1));
                return ki.getCards().get(1);
            }
            if (ki.getKnownCards().size() == 0) {
                ki.getKnownCards().add(ki.getCards().get(0));
                return ki.getCards().get(0);
            }
        }
        return currentPickedCard;
    }

    public Card returnSpyCardForKI(Player ki) {
        if (ki.getKnownCardsOfOther() != null) {
            if (ki.getKnownCardsOfOther().size() == 0) {
                ki.getKnownCardsOfOther().add(getRealPlayer().getCards().get(0));
                return getRealPlayer().getCards().get(0);
            }
            if (ki.getKnownCardsOfOther().size() == 1) {
                ki.getKnownCardsOfOther().add(getRealPlayer().getCards().get(1));
                return getRealPlayer().getCards().get(1);
            }
            if (ki.getKnownCardsOfOther().size() == 2) {
                ki.getKnownCardsOfOther().add(getRealPlayer().getCards().get(2));
                return getRealPlayer().getCards().get(2);
            }
            if (ki.getKnownCardsOfOther().size() == 3) {
                ki.getKnownCardsOfOther().add(getRealPlayer().getCards().get(3));
                return getRealPlayer().getCards().get(3);
            }
            if (ki.getKnownCardsOfOther().size() == 4) {
                ki.getKnownCardsOfOther().add(getRealPlayer().getCards().get(4));
                return getRealPlayer().getCards().get(0);
            }

        }
        return currentPickedCard;
    }

    public void updateKnownListsOfKIaferRealPlayerMove() {
        Player ki = returnKI();
        Player realPlayer = getRealPlayer();

        for (int i = 0; i < ki.getKnownCardsOfOther().size(); i++) {
            if (!ki.getKnownCardsOfOther().get(i).equalsCard(realPlayer.getCards().get(i))) {
                ki.getKnownCardsOfOther().remove(i);
                return;
            }
        }
    }

    /*****************************************
     * Copy and Paste from CardSuiteManager
     ****************************************
     */
    // reference: https://www.youtube.com/watch?v=aCo-iNedw2g

    final static int CARD_NUMBER = 13 * 4;
    private final static int DISTRIBUTION_CARD_NUMBER_AT_BEGINNING = 4;

    // The cards in available pile, face-down, never exposed these cards to clients
    private ArrayList<Card> availableCards = null;
    // The cards drawn from available card pile, they may be in discarded card pile, clients' decks.
    private ArrayList<Card> playedCards = null;
    // The cards discarded by clients, face-up
    private ArrayList<Card> discardedCards = null;

    // The game is terminate or not. It mean at least one client reaches more than 100 score.
    private boolean terminated = false;

    // Logger handler
    private final static Logger logger = Logger.getLogger(Gamestate.class.getName());

    public void generateCards(boolean shouldShuffle) {
        if (this.terminated) {
            logger.log(Level.INFO, "The game is terminated, no need to generate cards anymore.");
            this.availableCards = null;
            this.playedCards = null;
            this.discardedCards = null;
            return;
        }
        this.availableCards = new ArrayList<>();
        this.playedCards = new ArrayList<>();
        this.discardedCards = new ArrayList<>();

        this.generateUnShuffledCards();
        this.shuffleCards(shouldShuffle);
        logger.log(Level.INFO, "The cards is generated successfully.");

    }

    /**
     * Generate cards
     */
    private void generateUnShuffledCards() {
        for (int i = -1; i <= 13; i++) {
            if (i == 0 || i == 13) {
                this.availableCards.add(new Card(i, TypeDefs.SPADE));
                this.availableCards.add(new Card(i, TypeDefs.CLUB));
            } else if (i == 7 || i == 8) {
                // NOTE: 7 or 8: 'peek'
                this.availableCards.add(new Card(i, TypeDefs.SPADE, TypeDefs.PEEK));
                this.availableCards.add(new Card(i, TypeDefs.CLUB, TypeDefs.PEEK));
                this.availableCards.add(new Card(i, TypeDefs.HEART, TypeDefs.PEEK));
                this.availableCards.add(new Card(i, TypeDefs.DIAMOND, TypeDefs.PEEK));
            } else if (i == 9 || i == 10) {
                // NOTE: 9 or 10: 'spy'
                this.availableCards.add(new Card(i, TypeDefs.SPADE, TypeDefs.SPY));
                this.availableCards.add(new Card(i, TypeDefs.CLUB, TypeDefs.SPY));
                this.availableCards.add(new Card(i, TypeDefs.HEART, TypeDefs.SPY));
                this.availableCards.add(new Card(i, TypeDefs.DIAMOND, TypeDefs.SPY));
            } else if (i == 11 || i == 12) {
                // 11 or 12: 'swap'
                this.availableCards.add(new Card(i, TypeDefs.SPADE, TypeDefs.SWAP));
                this.availableCards.add(new Card(i, TypeDefs.CLUB, TypeDefs.SWAP));
                this.availableCards.add(new Card(i, TypeDefs.HEART, TypeDefs.SWAP));
                this.availableCards.add(new Card(i, TypeDefs.DIAMOND, TypeDefs.SWAP));
            } else {
                this.availableCards.add(new Card(i, TypeDefs.SPADE));
                this.availableCards.add(new Card(i, TypeDefs.CLUB));
                this.availableCards.add(new Card(i, TypeDefs.HEART));
                this.availableCards.add(new Card(i, TypeDefs.DIAMOND));
            }
        }
        logger.log(Level.FINER, "Generate plain cards successfully.");
    }

    /**
     * Shuffle cards
     *
     * @param shouldShuffle the flag to show whether the cards should be shuffled
     */
    private void shuffleCards(boolean shouldShuffle) {
        if (shouldShuffle) {
            logger.log(Level.FINER, "Shuffling the cards");
            Collections.shuffle(this.availableCards);
        } else {
            logger.log(Level.FINER, "We don't want to shuffle the cards");
        }
    }

    /**
     * Terminate the game
     */
    public void terminate() {
        this.terminated = true;
    }

    /**
     * @TODO Make this function easy to extend
     * Calculate the scores once caboed
     */
    public void calcScores() {
        // ArrayList<Player> allPlayers = this.cardSuiteManager.getPlayers();
        // Case1: Checking the special case, (0, 0, 13, 13)
        for (Player player : players.values()) {
            if (player.getCards().size() == 4) {
                Card ca = player.getCards().get(0);
                Card cb = player.getCards().get(1);
                Card cc = player.getCards().get(2);
                Card cd = player.getCards().get(3);

                boolean special = false;
                if (ca.getValue() == cb.getValue() && (ca.getValue() == 0 || ca.getValue() == 13)) {
                    if (cc.getValue() == cd.getValue() && (cd.getValue() == 0 || cd.getValue() == 13)) {
                        // Player has special cards (0, 0, 13, 13) or (13, 13, 0, 0)
                        special = true;
                    }
                } else if (ca.getValue() == cc.getValue() && (ca.getValue() == 0 || ca.getValue() == 13)) {
                    if (cb.getValue() == cd.getValue() && (cd.getValue() == 0 || cd.getValue() == 13)) {
                        // Player has special cards (0, 13, 0, 13) or (13, 0, 13, 0)
                        special = true;
                    }
                } else if (ca.getValue() == cd.getValue() && (ca.getValue() == 0 || ca.getValue() == 13)) {
                    if (cb.getValue() == cc.getValue() && (cb.getValue() == 0 || cb.getValue() == 13)) {
                        // Player has special cards (0, 13, 13, 0) or (13, 0, 0, 13)
                        special = true;
                    }
                }
                if (special) {
                    for (Player _player : players.values()) {
                        if (player.getId() != _player.getId()) {
                            _player.setScore(_player.getScore() + 50);
                            if (_player.getScore() == 100 || _player.getScore() == 50) {
                                _player.setScore(_player.getScore() / 2);
                            }
                            if (_player.getScore() >= maxPoints) {
                                this.terminate();
                            }
                        }
                    }
                    // Inside speical case
                    return;
                }
            }
        }

        // Case 2: common case
        int smallestPoint = Integer.MAX_VALUE;

        for (Player player : players.values()) {
            if (smallestPoint > player.calculatePoints()) {
                smallestPoint = player.calculatePoints();
            }
        }
        for (Player player : players.values()) {
            if (player.getCalledCabo()) {
                if (smallestPoint != player.calculatePoints()) {
                    player.setScore(player.calculatePoints() * 2 + player.getScore());
                } else {
                    // get zero score this time
                }
            } else {
                player.setScore(player.getScore() + player.calculatePoints());

            }
            if (player.getScore() == 100 || player.getScore() == 50) {
                player.setScore(player.getScore() / 2);
            }
            if (player.getScore() > maxPoints) {
                this.terminate();
            }
        }
    }

    /**
     * Draw a card from `availableCards` pile
     *
     * @return a card retrieve from `availableCard`
     */
    public Card takeFirstCardFromAvailableCards() {
        Card card = this.availableCards.remove(0);
        this.playedCards.add(card);
        return card;
    }

    public ArrayList<Card> getAvailableCards() {
        return this.availableCards;
    }

    public ArrayList<Card> getDiscardedCards() {
        return this.discardedCards;
    }

    public ArrayList<Card> getPlayedCards() {
        return this.playedCards;
    }

    /**
     * Distribute cards to all participated players;
     */
    public void distributeCardsAtBeginning() {
        players.forEach((k, player) -> {
            player.reset();
            for (int i = 0; i < DISTRIBUTION_CARD_NUMBER_AT_BEGINNING; i++) {
                player.drawCard();
            }
        });
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
