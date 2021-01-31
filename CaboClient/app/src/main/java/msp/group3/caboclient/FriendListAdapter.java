package msp.group3.caboclient;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListAdapter extends ArrayAdapter {
    int NOT_FOUND = 32000;
    Context context;
    ArrayList<Player> myFriendList;
    ArrayList<Player> party;
    private static LayoutInflater inflater = null;
    private Player me;
    private Communicator communicator;

    public FriendListAdapter(Context context, Player me, ArrayList<Player> party, Communicator communicator) {
        super(context, 0, me.getFriendList());
        this.context = context;
        this.myFriendList = me.getFriendList();
        this.me = me;
        this.party = party;
        this.communicator = communicator;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public int getPlayerIndex(Player player) {
        for (int i = 0; i < myFriendList.size(); i++) {
            if (myFriendList.get(i).getDbID().equals(player.getDbID()) && myFriendList.get(i).getNick().equals(player.getNick())) {
                return i;
            }
        }
        return NOT_FOUND;
    }

    @Override
    public int getCount() {
        return myFriendList.size();
    }

    @Override
    public Object getItem(int i) {
        return myFriendList.get(i);
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
        playerIcon.setImageResource(myFriendList.get(i).getAvatarIcon());
        TextView player_nick = (TextView) vi.findViewById(R.id.friendlist_name);
        player_nick.setText(myFriendList.get(i).getNick());
        ImageView status = (ImageView) vi.findViewById(R.id.friendlist_status);
        if (myFriendList.get(i).getOnline()){
            status.setImageResource(R.drawable.online);
        }
        else{
            status.setImageResource(R.drawable.offline);
        }

        ImageButton invite_btn = (ImageButton) vi.findViewById(R.id.friendlist_invite);
        if (isInParty(myFriendList.get(i))) {
            invite_btn.setOnClickListener(null);
            invite_btn.setImageResource(R.drawable.partyhat);
            invite_btn.setPadding(0, 0, 0,0);
        } else {
            invite_btn.setImageResource(R.drawable.send);
            invite_btn.setBackgroundResource(R.drawable.zoom_button_bg);
            invite_btn.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            invite_btn.setPadding(8, 8, 8,8);
            invite_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("SendPartyRequest", "click");
                    //if (me.getFriendList().size()<4){ //todo müsste das nicht party size sein?
                    if(party.size()<4){
                        try {
                            communicator.sendMessage(JSON_commands.sendPartyRequest(me, myFriendList.get(i)));
                            Log.e("SendPartyRequest", "Send Party Invitation");
                            Toast.makeText(context, "Invitation sent to "+myFriendList.get(i).getNick(),
                                    Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Log.e("SendPartyRequest", "Could not send Party Invitation");
                        }
                    }else{
                        //TODO: Toast that already 4 people in the party
                        Toast.makeText(context, "Your party is full",
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        return vi;
    }

    private boolean isInParty(Player player){
        for(Player partyPlayer : party){
            if(partyPlayer.getNick().equalsIgnoreCase(player.getNick())){
                return true;
            }
        }
        return false;
    }
}
