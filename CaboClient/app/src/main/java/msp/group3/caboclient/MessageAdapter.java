package msp.group3.caboclient;

import android.app.Activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MessageAdapter extends ArrayAdapter<ChatMessage> {

    private static class ViewHolder {
        TextView msgName;
        TextView msgText;
    }

    public MessageAdapter(ArrayList<ChatMessage> message, Context context) {
        super(context, R.layout.my_chat_message, message);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        ChatMessage chatMessage = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.my_chat_message, parent, false);
            viewHolder.msgName = (TextView) convertView.findViewById(R.id.name);
            viewHolder.msgText = (TextView) convertView.findViewById(R.id.message_body);


            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

        viewHolder.msgName.setText(chatMessage.getName());
        viewHolder.msgText.setText(chatMessage.getMessage());

        // Return the completed view to render on screen
        return convertView;
    }
}