package myServer;

import org.json.JSONObject;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws InterruptedException, IOException {
        gamestate.handleTextMessage(session, message);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (gamestate == null)
            gamestate = new Gamestate(this);
        gamestate.connectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closedStatus) throws Exception {
        gamestate.afterConnectionClosed(session);
    }


    public void sendMessage(WebSocketSession webSocketSession, JSONObject jsonMsg) throws IOException {
        webSocketSession.sendMessage(new TextMessage(jsonMsg.toString()));
    }

}
