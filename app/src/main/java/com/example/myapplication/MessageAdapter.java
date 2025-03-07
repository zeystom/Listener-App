package com.example.myapplication;

import android.content.Context;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(String currentUserId, List<Message> messageList) {
        this.currentUserId = currentUserId;
        this.messageList = messageList;
    }

    public void setMessages(List<Message> messages) {
        this.messageList = messages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Здесь можно различать типы сообщений, если потребуется разное оформление
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.bind(message, currentUserId);
    }

    @Override
    public int getItemCount() {
        return messageList != null ? messageList.size() : 0;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
        }

        public void bind(Message message, String currentUserId) {
            messageText.setText(message.getText());
            Context context = messageText.getContext();

            boolean isDarkMode = (context.getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

            LinearLayout parentLayout = (LinearLayout) messageText.getParent();
            if (message.getSenderId().equals(currentUserId)) {
                parentLayout.setGravity(Gravity.END);
                int bgColor = isDarkMode
                        ? ContextCompat.getColor(context, R.color.outgoing_dark_green)
                        : ContextCompat.getColor(context, R.color.outgoing_light_green);
                messageText.setBackgroundColor(bgColor);
                messageText.setTextColor(ContextCompat.getColor(context, R.color.textColorOutgoing));
            } else {
                parentLayout.setGravity(Gravity.START);
                int bgColor = isDarkMode
                        ? ContextCompat.getColor(context, R.color.incoming_dark_green)
                        : ContextCompat.getColor(context, R.color.incoming_light_green);
                messageText.setBackgroundColor(bgColor);
                messageText.setTextColor(ContextCompat.getColor(context, R.color.textColorIncoming));
            }
        }
    }
}
