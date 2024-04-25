package com.example.universitymarket.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
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
import com.example.universitymarket.models.Chat;
import com.example.universitymarket.models.Message;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.example.universitymarket.viewmodels.ChatViewModel;
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
    private ChatAdapter adapter;
    private FragmentManager fm;
    private TaskCompletionSource<String> load;
    private ChatViewModel viewModel;
    private List<ChatViewModel.MessagePreview> previews;
    private final Bundle dashMessage = new Bundle();

    private Observer<Boolean> isLoading;
    private Observer<Integer> numUnread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isLoading = currentlyLoading -> {
            if(currentlyLoading) {
                load = new TaskCompletionSource<>();
                loadPage(load.getTask());
            } else {
                if(load != null && !load.getTask().isComplete())
                    load.setResult("finish");
            }
        };

         numUnread = newUnread -> {
            if(newUnread == null) {
                unavailable.setVisibility(View.VISIBLE);
                dashMessage.putString("newSubtitle", "Unavailable");
            } else {
                unavailable.setVisibility(View.INVISIBLE);
                dashMessage.putString("newSubtitle", newUnread == 0 ? "You're all caught up!" : "You have " + newUnread + " unread message" + (newUnread > 1 ? "s" : ""));
            }
            dashMessage.putString("callingFragment", this.getClass().getName());
            fm.setFragmentResult("updateSubtitle", dashMessage);
        };
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

        viewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);
        final Observer<List<ChatViewModel.MessagePreview>> listenToChats = newPreviews -> {
            if(previews == null) {
                previews = newPreviews;
                adapter = new ChatAdapter(requireContext(), ChatFragment.this, previews);
                recycler.setAdapter(adapter);
                recycler.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
            } else {
                try {
                    for (ChatViewModel.MessagePreview messagePreview : newPreviews) {
                        if (messagePreview.add)
                            adapter.add(newPreviews);
                        else
                            adapter.remove(newPreviews);
                    }
                } catch(Exception e) {
                    return;
                }
                Data.updateAdapter(previews.stream().map(prev -> prev.chat).collect(Collectors.toList()),
                        newPreviews.stream().map(prev -> prev.chat).collect(Collectors.toList()), adapter);
                previews = newPreviews;
            }
        };
        viewModel.listenToActiveUsersChats().observe(getViewLifecycleOwner(), listenToChats);
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading);
        viewModel.getUnread().observe(getViewLifecycleOwner(), numUnread);
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
    public void onClick(ChatViewModel.MessagePreview preview) {
        List<User> otherParticipants = preview.participants.stream().filter(user -> !ActiveUser.email.equals(user.getEmail())).collect(Collectors.toList());
        String name = otherParticipants.size() > 1 ? otherParticipants.stream().map(User::getFirstName).collect(Collectors.joining(",")) : otherParticipants.get(0).getFirstName() + " " + otherParticipants.get(0).getLastName();

        dashMessage.putString("popupTitle", name);
        dashMessage.putString("popupFragment", MessageFragment.class.getName());
        dashMessage.putStringArray("popupFragArgs", new String[]{ preview.chat.getId() });
        fm.setFragmentResult("createPopup", dashMessage);
    }
}