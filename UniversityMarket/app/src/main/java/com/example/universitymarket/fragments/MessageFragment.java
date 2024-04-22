package com.example.universitymarket.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.example.universitymarket.utilities.Network;
import com.example.universitymarket.viewmodels.MessageViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
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
    private MessageViewModel viewModel;
    private final FragmentManager fm;

    private Observer<List<Message>> listenToMessages;

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
        viewModel = new ViewModelProvider(requireActivity()).get(MessageViewModel.class);

        sendButton.setEnabled(false);
        offerButton.setEnabled(false);
        sendButton.setOnClickListener(this);
        offerButton.setOnClickListener(this);

        Network.getChat(args[0], new Callback<Chat>() {
            @Override
            public void onSuccess(Chat result) {
                chat = result;

                listenToMessages = newMessages -> {
                    if(messages == null) {
                        messages = newMessages;
                        adapter = new MessageAdapter(requireContext(), result, messages);
                        recycler.setAdapter(adapter);
                        recycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
                    } else {
                        adapter.addMessages(newMessages);
                        Data.updateAdapter(messages, newMessages, adapter);
                        messages = newMessages;
                    }
                };
                viewModel.listenToChatMessages(chat).observe(getViewLifecycleOwner(), listenToMessages);

                sendButton.setEnabled(true);
                offerButton.setEnabled(true);
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("getChat", error.getMessage());
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

                    Network.setChat(chat, false, null);
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
            MessageDialogFragment postChooser = new MessageDialogFragment(chat);
            postChooser.show(fm, null);
        }
    }
}