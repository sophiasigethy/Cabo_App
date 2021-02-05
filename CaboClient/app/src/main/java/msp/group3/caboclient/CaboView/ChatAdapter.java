package msp.group3.caboclient.CaboView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import msp.group3.caboclient.R;

class ChatAdapter extends BaseAdapter {

    private ArrayList<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
    private Context context;
    private LayoutInflater layoutinflater;

    public ChatAdapter(Context context, ArrayList<ChatMessage> chatMessageList) {
        this.context = context;
        this.chatMessageList = chatMessageList;
        this.layoutinflater = LayoutInflater.from(context);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public ChatMessage getItem(int index) {
        return this.chatMessageList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        ChatMessage msg = this.chatMessageList.get(position);
        holder = new ViewHolder();


        if (convertView == null) {
            if(msg.getIfMyMessage()){
                convertView = layoutinflater.inflate(R.layout.my_chat_message, null);

            }
            else{
                convertView = layoutinflater.inflate(R.layout.their_chat_message, null);
                holder.avatar = (CircleImageView) convertView.findViewById(R.id.avatar);
                holder.avatar.setImageResource(msg.getAvatar());
            }

            holder.senderNameView = (TextView) convertView.findViewById(R.id.name);
            holder.chatMessageView = (TextView) convertView.findViewById(R.id.message_body);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.senderNameView.setText(msg.getName());
        holder.chatMessageView.setText(msg.getMessage());

        return convertView;
    }

    static class ViewHolder {
        TextView senderNameView;
        TextView chatMessageView;
        CircleImageView avatar;

    }
}
