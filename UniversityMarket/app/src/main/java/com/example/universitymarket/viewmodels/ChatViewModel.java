package com.example.universitymarket.viewmodels;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.universitymarket.globals.Policy;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.Chat;
import com.example.universitymarket.models.Message;
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Listener;
import com.example.universitymarket.utilities.Network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ChatViewModel extends ViewModel {

    private final MutableLiveData<List<MessagePreview>> previewList = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(Boolean.FALSE);
    private final MutableLiveData<Integer> numUnread = new MutableLiveData<>();
    private List<Chat> chats;
    private List<List<User>> participants;
    private List<List<String>> participantsOrdered;
    private List<Message> previews;
    private List<String> previewIds;
    private Thread retrieve;

    public static class MessagePreview {
        public Chat chat;
        public List<User> participants;
        public Message preview;
        public Boolean add = true;

        public MessagePreview(Chat chat, List<User> participants, Message preview) {
            this.chat = chat;
            this.participants = participants;
            this.preview = preview;
        }
    }

    public MutableLiveData<List<MessagePreview>> listenToActiveUsersChats() {
        refresh();
        Network.listenToUser(ActiveUser.email, new Listener<User>() {
            @Override
            public void onAdded(User ignored) {}

            @Override
            public void onModified(User modified) {
                refresh();
            }

            @Override
            public void onRemoved(User ignored) {}

            @Override
            public void onFailure(Exception error) {
                Log.e("listenToUser", error.getMessage());
            }
        });
        return previewList;
    }

    public MutableLiveData<Boolean> getLoading() {
        return isLoading;
    }

    public MutableLiveData<Integer> getUnread() {
        return numUnread;
    }

    private void refresh() {
        participants = null;
        participantsOrdered = null;
        previews = null;
        previewIds = null;

        if(retrieve != null)
            retrieve.interrupt();
        isLoading.setValue(true);

        Network.getChats(ActiveUser.chat_ids, null, new Callback<List<Chat>>() {
            @Override
            public void onSuccess(List<Chat> result) {
                chats = result;
                participantsOrdered = chats.stream().map(Chat::getParticipantEmails).collect(Collectors.toList());
                previewIds = result.stream().map(Chat::getMessageIds).map(msgs -> msgs.isEmpty() ? null : msgs.get(msgs.size() - 1)).collect(Collectors.toList());
                List<String> allUsers = participantsOrdered.stream().flatMap(List::stream).distinct().collect(Collectors.toList());

                Network.getUsers(allUsers, null, new Callback<List<User>>() {
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

                Network.getMessages(previewIds.stream().filter(Objects::nonNull).collect(Collectors.toList()), null, new Callback<List<Message>>() {
                    @Override
                    public void onSuccess(List<Message> result) {
                        previews = IntStream.range(0, previewIds.size()).boxed()
                                .map(i -> previewIds.get(i) == null ? null : result.remove(0))
                                .collect(Collectors.toList());
                    }

                    @Override
                    public void onFailure(Exception error) {
                        previews = Collections.nCopies(previewIds.size(), null);
                        Log.e("getMessages", error.getMessage());
                    }
                });

                retrieve = new Thread(() -> {
                    while((participants == null || chats == null || previews == null) || participants.size() != chats.size() && previews.size() != chats.size());

                    List<MessagePreview> previewsInMemory = previewList.getValue() == null ? new ArrayList<>() : previewList.getValue();
                    IntStream.range(0, chats.size()).forEach(i -> previewsInMemory.add(new MessagePreview(chats.get(i), participants.get(i), previews.get(i))));

                    new Handler(Looper.getMainLooper()).post(() -> {
                        previewList.setValue(previewsInMemory);
                        isLoading.setValue(false);
                        numUnread.setValue((int) previewsInMemory.stream().map(prev -> prev.preview).filter(prev -> prev != null && !prev.getReadEmails().contains(ActiveUser.email)).count());
                    });
                });
                retrieve.start();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if((participants == null || chats == null || previews == null) || participants.size() != chats.size() && previews.size() != chats.size()) {
                        isLoading.setValue(false);
                        numUnread.setValue(null);
                        retrieve.interrupt();
                        Log.e("retrieve", "timeout",
                                new TimeoutException("Message and User retrieval timeout"));
                    }
                }, Policy.max_seconds_before_timeout * 1000);
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("getChats", error.getMessage());
                isLoading.setValue(false);
                numUnread.setValue(null);
            }
        });
    }
}
