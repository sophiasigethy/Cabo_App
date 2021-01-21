package msp.group3.caboclient;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class InGameChatFragment extends Fragment {

    public TextView textMsg;
    public EditText textInput;
    protected ListView chatMessageListView;
    protected ArrayList<ChatMessage> chatMessagesList= new ArrayList();
    protected static ChatArrayAdapter adapter;

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
        textMsg = view.findViewById(R.id.messages);
        textInput = view.findViewById(R.id.editText);
        chatMessageListView = view.findViewById(R.id.chat_list_view);

        Button sendButton = view.findViewById(R.id.button);

        adapter= new ChatArrayAdapter(getActivity().getApplicationContext(), R.layout.my_chat_message);
        chatMessageListView.setAdapter(adapter);


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentText = textInput.getText().toString();
                Player sender=((InGameActivity) getActivity()).me;
                String text = "(" + ((InGameActivity) getActivity()).me.getName() + "): " + currentText;
                //String text = currentText;
                textInput.setText("");
                try {

                    ((InGameActivity) getActivity()).webSocketClient.send(String.valueOf(JSON_commands.chatMessage(currentText, sender)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                chatMessageListView.setSelection(adapter.getCount() - 1);
            }
        });
    }


}
