package com.example.universitymarket.adapters;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
import com.example.universitymarket.globals.Policy;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.Chat;
import com.example.universitymarket.models.Message;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.models.Transaction;
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.squareup.picasso.Picasso;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Transaction transaction;
    private List<Message> messages;
    private final Context context;
    private final Chat chat;

    public MessageAdapter(Context context, Chat chat, List<Message> messages) {
        this.context = context;
        this.chat = chat;
        this.messages = messages;
    }

    public void removeMessage(List<Message> message) {
        messages.removeAll(message);
    }

    public void addMessages(List<Message> message) {
        messages.addAll(message);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_message_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messages.get(position);
        String senderEmail = message.getSenderEmail();

        ViewGroup.LayoutParams params = holder.offerButton.getLayoutParams();
        params.height = 0;
        params.width = 0;

        if(senderEmail.equals(ActiveUser.email)) {
            // It's our message, shift it right and recolor
                /*ConstraintSet newConstraints = new ConstraintSet();
                newConstraints.clone(context, R.id.message_constraint);
                newConstraints.connect(R.id.message_card_item, ConstraintSet.RIGHT, R.id.message_constraint, ConstraintSet.RIGHT, 0);
                newConstraints.connect(R.id.message_timestamp, ConstraintSet.RIGHT, R.id.message_constraint, ConstraintSet.RIGHT, 0);
                newConstraints.applyTo(holder.constraintLayout);*/

            TypedValue colorSecondary = new TypedValue();
            context.getTheme().resolveAttribute(R.attr.colorSecondary, colorSecondary, true);
            holder.cardContainer.setCardBackgroundColor(colorSecondary.data);
        }

        if(message.getOfferPostId() != null) {
            if(message.getOfferTaken())
                return;

            Network.getPost(message.getOfferPostId(), new Callback<Post>() {
                @Override
                public void onSuccess(Post post) {
                    holder.offerImage.setVisibility(View.VISIBLE);
                    holder.offerTitle.setVisibility(View.VISIBLE);
                    holder.offerButton.setVisibility(View.VISIBLE);

                    holder.regularContent.setLayoutParams(params);
                    holder.regularContent.setVisibility(View.INVISIBLE);
                    new Handler(Looper.getMainLooper()).post(() -> Picasso
                            .get()
                            .load(post.getImageUrls().isEmpty() ? Policy.invalid_image.get(0) : post.getImageUrls().get(0))
                            .resize(Data.convertDpToPixel((Activity) context, 100), Data.convertDpToPixel((Activity) context, 100))
                            .into(holder.offerImage, new com.squareup.picasso.Callback() {
                                @Override
                                public void onSuccess() {
                                }

                                @Override
                                public void onError(Exception ignored) {
                                    Picasso.get().load(Policy.invalid_image.get(0)).resize(Data.convertDpToPixel((Activity) context, 100), Data.convertDpToPixel((Activity) context, 100)).into(holder.offerImage);
                                }
                            }));
                    holder.offerTitle.setText(post.getItemTitle());
                    holder.offerButton.setOnClickListener(l -> {
                        transaction = new Transaction(
                                post.getDescriptors(),
                                post.getId(),
                                post.getGenre(),
                                false,
                                post.getItemDescription(),
                                post.getId() + post.getQuantity(),
                                post.getImageContexts(),
                                post.getItemTitle(),
                                chat.getId(),
                                null,
                                ActiveUser.email,
                                new Date().toString(),
                                senderEmail,
                                post.getListPrice(),
                                "closed",
                                Data.generateID("tsct")
                        );

                        Network.setTransaction(transaction, false, new Callback<Transaction>() {
                            @Override
                            public void onSuccess(Transaction ignored) {
                                ActiveUser.transact_ids.add(transaction.getId());
                                message.setOfferTaken(true);
                                holder.offerButton.setEnabled(false);

                                Network.setMessage(message, false, new Callback<Message>() {
                                    @Override
                                    public void onSuccess(Message ignored) {}

                                    @Override
                                    public void onFailure(Exception error) {
                                        Log.e("setMessage", error.getMessage());
                                    }
                                });

                                Network.setUser(ActiveUser.toPOJO(), false, new Callback<User>() {
                                    @Override
                                    public void onSuccess(User ignored) {}

                                    @Override
                                    public void onFailure(Exception error) {
                                        Log.e("setUser", error.getMessage());
                                    }
                                });

                                Network.getUser(senderEmail, new Callback<User>() {
                                    @Override
                                    public void onSuccess(User user) {
                                        user.setTransactIds((ArrayList<String>) Stream.concat(user.getTransactIds().stream(), Stream.of(transaction.getId())).collect(Collectors.toList()));

                                        Network.setUser(user, false, new Callback<User>() {
                                            @Override
                                            public void onSuccess(User ignored) {}

                                            @Override
                                            public void onFailure(Exception error) {
                                                Log.e("setUser", error.getMessage());
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(Exception error) {
                                        Log.e("getUser", error.getMessage());
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception error) {
                                Log.e("setTransaction", error.getMessage());
                                Toast.makeText(
                                        context,
                                        "Unable to commit the offer, try again later",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        });
                    });
                }

                @Override
                public void onFailure(Exception error) {
                    Log.e("getPost", error.getMessage());
                    holder.offerButton.setLayoutParams(params);
                    holder.offerImage.setLayoutParams(params);
                    holder.offerTitle.setLayoutParams(params);

                    holder.regularContent.setVisibility(View.VISIBLE);
                    holder.regularContent.setText(R.string.message_post_unavailable);
                }
            });
        } else {
            holder.offerButton.setLayoutParams(params);
            holder.offerImage.setLayoutParams(params);
            holder.offerTitle.setLayoutParams(params);

            holder.regularContent.setText(message.getContents());
        }

        TemporalAccessor parsed = Data.parseDate(message.getTimestamp());
        if(parsed != null)
            holder.messageTimestamp.setText(Data.formatDate(parsed, "MMM dd, yyyy HH:mm a"));
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