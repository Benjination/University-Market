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
import com.example.universitymarket.adapters.RecordsAdapter;
import com.example.universitymarket.globals.Policy;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.models.Transaction;
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Network;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class RecordsFragment extends Fragment {

    private View root;
    private LayoutInflater inflater;
    private RecyclerView recyclerView;
    private TaskCompletionSource<String> load;
    private RecordsAdapter adapter;

    private List<Transaction> transactions;
    private List<Post> posts;
    private List<User> users;
    private Thread retrieve;
    private TextView unavailable;
    private final Bundle dashMessage = new Bundle();
    private final String[] args;
    private final FragmentManager fm;

    public RecordsFragment(String[] args, FragmentManager fm) {
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
        root = inflater.inflate(R.layout.fragment_records, container, false);
        configure(root);
        return root;
    }

    private void configure(View v) {
        unavailable = v.findViewById(R.id.records_unavailable_text);
        recyclerView = v.findViewById(R.id.records_recycle_view);

        load = new TaskCompletionSource<>();
        loadPage(load.getTask());
        Network.getTransactions(ActiveUser.transact_ids, null, new Callback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> result) {
                transactions = result;
                List<String> postIds = new ArrayList<>(), userIds = new ArrayList<>();
                transactions.forEach(transaction -> {
                    postIds.add(transaction.getPostId());
                    userIds.add(transaction.getSellerEmail());
                });

                Network.getPosts(postIds, null, new Callback<List<Post>>() {
                    @Override
                    public void onSuccess(List<Post> result) {
                        posts = result;
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e("getPosts", error.getMessage());
                    }
                });

                Network.getUsers(userIds, null, new Callback<List<User>>() {
                    @Override
                    public void onSuccess(List<User> result) {
                        users = result;
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e("getUsers", error.getMessage());
                    }
                });

                retrieve = new Thread(() -> {
                    while(posts.size() != transactions.size() && users.size() != transactions.size());
                    adapter = new RecordsAdapter(requireContext(), transactions, posts, users);
                    load.setResult("getPosts and getUsers");
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
                });
                retrieve.start();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if(posts.size() != transactions.size() && users.size() != transactions.size()) {
                        load.setResult("getPosts and getUsers");
                        retrieve.interrupt();
                        Log.e("retrieve", String.format(
                                        "tsct: %d, usrs: %d, psts: %d",
                                        transactions.size(), users.size(), posts.size()),
                                new TimeoutException("Post and User retrieval timeout"));
                    }
                }, Policy.max_seconds_before_timeout * 1000);
            }

            @Override
            public void onFailure(Exception error) {
                unavailable.setVisibility(View.VISIBLE);
                load.setResult("getTransactions");
            }
        });
    }

    private void loadPage(Task<String> task) {
        dashMessage.putBoolean("isLoading", true);
        fm.setFragmentResult("setLoading", dashMessage);

        task.addOnCompleteListener(res -> {
            dashMessage.putBoolean("isLoading", false);
            fm.setFragmentResult("setLoading", dashMessage);
        });
    }
}