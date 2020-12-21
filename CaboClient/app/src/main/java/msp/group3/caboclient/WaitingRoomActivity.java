package msp.group3.caboclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.json.JSONException;
import org.json.JSONObject;

public class WaitingRoomActivity extends AppCompatActivity implements Communicator.CommunicatorCallback {

    private EditText editText;
    private Button button;
    private WebSocketClient mWebSocketClient;
    private Communicator communicator;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);

        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.messages);



        communicator = Communicator.getInstance(this);
        mWebSocketClient = communicator.getmWebSocketClient();
        communicator.setLoginActivity(this);

    }

    public void sendMessage(View view) throws JSONException {
        String message = editText.getText().toString();
        JSONObject jsonObject = JSON_commands.chatMessage(message);
        mWebSocketClient.send(jsonObject.toString());
        editText.setText("");
    }


    @Override
    public void handelTextMessage(String message) throws JSONException {
            JSONObject jsonObject = new JSONObject(message);


            // this is received when another client sent a chat message
            if (jsonObject.has("chatMessage")) {
                String mes = jsonObject.get("chatMessage").toString();
                showText(mes);

            }
    }

    private void showText(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // mTextView.setText(mTextView.getText() + "\n" + getTextMessage());
                textView.setText(textView.getText() + "\n" + message);
                if (textView.getLayout() != null) {
                    final int scrollAmount = textView.getLayout().getLineTop(textView.getLineCount()) - textView.getHeight();
                    // if there is no need to scroll, scrollAmount will be <=0
                    if (scrollAmount > 0)
                        textView.scrollTo(0, scrollAmount);
                    else
                        textView.scrollTo(0, 0);
                }
            }
        });

    }
}