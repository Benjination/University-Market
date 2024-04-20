package com.example.universitymarket.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.universitymarket.R;
import com.example.universitymarket.adapters.ChatAdapter;
import com.example.universitymarket.globals.Policy;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.objects.Chat;
import com.example.universitymarket.objects.Message;
import com.example.universitymarket.objects.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Network;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class ChatFragment extends Fragment implements ChatAdapter.onClickListener {

    private LayoutInflater inflater;
    private TextView unavailable;
    private RecyclerView recycler;
    private ViewGroup container;
    private View root;
    private FragmentManager fm;
    private Thread retrieve;
    private ChatAdapter adapter;
    private TaskCompletionSource<String> load;
    private final Bundle dashMessage = new Bundle();

    private List<Chat> chats;
    private List<List<User>> participants;
    private List<List<String>> participantsOrdered;
    private List<Message> previews;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = container;
        fm = getParentFragmentManager();
        root = inflater.inflate(R.layout.fragment_chat, container, false);
        configure(root);
        return root;
    }

    private void configure(View v) {
        unavailable = v.findViewById(R.id.chat_unavailable_text);
        recycler = v.findViewById(R.id.chat_recycle_view);

        load = new TaskCompletionSource<>();
        loadPage(load.getTask());
        Network.getChats(requireActivity(), ActiveUser.chat_ids, new Callback<List<Chat>>() {
            @Override
            public void onSuccess(List<Chat> result) {
                chats = result;
                participantsOrdered = new ArrayList<>();
                List<String> msgIds = new ArrayList<>();

                chats.forEach(chat -> {
                    participantsOrdered.add(chat.getParticipantEmails());
                    if(!chat.getMessageIds().isEmpty())
                        msgIds.add(chat.getMessageIds().get(chat.getMessageIds().size() - 1));
                });
                List<String> allUsers = participantsOrdered.stream().flatMap(List::stream).distinct().collect(Collectors.toList());

                Network.getUsers(requireActivity(), allUsers, new Callback<List<User>>() {
                    @Override
                    public void onSuccess(List<User> result) {
                        participants = participantsOrdered.stream()
                                .map(list -> (list.stream()
                                        .map(s -> result.stream()
                                                .filter(user -> user.getId().equals(s))
                                                .findFirst()
                                                .orElse(null))
                                        .collect(Collectors.toList())))
                                .collect(Collectors.toList());
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e("getUsers", error.getMessage());
                    }
                });

                Network.getMessages(requireActivity(), msgIds, new Callback<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> result) {
                        previews = result;
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e("getMessages", error.getMessage());
                    }
                });

                retrieve = new Thread(() -> {
                    while((participants == null || chats == null || previews == null) || participants.size() != chats.size() && previews.size() != chats.size());
                    adapter = new ChatAdapter(requireContext(), ChatFragment.this, chats, participants, previews);
                    load.setResult("getMessages and getUsers");
                    recycler.setAdapter(adapter);
                    recycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
                    callback();
                });
                retrieve.start();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if((participants == null || chats == null || previews == null) || participants.size() != chats.size() && previews.size() != chats.size()) {
                        load.setResult("getPosts and getUsers");
                        retrieve.interrupt();
                        Log.e("retrieve", "timeout",
                                new TimeoutException("Message and User retrieval timeout"));
                    }
                }, Policy.max_seconds_before_timeout * 1000);
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("getChats", error.getMessage());
                unavailable.setVisibility(View.VISIBLE);
                load.setResult("getTransactions");
                dashMessage.putString("newSubtitle", "Unavailable");
                dashMessage.putString("callingFragment", this.getClass().getName());
                fm.setFragmentResult("updateSubtitle", dashMessage);
            }
        });
    }

    private void callback() {
        int numberUnreadConversations = (int) previews.stream().filter(message -> message.getReadEmails().contains(ActiveUser.email)).count();
        if(numberUnreadConversations == 0)
            dashMessage.putString("newSubtitle", "You're all caught up!");
        else
            dashMessage.putString("newSubtitle", "You have " + numberUnreadConversations + " unread conversations");
        dashMessage.putString("callingFragment", this.getClass().getName());
        fm.setFragmentResult("updateSubtitle", dashMessage);
    }

    private void loadPage(Task<String> task) {
        dashMessage.putBoolean("isLoading", true);
        fm.setFragmentResult("setLoading", dashMessage);

        task.addOnCompleteListener(res -> {
            dashMessage.putBoolean("isLoading", false);
            fm.setFragmentResult("setLoading", dashMessage);
        });
    }

    @Override
    public void onClick(Chat chat) {
        dashMessage.putString("popupTitle", "Johnny Hamcheck");
        dashMessage.putString("popupFragment", MessageFragment.class.getName());
        dashMessage.putStringArray("popupFragArgs", new String[]{ chat.getId() });
        fm.setFragmentResult("createPopup", dashMessage);
    }
}