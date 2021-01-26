package msp.group3.caboclient;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;

public class CustomSearchDialog {
    public void showDialog(Activity activity, Player me, ArrayList<Player> allUsers, Communicator communicator){
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.search_dialog);

        TextView title = (TextView) dialog.findViewById(R.id.text_dialog_title);
        EditText searchNick = (EditText) dialog.findViewById(R.id.search_friend_input);

        Button dialogButtonSearch = (Button) dialog.findViewById(R.id.btn_dialog_search);
        dialogButtonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchNick.getText().toString().isEmpty()) {
                    if (!(me.getFriendsNicknames().contains(searchNick.getText().toString()))) {
                        if (allUsers.size() > 0) {
                            for (Player user : allUsers) {
                                if (user.getNick().toLowerCase().equals(
                                        searchNick.getText().toString().toLowerCase().trim())) {
                                    Toast.makeText(activity,
                                            "Friendrequest sent to " + user.getDbID(), Toast.LENGTH_LONG);
                                    try {
                                        communicator.sendMessage(JSON_commands.sendFriendRequest(me, user));
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    dialog.dismiss();
                                    break;
                                }
                            }
                            Toast.makeText(activity,
                                    "No such user found: " + searchNick.getText().toString(),
                                    Toast.LENGTH_LONG);
                            dialog.dismiss();
                        }
                    } else
                        Toast.makeText(activity,
                                searchNick.getText().toString() + " is already your friend",
                                Toast.LENGTH_LONG);
                }
                Toast.makeText(activity,
                        "You have to enter a Name", Toast.LENGTH_LONG);
                dialog.cancel();
            }
        });

        Button dialogButtonCancel = (Button) dialog.findViewById(R.id.btn_dialog_cancel);
        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.cancel();
            }
        });

        dialog.show();

    }
}
