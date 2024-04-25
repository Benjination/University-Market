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
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.viewmodels.ChatViewModel;
import java.util.List;
import java.util.stream.Collectors;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final Context context;
    private final onClickListener clickListener;
    private final List<ChatViewModel.MessagePreview> previewList;

    public ChatAdapter(Context context, onClickListener clickListener, List<ChatViewModel.MessagePreview> previewList) {
        this.context = context;
        this.previewList = previewList;
        this.clickListener = clickListener;
    }

    public void add(List<ChatViewModel.MessagePreview> previewList) {
        previewList.addAll(previewList);
        notifyDataSetChanged();
    }

    public void remove(List<ChatViewModel.MessagePreview> previewList) {
        previewList.removeAll(previewList);
        notifyDataSetChanged();
    }

    public interface onClickListener {
        void onClick(ChatViewModel.MessagePreview preview);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_chat_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = previewList.get(position).chat;
        List<User> otherParticipants = previewList.get(position).participants.stream().filter(user -> !ActiveUser.email.equals(user.getEmail())).collect(Collectors.toList());
        Message preview = previewList.get(position).preview;

        holder.itemView.setOnClickListener(l -> clickListener.onClick(previewList.get(position)));

        String name = otherParticipants.size() > 1 ? otherParticipants.stream().map(User::getFirstName).collect(Collectors.joining(",")) : otherParticipants.get(0).getFirstName() + " " + otherParticipants.get(0).getLastName();
        String message = preview == null ? "New conversation" : preview.getContents() == null ? "Item offer" : preview.getContents();
        String delivered = preview == null ? Data.formatDate(Data.parseDate(chat.getDateCreated()), "MMM dd, yyyy") : preview.getTimestamp() == null ? "Unknown" : Data.formatDate(Data.parseDate(preview.getTimestamp()), "MMM dd, yyyy");
        holder.senders.setText(name);
        holder.deliverdate.setText(delivered);
        holder.preview.setText(message);
        if(preview != null && !preview.getReadEmails().contains(ActiveUser.email))
            holder.unreadIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return previewList.size();
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