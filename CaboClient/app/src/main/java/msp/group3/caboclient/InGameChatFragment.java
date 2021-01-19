package msp.group3.caboclient;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.json.JSONException;
import org.w3c.dom.Text;

public class InGameChatFragment extends Fragment {

    public TextView textMsg;
    public EditText textInput;

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
        Button sendButton = view.findViewById(R.id.button);


        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentText = textInput.getText().toString();
                String text = "(" + ((InGameActivity) getActivity()).me.getName() + "): " + currentText;
                textInput.setText("");
                try {

                    ((InGameActivity) getActivity()).webSocketClient.send(String.valueOf(JSON_commands.chatMessage(text)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
