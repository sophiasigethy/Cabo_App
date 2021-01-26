package msp.group3.caboclient;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewFriendListAdapter extends BaseAdapter {

    private ArrayList<Player> partyList = new ArrayList<Player>();
    ArrayList<Player> friends;
    private Context context;
    private LayoutInflater layoutinflater;
    private Player me;
    private Communicator communicator;

    public NewFriendListAdapter(Context context, ArrayList<Player> partyList, Player me) {
        this.context = context;
        this.partyList = partyList;
        this.me = me;
        this.friends = me.getFriendList();
        this.layoutinflater = LayoutInflater.from(context);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getCount() {
        return this.partyList.size();
    }

    public Player getItem(int index) {
        return this.partyList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Player friend = this.partyList.get(position);
        holder = new ViewHolder();


        if (convertView == null) {
            convertView = layoutinflater.inflate(R.layout.friendlist_item, null);
            convertView.setTag(holder);

        } else {
            holder = (NewFriendListAdapter.ViewHolder) convertView.getTag();
        }
        holder.friendAvatar = (CircleImageView) convertView.findViewById(R.id.friendlist_image);
        holder.friendAvatar.setImageResource(friend.getAvatar());
        holder.friendNameView = (TextView) convertView.findViewById(R.id.friendlist_name);
        holder.friendNameView.setText(friend.getName());
        holder.onlineStatusImage = (ImageView) convertView.findViewById(R.id.friendlist_status);
        holder.invite_btn = (ImageButton) convertView.findViewById(R.id.friendlist_invite);
        if(friend.isOnline()){
            holder.onlineStatusImage.setImageResource(R.drawable.online);
        }
        else{
            holder.onlineStatusImage.setImageResource(R.drawable.offline);
        }

        if (partyList.contains(friend)) {
            holder.invite_btn.setOnClickListener(null);
            holder.invite_btn.setImageResource(R.drawable.partyhat);
            holder.invite_btn.setPadding(0, 0, 0,0);
        } else {
            holder.invite_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e("SendPartyRequest", "click");
                    if (me.getFriendList().size()<4){
                        try {
                            communicator.sendMessage(JSON_commands.sendPartyRequest2(me, friend));
                            Log.e("SendPartyRequest", "Send Party Invitation");

                        } catch (JSONException e) {
                            Log.e("SendPartyRequest", "Could not send Party Invitation");
                        }
                    }else{
                        //TODO: Toast that already 4 people in the party
                    }

                }
            });
        }

        return convertView;
    }

    static class ViewHolder {
        TextView friendNameView;
        CircleImageView friendAvatar;
        ImageView onlineStatusImage;
        ImageButton invite_btn;
    }
}
