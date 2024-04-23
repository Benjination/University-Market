package com.example.universitymarket.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.universitymarket.R;
import com.example.universitymarket.adapters.WatchAdapter;
import com.example.universitymarket.adapters.myPostAdapter;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.example.universitymarket.viewmodels.WatchViewModel;
import com.example.universitymarket.viewmodels.myPostsViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.List;
import java.util.stream.Collectors;

public class myPostFragment extends Fragment implements myPostAdapter.OnItemClickListener, myPostAdapter.OnItemBtnClickListener {
    private View root;
    private RecyclerView recyclerView;
    private myPostsViewModel myViewModel;
    private List<Post> myPosts;
    private TaskCompletionSource<String> load;
    private myPostAdapter adapter;
    private FragmentManager fm;
    private final String userEmail;
    private final Bundle dashMessage = new Bundle();

    public myPostFragment(FragmentManager fm, String userEmail) {
        this.fm = fm;
        this.userEmail = userEmail;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_mypost, container, false);
        recyclerView = root.findViewById(R.id.mypost_recyclerView);
        myViewModel = new ViewModelProvider(requireActivity()).get(myPostsViewModel.class);
        final Observer<List<Post>> myPostObserver = updatedList -> {
            //load = new TaskCompletionSource<>();
            //loadPage(load.getTask());
            if (myPosts == null) {
                myPosts = updatedList;
                adapter = new myPostAdapter(requireContext(), myPosts, myPostFragment.this, myPostFragment.this);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(),
                        LinearLayoutManager.VERTICAL, false));
            } else {
                adapter.update(updatedList);
                Data.updateAdapter(myPosts, updatedList, adapter);
                myPosts = updatedList;
            }
            //load.setResult("getPosts");
        };
        myViewModel.getMyPosts().observe(getViewLifecycleOwner(), myPostObserver);
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

    @Override
    public void onItemBtnClicked(Post post) {
        myViewModel.removeMyPost(post);
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