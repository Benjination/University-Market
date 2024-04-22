package com.example.universitymarket.fragments;

import android.app.Dialog;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.example.universitymarket.R;
import com.example.universitymarket.adapters.MessageAdapter;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.Chat;
import com.example.universitymarket.models.Message;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Listener;
import com.example.universitymarket.utilities.Network;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MessageFragment extends Fragment implements View.OnClickListener {

    private final String[] args;
    private LayoutInflater inflater;
    private View root;
    private ViewGroup container;
    private RecyclerView recycler;
    private MessageAdapter adapter;
    private EditText inputBox;
    private ImageButton sendButton;
    private FloatingActionButton offerButton;
    private List<Post> offerPosts;
    private List<Message> messages;
    private Chat chat;
    private final FragmentManager fm;

    public MessageFragment(String[] args, FragmentManager fm) {
        // args[0] contains the chatID
        this.args = args;
        this.fm = fm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = container;
        root = inflater.inflate(R.layout.fragment_message, container, false);
        configure(root);
        return root;
    }

    private void configure(View v) {
        recycler = v.findViewById(R.id.message_recycler_view);
        inputBox = v.findViewById(R.id.message_input_box);
        sendButton = v.findViewById(R.id.message_send_button);
        offerButton = v.findViewById(R.id.message_offer_button);

        sendButton.setEnabled(false);
        offerButton.setEnabled(false);

        Network.getChat(args[0], new Callback<Chat>() {
            @Override
            public void onSuccess(Chat result) {
                chat = result;

                Network.getMessages(chat.getMessageIds(), new Callback<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> msgs) {
                        messages = msgs;
                        adapter = new MessageAdapter(requireContext(), chat, messages);
                        recycler.setAdapter(adapter);

                        Network.listenToChat(chat.getId(), new Listener<Chat>() {
                            @Override
                            public void onAdded(Chat ignored) {}

                            @Override
                            public void onModified(Chat modified) {
                                int before = chat.getMessageIds().size(), after = modified.getMessageIds().size();
                                Stream<Integer> indexing = IntStream.range(Math.min(before, after), Math.max(before, after) - 1).boxed();

                                if(before < after) {
                                    List<String> msgIdsChanged = indexing.map(i -> modified.getMessageIds().get(i)).collect(Collectors.toList());
                                    chat = modified;
                                    Network.getMessages(msgIdsChanged, new Callback<List<Message>>() {
                                        @Override
                                        public void onSuccess(List<Message> nmsgs) {
                                            messages = nmsgs;
                                            for(Message m : nmsgs) {
                                                adapter.addMessage(m);
                                            }
                                        }

                                        @Override
                                        public void onFailure(Exception error) {
                                            Log.e("getMessages", error.getMessage());
                                        }
                                    });
                                } else if (before > after) {
                                    List<Message> msgsChanged = indexing.map(i -> messages.get(i)).collect(Collectors.toList());
                                    for(Message m : msgsChanged) {
                                        adapter.removeMessage(m);
                                        messages.remove(m);
                                    }
                                    chat = modified;
                                }
                            }

                            @Override
                            public void onRemoved(Chat ignored) {}

                            @Override
                            public void onFailure(Exception error) {
                                Log.e("listenToChat", error.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e("getMessages", error.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("getChat", error.getMessage());
                Toast.makeText(
                        requireContext(),
                        "Could not retrieve chat, check your network connection",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.message_send_button) {
            String msgID = Data.generateID("msg");
            Message msg = new Message(
                    null,
                    null,
                    inputBox.getText().toString(),
                    new ArrayList<>(),
                    ActiveUser.email,
                    new Date().toString(),
                    msgID
            );

            Network.setMessage(msg, false, new Callback<Message>() {
                @Override
                public void onSuccess(Message message) {
                    inputBox.getText().clear();
                    chat.setMessageIds((ArrayList<String>) Stream.concat(chat.getMessageIds().stream(), Stream.of(message.getId())).collect(Collectors.toList()));

                    Network.setChat(chat, false, new Callback<Chat>() {
                        @Override
                        public void onSuccess(Chat ignored) {
                            adapter.addMessage(message);
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
                            "Unable to send message",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        } else if(v.getId() == R.id.message_offer_button) {
            DialogFragment postChooser = new DialogFragment(R.layout.layout_offer_dialog);
            Dialog postChooserView = postChooser.getDialog();
            if(postChooserView == null)
                return;

            FloatingActionButton closeOffer = postChooserView.findViewById(R.id.offer_close_button);
            RadioGroup postSelection = postChooserView.findViewById(R.id.offer_posts_radiogroup);
            TextView noInWatchlist = postChooserView.findViewById(R.id.offer_post_in_watchlists);
            TextView daysRemaining = postChooserView.findViewById(R.id.offer_post_days_remaining);
            TextView listPrice = postChooserView.findViewById(R.id.offer_post_list_price);
            Button sendOffer = postChooserView.findViewById(R.id.offer_send_button);

            closeOffer.setOnClickListener(l -> postChooser.dismiss());

            Network.getPosts(ActiveUser.post_ids, new Callback<List<Post>>() {
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

                    noInWatchlist.setText("");
                    daysRemaining.setText("");
                    listPrice.setText("");

                    postSelection.setOnCheckedChangeListener((rg, checkedId) -> {
                        Post post = offerPosts.stream().filter(res -> res.getItemTitle().contentEquals((((RadioButton) rg.findViewById(checkedId)).getText()))).findFirst().orElse(null);
                        if(post == null)
                            return;

                        long numDaysRemain = Duration.between((Temporal) Data.parseDate(post.getDateCreated()), LocalDateTime.now()).toDays();
                        String daysRemText = "Expires in: " + numDaysRemain + "days";
                        String listPriceText = "List price: $" + post.getListPrice();

                        daysRemaining.setText(daysRemText);
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
                                        postChooser.dismiss();
                                        adapter.addMessage(message);
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
        }
    }
}