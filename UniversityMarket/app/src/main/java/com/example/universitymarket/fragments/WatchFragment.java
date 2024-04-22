package com.example.universitymarket.fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.universitymarket.R;
import com.example.universitymarket.adapters.WatchAdapter;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.example.universitymarket.viewmodels.WatchViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.List;
import java.util.stream.Collectors;

public class WatchFragment extends Fragment implements WatchAdapter.OnItemClickListener {
    private View root;
    private RecyclerView recyclerView;
    private WatchViewModel watchViewModel;
    private List<Post> watchedPosts;
    private TaskCompletionSource<String> load;
    private WatchAdapter adapter;
    private FragmentManager fm;
    private final Bundle dashMessage = new Bundle();

    public WatchFragment(FragmentManager fm) {
        this.fm = fm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_watch, container, false);
        recyclerView = root.findViewById(R.id.watch_recyclerView);
        watchViewModel = new ViewModelProvider(requireActivity()).get(WatchViewModel.class);
        final Observer<List<Post>> watchObserver = updatedList -> {
            //load = new TaskCompletionSource<>();
            //loadPage(load.getTask());
            if (watchedPosts == null) {
                watchedPosts = updatedList;
                adapter = new WatchAdapter(requireContext(), watchedPosts, WatchFragment.this);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(),
                        LinearLayoutManager.VERTICAL, false));
            } else {
                Log.e("UPDATErecycler","test");
                Log.e("lists", "\n" + watchedPosts.stream().map(Post::getId).collect(Collectors.toList()) + "\n" + updatedList.stream().map(Post::getId).collect(Collectors.toList()));
                adapter.update(updatedList);
                Data.updateAdapter(watchedPosts, updatedList, adapter);
                watchedPosts = updatedList;
            }
            //load.setResult("getPosts");
        };
        watchViewModel.getWatchedPosts().observe(getViewLifecycleOwner(), watchObserver);
        return root;
    }

    @Override
    public void onItemClicked(Post post) {
        Bundle popupArgs = new Bundle();
        popupArgs.putString("popupTitle", post.getItemTitle());
        popupArgs.putString("popupFragment", viewPostFragment.class.getName());
        popupArgs.putStringArray("popupFragArgs", new String[]{ post.getId() });
        fm.setFragmentResult("createPopup", popupArgs);
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