package com.example.universitymarket.fragments;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.universitymarket.R;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.Chat;
import com.example.universitymarket.models.Message;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MessageDialogFragment extends DialogFragment {

    private List<Post> offerPosts;
    private Chat chat;

    public MessageDialogFragment(Chat chat) {
        super();
        this.chat = chat;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.layout_offer_dialog, container, false);

        FloatingActionButton closeOffer = root.findViewById(R.id.offer_close_button);
        RadioGroup postSelection = root.findViewById(R.id.offer_posts_radiogroup);
        TextView listPrice = root.findViewById(R.id.offer_post_list_price);
        Button sendOffer = root.findViewById(R.id.offer_send_button);

        closeOffer.setOnClickListener(l -> this.dismiss());

        Network.getPosts(ActiveUser.post_ids, null, new Callback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                offerPosts = result;

                if(!result.isEmpty())
                    sendOffer.setEnabled(true);
                for(Post post : result) {
                    RadioButton rb = new RadioButton(requireContext());
                    rb.setText(post.getItemTitle());
                    postSelection.addView(rb);
                }

                listPrice.setText("");

                postSelection.setOnCheckedChangeListener((rg, checkedId) -> {
                    Post post = offerPosts.stream().filter(res -> res.getItemTitle().contentEquals((((RadioButton) rg.findViewById(checkedId)).getText()))).findFirst().orElse(null);
                    if(post == null)
                        return;

                    long numDaysRemain = Duration.between(LocalDateTime.from(Data.parseDate(post.getDateCreated())), LocalDateTime.now()).toDays();
                    String daysRemText = "Expires in: " + numDaysRemain + "days";
                    String listPriceText = "List price: $" + post.getListPrice();

                    listPrice.setText(listPriceText);
                });

                sendOffer.setOnClickListener(l -> {
                    Post post = offerPosts.stream().filter(res -> res.getItemTitle().contentEquals((((RadioButton) postSelection.findViewById(postSelection.getCheckedRadioButtonId())).getText()))).findFirst().orElse(null);
                    if(post == null)
                        return;

                    String msgID = Data.generateID("msg");
                    Message msg = new Message(
                            false,
                            post.getId(),
                            null,
                            new ArrayList<>(),
                            ActiveUser.email,
                            new Date().toString(),
                            msgID
                    );

                    Network.setMessage(msg, false, new Callback<Message>() {
                        @Override
                        public void onSuccess(Message message) {
                            chat.setMessageIds((ArrayList<String>) Stream.concat(chat.getMessageIds().stream(), Stream.of(message.getId())).collect(Collectors.toList()));

                            Network.setChat(chat, false, new Callback<Chat>() {
                                @Override
                                public void onSuccess(Chat ignored) {
                                    MessageDialogFragment.this.dismiss();
                                }

                                @Override
                                public void onFailure(Exception error) {
                                    Log.e("setChat", error.getMessage());
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception error) {
                            Log.e("setMessage", error.getMessage());
                            Toast.makeText(
                                    requireContext(),
                                    "Unable to send offer",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                });
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("getPosts", error.getMessage());
                Toast.makeText(
                        requireContext(),
                        "Could not retrieve your posts",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        return root;
    }
}
