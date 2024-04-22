package com.example.universitymarket.adapters;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.universitymarket.R;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.Chat;
import com.example.universitymarket.models.Message;
import com.example.universitymarket.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final Context context;
    private final List<Chat> chats;
    private final onClickListener clickListener;
    private final HashMap<Chat, Pair<List<User>, Message>> previewMap = new HashMap<>();

    public ChatAdapter(Context context, onClickListener clickListener, List<Chat> chats, List<List<User>> participants, List<Message> previews) {
        this.context = context;
        this.clickListener = clickListener;
        this.chats = chats;

        IntStream.range(0, chats.size()).forEach(i -> previewMap.put(chats.get(i), new Pair<>(participants.get(i), previews.get(i))));
    }

    public interface onClickListener {
        void onClick(Chat chat);
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        Chat chat = chats.get(position);
        Pair<List<User>, Message> pair = previewMap.get(chat);
        if(pair == null)
            return;

        holder.itemView.setOnClickListener(l -> {
            clickListener.onClick(chat);
        });

        List<User> otherParticipants = pair.first.stream().filter(e -> !ActiveUser.email.equals(e.getId())).collect(Collectors.toList());
        Message preview = pair.second;

        String name = otherParticipants.size() > 1 ? otherParticipants.stream().map(User::getFirstName).collect(Collectors.joining(",")) : otherParticipants.get(0).getFirstName() + " " + otherParticipants.get(0).getLastName();
        String message = preview.getContents() == null ? "Item offer" : preview.getContents();
        String delivered = preview.getTimestamp() == null ? "Unknown" : preview.getTimestamp();
        holder.senders.setText(name);
        holder.deliverdate.setText(delivered);
        holder.preview.setText(message);
        if(!pair.second.getReadEmails().contains(ActiveUser.email))
            holder.unreadIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView senders;
        private final TextView deliverdate;
        private final TextView preview;
        private final ImageView unreadIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            senders = itemView.findViewById(R.id.chat_sender_text);
            deliverdate = itemView.findViewById(R.id.chat_deliverdate_text);
            preview = itemView.findViewById(R.id.chat_preview_text);
            unreadIndicator = itemView.findViewById(R.id.chat_indicator_dot);

        }
    }
}