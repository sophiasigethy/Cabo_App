package myServer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Gamestate {

    // contains websocketsession-id and the associated player object
    public HashMap<String, Player> players = new HashMap<String, Player>();
    private final int MAX_PLAYER = 2;
    private int test = 0;
    // determines how many players are already registered
    private int countPlayer = 0;
    //status of the game- see Type Defs for all 3 state
    private String state = TypeDefs.MATCHING;

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
            socketHandler.sendMessage(session, JSON_commands.notAccepted(msg));
        } else {
            //String text = "Welcome to the game. Please state your username";
            //socketHandler.sendMessage(session, JSON_commands.Hallo(text));
            socketHandler.sendMessage(session, JSON_commands.accepted("accepted"));
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
        JSONObject jsonObject = getMessage(message.getPayload());

        if (jsonObject.has("welcomeMessage")) {
            String text = "Welcome to the game. Please state your username";
            socketHandler.sendMessage(session, JSON_commands.Hallo(text));
        }
        //client sent the username he would like to have
        if (jsonObject.has("username")) {
            if (isMaxPlayer()) {
                String msg = "Sorry enough players have registered in the meantime. You can no longer join this game.";
                socketHandler.sendMessage(session, JSON_commands.notAccepted(msg));
            } else {
                String name = jsonObject.get("username").toString();
                if (isExist(name)) {
                    //sends username is already in use
                    socketHandler.sendMessage(session, JSON_commands.usernameInUse(name));
                } else {
                    addPlayer(session, name);
                    sendOtherPlayers();
                    if (isMaxPlayer()) {
                        //sends gamestart and players start to play cabo
                        sendToAll(JSON_commands.statusupdateServer(state));
                       /* //send 4 cards to every client
                        distributeCardsAtBeginning();
                        sendInitialCards();*/
                    } else {
                        //send status update to inform client that system is waiting for other players
                        socketHandler.sendMessage(session, JSON_commands.statusupdateServer(state));
                        //sendOtherPlayers();
                    }
                }
            }
        }
        // client sent chat message
        if (jsonObject.has("chatMessage")) {
            sendToAll(jsonObject);
            //sendToOne(jsonObject);
        }

        /*if (jsonObject.has("statusupdate")) {
            String status = jsonObject.get("statusupdate").toString();
            if (status.equalsIgnoreCase(TypeDefs.readyForGamestart)) {
                getPlayerBySessionId(session.getId()).setStatus(TypeDefs.readyForGamestart);
                if (checkIfEveryoneReady()) {
                    //send which player's turn it is
                    int nextPlayerId = getFirstPlayer();
                    updatePlayerStatus(nextPlayerId);
                    sendStatusupdatePlayer();
                    sendNextPlayer(nextPlayerId);
                }
            }
        }*/
        if (jsonObject.has("startGameForAll")) {
            sendToAll(JSON_commands.startGame("start"));

        }
        if (jsonObject.has("askForInitialSettings")) {
            // sendToAll(JSON_commands.sendMAXPlayer(MAX_PLAYER));
            socketHandler.sendMessage(session, JSON_commands.sendMAXPlayer(MAX_PLAYER));
            initialSetUp++;
            if (initialSetUp == MAX_PLAYER) {
                generateCards(true);
                //send 4 cards to every client
                distributeCardsAtBeginning();

                for (Player player : players.values()) {
                    sendInitialSetUp(player);
                }
            }


        }

        if (jsonObject.has("memorizedCards")) {
            Player player= getPlayerBySessionId(session.getId());
            if (!(player.getStatus().equalsIgnoreCase(TypeDefs.waiting)||player.getStatus().equalsIgnoreCase(TypeDefs.playing))){
                getPlayerBySessionId(session.getId()).setStatus(TypeDefs.readyForGamestart);
            }
            if (checkIfEveryoneReady()) {
                //send which player's turn it is
                currentPlayerId = getFirstPlayer();
                updatePlayerStatus();
                sendStatusupdatePlayer();
                sendNextPlayer();
            }
        }

        if (jsonObject.has("pickCard")) {

            if (checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()))) {
                //TODO draw card ? discard card?
                //getPlayerBySessionId(session.getId()).drawCard();
                //currentPickedCard = takeFirstCardFromAvailableCards();
                Player currentPlayer = getPlayerBySessionId(session.getId());
                if (currentPlayer != null) {
                    //currentPickedCard = availableCards.get(0);
                    currentPickedCard = new Card(8, "", "");
                    // Player firstPlayer = getPlayerById(currentPlayerId);
                    socketHandler.sendMessage(session, JSON_commands.sendFirstCard(currentPickedCard));
                }
            }
        }

        if (jsonObject.has("swapPickedCardWithOwnCards")) {
            if (checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()))) {
                JSONObject js = jsonObject.getJSONObject("swapPickedCardWithOwnCards");
                String json = js.get("card").toString();
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Card ownCard = objectMapper.readValue(json, Card.class);
                Player currentPlayer = getPlayerBySessionId(session.getId());

                currentPlayer.swapWithOwnCard(ownCard, currentPickedCard);
                sendToAll(JSON_commands.sendDiscardedCard(ownCard));
                sendToAll(JSON_commands.sendUpdatePlayer(currentPlayer));
            }
        }

        if (jsonObject.has("playPickedCard")) {
            if (checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()))) {
                takeFirstCardFromAvailableCards();
                availableCards.remove(0);
                playedCards.add(currentPickedCard);
                sendToAll(JSON_commands.sendPlayedCard(currentPickedCard));
            }
        }

        if (jsonObject.has("useFunctionalityPeek")) {
            if (checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()))) {
                JSONObject js = jsonObject.getJSONObject("useFunctionalityPeek");
                String json = js.get("card").toString();
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Card card = objectMapper.readValue(json, Card.class);

                sendToAll(JSON_commands.useFunctionalityPeek(card));
                Player currentPlayer = getPlayerBySessionId(session.getId());

            }
        }
        if (jsonObject.has("useFunctionalitySpy")) {
            if (checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()))) {
                JSONObject js = jsonObject.getJSONObject("useFunctionalitySpy");
                String json1 = js.get("card").toString();
                String json3 = js.get("spyedPlayer").toString();

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                Card card = objectMapper.readValue(json1, Card.class);
                Player spyedPlayer = objectMapper.readValue(json3, Player.class);


                sendToAll(JSON_commands.useFunctionalitySpy(card, spyedPlayer));
                Player currentPlayer = getPlayerBySessionId(session.getId());

            }
        }
        if (jsonObject.has("useFunctionalitySwap")) {
            if (checkIfPlayerIsAuthorised(getPlayerBySessionId(session.getId()))) {
                JSONObject js = jsonObject.getJSONObject("useFunctionalitySwap");
                String json = js.get("card1").toString();
                String json2 = js.get("card2").toString();
                String json3 = js.get("player1").toString();
                String json4 = js.get("player1").toString();
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

            }
        }

        if (jsonObject.has("finishMove")) {
            Player player= getPlayerBySessionId(session.getId());
            if(checkIfPlayerIsAuthorised(player)) {
                finishMove();
                sendStatusupdatePlayer();
                sendToAll(JSON_commands.sendNextPlayer(currentPlayerId));
            }

        }

        if (jsonObject.has("cabo")) {
            Player currentPlayer = getPlayerBySessionId(session.getId());
            currentPlayer.setCalledCabo(true);
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
        this.players.put(webSocketSession.getId(), newPlayer);
        //player is informed that he can join the game
        socketHandler.sendMessage(webSocketSession, JSON_commands.Welcome(newPlayer));
        informOtherPlayers(JSON_commands.newPlayer(newPlayer));
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

    /**
     * this method informs ALL players about an event
     *
     * @param jsonObject
     * @throws IOException
     */
    public void sendToAll(JSONObject jsonObject) throws IOException {
        for (WebSocketSession webSocketSession : sessions) {
            socketHandler.sendMessage(webSocketSession, jsonObject);
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
                if (player.getName().equalsIgnoreCase(name)) {
                    return true;
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
        if (players.size() == MAX_PLAYER) {
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
                socketHandler.sendMessage(getSessionByPlayerID(player.getId()), JSON_commands.sendInitialME(player));
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
            if (id.equalsIgnoreCase(session.getId())) {
                return session;
            }
        }
        return null;
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

    /**
     * this method sends the player who's next in line to play
     *
     * @throws IOException
     */
    public void sendNextPlayer() throws IOException {
        for (WebSocketSession session : sessions) {
            socketHandler.sendMessage(session, JSON_commands.sendNextPlayer(currentPlayerId));
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
        if (getPlayerById(currentPlayerId).getCalledCabo()) {
            //TODO finish Game
            calcScores();
            for (WebSocketSession session : sessions) {
                socketHandler.sendMessage(session, JSON_commands.sendScores(getPlayerBySessionId(session.getId())));
            }
        } else {
            currentPlayerId = getNextPlayerId();
            updatePlayerStatus();
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
        for (int i = 0; i <= 13; i++) {
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
    private void terminate() {
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
                            if (_player.getScore() > 100) {
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
            if (player.getScore() > 100) {
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
}




