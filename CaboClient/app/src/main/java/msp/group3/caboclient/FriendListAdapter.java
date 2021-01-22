package msp.group3.caboclient;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.json.JSONException;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListAdapter extends ArrayAdapter {
    int NOT_FOUND = 32000;
    Context context;
    ArrayList<Player> friends;
    ArrayList<Player> party;
    private static LayoutInflater inflater = null;
    private Player me;
    private Communicator communicator;

    public FriendListAdapter(Context context, Player me, ArrayList<Player> party, Communicator communicator) {
        super(context, 0, me.getFriendList());
        this.context = context;
        this.friends = me.getFriendList();
        this.me = me;
        this.party = party;
        this.communicator = communicator;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public int getPlayerIndex(Player player) {
        for (int i = 0; i < friends.size(); i++) {
            if (friends.get(i).getDbID().equals(player.getDbID()) && friends.get(i).getNick().equals(player.getNick())) {
                return i;
            }
        }
        return NOT_FOUND;
    }

    @Override
    public int getCount() {
        return friends.size();
    }

    @Override
    public Object getItem(int i) {
        return friends.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vi = view;
        if (vi == null)
            vi = inflater.inflate(R.layout.friendlist_item, null);
        CircleImageView playerIcon = (CircleImageView) vi.findViewById(R.id.friendlist_image);
        playerIcon.setImageResource(friends.get(i).getAvatar());
        TextView player_nick = (TextView) vi.findViewById(R.id.friendlist_name);
        player_nick.setText(friends.get(i).getNick());
        ImageView status = (ImageView) vi.findViewById(R.id.friendlist_status);
        //TODO Check why online status does not work
        if (friends.get(i).isOnline()){
            status.setImageResource(R.drawable.online);
            //status.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_green));
        }
        else{
            status.setImageResource(R.drawable.offline);
            //status.setBackground(ContextCompat.getDrawable(context, R.drawable.circle_red));
        }

        ImageButton invite_btn = (ImageButton) vi.findViewById(R.id.friendlist_invite);
        if (party.contains(friends.get(i))) {
            //TODO Check why Icon is not changed
            invite_btn.setImageResource(R.drawable.partyhat);
        } else {
            invite_btn.setImageResource(R.drawable.ic_baseline_add_circle_outline_24);
            invite_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        communicator.sendMessage(JSON_commands.sendPartyRequest(me, friends.get(i)));
                    } catch (JSONException e) {
                        Log.e("SendPartyRequest", "Could not send Party Invitation");
                    }
                }
            });
        }
        return vi;
    }
}
