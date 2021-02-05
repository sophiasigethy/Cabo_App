package msp.group3.caboclient.CaboView;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import msp.group3.caboclient.CaboController.DatabaseOperation;
import msp.group3.caboclient.CaboController.JSON_commands;
import msp.group3.caboclient.CaboModel.Player;
import msp.group3.caboclient.R;

import org.json.JSONException;
import java.util.ArrayList;

public class InGameChatFragment extends Fragment {

    public TextView textMsg;
    public EditText textInput;
    protected ListView chatMessageListView;
    protected ArrayList<ChatMessage> chatMessagesList= new ArrayList();
    protected ChatAdapter adapter;
    protected SharedPreferences sharedPref;
    protected Activity ingameActivity;

    public InGameChatFragment() {
        super(R.layout.ingamechat_fragment);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.ingamechat_fragment, parent, false);
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Setup any handles to view objects here
        // EditText etFoo = (EditText) view.findViewById(R.id.etFoo);
        textInput = view.findViewById(R.id.editText);
        chatMessageListView = view.findViewById(R.id.chat_list_view);

        ImageButton sendButton = view.findViewById(R.id.button);

        ChatMessage welcomeMsg = new ChatMessage("BOT1", "Welcome!", true, R.drawable.robot);
        ChatMessage welcomeMsg2 = new ChatMessage("BOT2", "Welcome!", false, R.drawable.robot);

        chatMessagesList.add(welcomeMsg);
        chatMessagesList.add(welcomeMsg2);

        adapter = new ChatAdapter(view.getContext(), chatMessagesList);
        chatMessageListView.setAdapter(adapter);
        ingameActivity = getActivity();
        sharedPref = ingameActivity.getSharedPreferences(
                R.string.preference_file_key + "", Context.MODE_PRIVATE);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentText = textInput.getText().toString();
                Player sender=((InGameActivity) getActivity()).me;
                String text = "(" + ((InGameActivity) getActivity()).me.getName() + "): " + currentText;
                //String text = currentText;
                textInput.setText("");
                playSound(R.raw.send_message);
                try {
                    ((InGameActivity) getActivity()).webSocketClient.send(String.valueOf(JSON_commands.chatMessage(currentText, sender)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void scrollMyListViewToBottom() {
        chatMessageListView.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                chatMessageListView.setSelection(adapter.getCount() - 1);
            }
        });
    }

    /**
     * This function is responsible for playing sounds in this Activity
     * @param sound: The R.raw.*ID* of the sound you want to play
     * */
    public void playSound(int sound) {
        if (DatabaseOperation.getDao().getSoundsPlaying(sharedPref).equals("Play")) {
            MediaPlayer soundPlayer = MediaPlayer.create(ingameActivity, sound);
            soundPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mediaPlayer) {
                    soundPlayer.stop();
                    soundPlayer.release();
                }
            });
            soundPlayer.setVolume(90, 90);
            soundPlayer.start();
        }
    }
}
