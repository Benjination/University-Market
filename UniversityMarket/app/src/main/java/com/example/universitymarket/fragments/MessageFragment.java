package com.example.universitymarket.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.universitymarket.R;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.objects.Chat;
import com.example.universitymarket.objects.Message;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.Date;

public class MessageFragment extends Fragment implements View.OnClickListener {

    private final String[] args;
    private LayoutInflater inflater;
    private View root;
    private ViewGroup container;
    private RecyclerView recycler;
    private EditText inputBox;
    private ImageButton sendButton;
    private FloatingActionButton offerButton;
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

        Network.getChat(requireActivity(), args[0], new Callback<Chat>() {
            @Override
            public void onSuccess(Chat result) {
                chat = result;
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("getChat", error.getMessage());
                Toast.makeText(
                        requireContext(),
                        "Could not retrieve chat",
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
                    null,
                    inputBox.getText().toString(),
                    new ArrayList<>(),
                    ActiveUser.email,
                    new Date().toString(),
                    msgID
            );

            Network.setMessage(requireActivity(), msg, false, new Callback<Message>() {
                @Override
                public void onSuccess(Message result) {
                    inputBox.getText().clear();

                    //TODO: more work for the send button, and DialogBox for send offer
                }

                @Override
                public void onFailure(Exception error) {
                    Log.e("setMessage", error.getMessage());
                    Toast.makeText(
                            requireContext(),
                            "Message not sent",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        } else if(v.getId() == R.id.message_offer_button) {

        }
    }
}