package com.example.universitymarket.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
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
import com.example.universitymarket.objects.Post;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Network;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.List;

public class WatchFragment extends Fragment implements WatchAdapter.OnItemClickListener {
    private View root;
    private RecyclerView recyclerView;
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
        configure(root);
        return root;
    }

    @Override
    public void onItemClicked(Post post) {
        Bundle popupArgs = new Bundle();
        popupArgs.putString("popupTitle", post.getItemTitle());
        popupArgs.putString("popupFragment", "viewPostFragment");
        popupArgs.putStringArray("popupFragArgs", new String[]{ post.getId() });
        fm.setFragmentResult("createPopup", popupArgs);
    }

    private void configure(View v) {
        recyclerView = v.findViewById(R.id.watch_recyclerView);

        load = new TaskCompletionSource<>();
        loadPage(load.getTask());
        Network.getPosts(requireActivity(), ActiveUser.watch_ids, new Callback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                adapter = new WatchAdapter(requireContext(), result, WatchFragment.this);
                load.setResult("getPosts");
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(),
                        LinearLayoutManager.VERTICAL, false));
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("getPosts", error.getMessage());
                Toast.makeText(
                        getContext(),
                        error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
                load.setResult("getPosts");
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