package com.example.universitymarket.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;
import com.example.universitymarket.R;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.objects.Chat;
import com.example.universitymarket.objects.Message;
import com.example.universitymarket.objects.Post;
import com.example.universitymarket.objects.Transaction;
import com.example.universitymarket.objects.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.squareup.picasso.Picasso;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Transaction transaction;
    private final Context context;
    private final List<Message> messages;
    private final Chat chat;
    // List<Object> contains: User sender, @Nullable Post associatedPost, @Nullable Boolean activeUserHasBought
    private final Map<Message, List<Object>> messageMap;

    public MessageAdapter(Context context, Chat chat, List<Message> messages, List<User> senders, List<Post> posts, List<Boolean> facts) {
        this.context = context;
        this.chat = chat;
        this.messages = messages;

        messageMap = IntStream.range(0, messages.size()).boxed().collect(Collectors.toMap(messages::get, i -> Arrays.asList(senders.get(i), posts.get(i), facts.get(i))));
    }

    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_message_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {
        Message message = messages.get(position);
        List<Object> objList = messageMap.get(message);
        if(objList == null || objList.isEmpty() || objList.get(0) == null)
            return;

        User sender = (User) objList.get(0);
        if(objList.get(1) == null) {
            if(objList.get(2) == null)
                return;
            // The associated message is an offer message

            Post associatedPost = (Post) objList.get(1);
            boolean activeUserHasBought = (boolean) objList.get(2);
            holder.regularContent.setVisibility(View.INVISIBLE);
            Picasso
                    .get()
                    .load(associatedPost.getImageUrls().isEmpty() ? "https://firebasestorage.googleapis.com/v0/b/university-market-e4aa7.appspot.com/o/invalid.png?alt=media&token=4034f579-5c6f-4ac9-a38b-29e3a2b005bb" : associatedPost.getImageUrls().get(0))
                    .resize(Data.convertDpToPixel((Activity) context, 100), Data.convertDpToPixel((Activity) context, 100))
                    .into(holder.offerImage);
            holder.offerTitle.setText(associatedPost.getItemTitle());

            if(activeUserHasBought) {
                holder.offerButton.setEnabled(true);
                holder.offerButton.setOnClickListener(l -> {
                    transaction = new Transaction(
                            associatedPost.getDescriptors(),
                            associatedPost.getId(),
                            associatedPost.getGenre(),
                            false,
                            associatedPost.getItemDescription(),
                            associatedPost.getId() + associatedPost.getQuantity(),
                            associatedPost.getImageContexts(),
                            associatedPost.getItemTitle(),
                            chat.getId(),
                            null,
                            ActiveUser.email,
                            new Date().toString(),
                            sender.getEmail(),
                            associatedPost.getListPrice(),
                            "closed",
                            Data.generateID("tsct")
                    );

                    Network.setTransaction((Activity) context, transaction, false, new Callback<Transaction>() {
                        @Override
                        public void onSuccess(Transaction result) {
                            transaction = result;
                            ActiveUser.transact_ids.add(transaction.getId());

                            Network.setUser((Activity) context, ActiveUser.toPOJO(), false, new Callback<User>() {
                                @Override
                                public void onSuccess(User result) {}

                                @Override
                                public void onFailure(Exception error) {
                                    Log.e("setUser", error.getMessage());
                                    Toast.makeText(
                                            context,
                                            "Unable to add to ActiveUser's transaction record",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            });

                            // We are getting user here to update the sender. Since we can't update individual fields,
                            // we must be sure we are updating their user state from the latest snapshot as possible
                            Network.getUser((Activity) context, sender.getId(), new Callback<User>() {
                                @Override
                                public void onSuccess(User result) {
                                    // And now we update their transaction record as fast as possible
                                    ArrayList<String> sellersTransactIds = result.getTransactIds();
                                    sellersTransactIds.add(transaction.getId());
                                    result.setTransactIds(sellersTransactIds);

                                    Network.setUser((Activity) context, result, false, new Callback<User>() {
                                        @Override
                                        public void onSuccess(User result) {}

                                        @Override
                                        public void onFailure(Exception error) {
                                            Log.e("setUser", error.getMessage());
                                            Toast.makeText(
                                                    context,
                                                    "Unable to add to transaction record for " + sender.getEmail(),
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Exception error) {
                                    Log.e("getUser", error.getMessage());
                                    Toast.makeText(
                                            context,
                                            "Unable to retrieve and update user " + sender.getEmail(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            });

                        }

                        @Override
                        public void onFailure(Exception error) {
                            Log.e("setTransaction", error.getMessage());
                            Toast.makeText(
                                    context,
                                    "Unable to commit the offer, try again later",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                });
            }
            holder.offerImage.setVisibility(View.VISIBLE);
            holder.offerTitle.setVisibility(View.VISIBLE);
            holder.offerButton.setVisibility(View.VISIBLE);
        } else {
            // Otherwise it's a regular one

            if(sender.getEmail().equals(ActiveUser.email)) {
                // It's our message, shift it right and recolor
                ConstraintSet newConstraints = new ConstraintSet();
                newConstraints.clone(context, R.id.message_constraint);
                newConstraints.connect(R.id.message_card_item, ConstraintSet.RIGHT, R.id.message_constraint, ConstraintSet.RIGHT, 0);
                newConstraints.connect(R.id.message_timestamp, ConstraintSet.RIGHT, R.id.message_constraint, ConstraintSet.RIGHT, 0);
                newConstraints.applyTo(holder.constraintLayout);

                TypedValue colorSecondary = new TypedValue();
                context.getTheme().resolveAttribute(R.attr.colorSecondary, colorSecondary, true);
                holder.cardContainer.setCardBackgroundColor(colorSecondary.data);
            }

            holder.regularContent.setText(message.getContents());
        }
        try {
            SimpleDateFormat parser = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy", Locale.US);
            Date parsed = parser.parse(message.getTimestamp());
            if(parsed != null) {
                holder.messageTimestamp.setText(new SimpleDateFormat("MMM dd, yyyy HH:mm a", Locale.US).format(parsed));
            }
        } catch(ParseException e) {
            Log.e("onBindViewHolder", e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final Button offerButton;
        private final ImageView offerImage;
        private final TextView offerTitle;
        private final TextView regularContent;
        private final CardView cardContainer;
        private final ConstraintLayout constraintLayout;
        private final TextView messageTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            offerButton = itemView.findViewById(R.id.message_offer_button);
            offerImage = itemView.findViewById(R.id.message_offer_image);
            offerTitle = itemView.findViewById(R.id.message_offer_title);
            regularContent = itemView.findViewById(R.id.message_text);
            cardContainer = itemView.findViewById(R.id.message_card_item);
            constraintLayout = itemView.findViewById(R.id.message_constraint);
            messageTimestamp = itemView.findViewById(R.id.message_timestamp);
        }
    }
}