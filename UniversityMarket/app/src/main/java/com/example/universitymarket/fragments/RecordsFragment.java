package com.example.universitymarket.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.universitymarket.R;
import com.example.universitymarket.adapters.RecordsAdapter;
import com.example.universitymarket.globals.Policy;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.objects.Post;
import com.example.universitymarket.objects.Transaction;
import com.example.universitymarket.objects.User;
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
    private LinearLayout titlebar;
    private ProgressBar loadbar;
    private View loadscreen;
    private RecyclerView recyclerView;
    private TaskCompletionSource<String> load;
    private RecordsAdapter adapter;

    private List<Transaction> transactions;
    private List<Post> posts;
    private List<User> users;
    private Thread retrieve;

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
        titlebar = v.findViewById(R.id.records_module_label);
        loadbar = v.findViewById(R.id.records_load_animation);
        loadscreen = v.findViewById(R.id.records_load_screen);
        recyclerView = v.findViewById(R.id.records_recycle_view);
        load = new TaskCompletionSource<>();
        loadPage(load.getTask());

        Network.getTransactions(requireActivity(), ActiveUser.transact_ids, new Callback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> result) {
                transactions = result;
                List<String> postIds = new ArrayList<>(), userIds = new ArrayList<>();
                transactions.forEach(transaction -> {
                    postIds.add(transaction.getPostId());
                    userIds.add(transaction.getSellerEmail());
                });

                Network.getPosts(requireActivity(), postIds, new Callback<List<Post>>() {
                    @Override
                    public void onSuccess(List<Post> result) {
                        posts = result;
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e("getPosts", error.getMessage());
                    }
                });

                Network.getUsers(requireActivity(), userIds, new Callback<List<User>>() {
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
                        Log.e("retrieve", String.format(
                                        "tsct: %d, usrs: %d, psts: %d",
                                        transactions.size(), users.size(), posts.size()),
                                new TimeoutException("Post and User retrieval timeout"));
                        load.setResult("getPosts and getUsers");
                        retrieve.interrupt();
                    }
                }, Policy.max_seconds_before_timeout * 1000);
            }

            @Override
            public void onFailure(Exception error) {
                Toast.makeText(
                        getContext(),
                        error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
                load.setResult("getTransactions");
            }
        });
    }

    private void loadPage(Task<String> task) {
        loadscreen.setEnabled(true);
        loadscreen.setVisibility(View.VISIBLE);
        loadbar.setVisibility(View.VISIBLE);

        task.addOnCompleteListener(res -> {
            loadscreen.setVisibility(View.INVISIBLE);
            loadbar.setVisibility(View.INVISIBLE);
            loadscreen.setEnabled(false);
        });
    }
}