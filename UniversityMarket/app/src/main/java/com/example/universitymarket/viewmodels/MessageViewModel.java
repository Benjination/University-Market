package com.example.universitymarket.viewmodels;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.universitymarket.models.Chat;
import com.example.universitymarket.models.Message;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Listener;
import com.example.universitymarket.utilities.Network;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageViewModel extends ViewModel {

    private final MutableLiveData<List<Message>> messageList = new MutableLiveData<>();

    public MutableLiveData<List<Message>> listenToChatMessages(Chat chat) {
        load(chat.getMessageIds());
        Network.listenToChat(chat.getId(), new Listener<Chat>() {
            @Override
            public void onAdded(Chat added) {}

            @Override
            public void onModified(Chat modified) {
                ArrayList<String> differentMsgIds = (ArrayList<String>) Data.differingValuePairs(chat, modified).stream()
                        .filter(pair -> pair.getKey()
                                .equals("message_ids"))
                        .findFirst()
                        .map(Map.Entry::getValue)
                        .orElse(null);

                if(differentMsgIds != null)
                    load(differentMsgIds);
            }

            @Override
            public void onRemoved(Chat removed) {}

            @Override
            public void onFailure(Exception error) {}
        });
        return messageList;
    }

    private void load(List<String> messageIds) {
        Network.getMessages(messageIds, new Callback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> result) {
                messageList.setValue(result);
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("getMessages", error.getMessage());
            }
        });
    }
}
