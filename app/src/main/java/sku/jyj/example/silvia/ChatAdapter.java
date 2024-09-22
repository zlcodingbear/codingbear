package sku.jyj.example.silvia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_BOT = 2;

    private List<ChatMessage> chatMessages = new ArrayList<>();

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        return message.isUser() ? VIEW_TYPE_USER : VIEW_TYPE_BOT;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message_bot, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).bind(chatMessage);
        } else {
            ((BotViewHolder) holder).bind(chatMessage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public void addMessage(ChatMessage message) {
        chatMessages.add(message);
        notifyItemInserted(chatMessages.size() - 1);
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }

        public void bind(ChatMessage message) {
            messageTextView.setText(message.getMessage());
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        private TextView messageTextView;

        public BotViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
        }

        public void bind(ChatMessage message) {
            messageTextView.setText(message.getMessage());
        }
    }
}
