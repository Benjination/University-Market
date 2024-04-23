package com.example.universitymarket.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.universitymarket.R;
import com.example.universitymarket.adapters.myPostAdapter;
import com.example.universitymarket.adapters.myPostProfileAdapter;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.viewmodels.myPostsProfileViewModel;
import com.example.universitymarket.viewmodels.myPostsViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.List;
import java.util.stream.Collectors;

public class myPostProfileFragment extends Fragment implements myPostProfileAdapter.OnItemClickListener {
    private View root;
    private RecyclerView recyclerView;
    private myPostsProfileViewModel myViewModel;
    private List<Post> myPosts;
    private TaskCompletionSource<String> load;
    private myPostProfileAdapter adapter;
    private FragmentManager fm;
    private final Bundle dashMessage = new Bundle();

    public myPostProfileFragment(FragmentManager fm) {
        this.fm = fm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_mypost_profile, container, false);
        recyclerView = root.findViewById(R.id.mypost_profile_recyclerView);
        myViewModel = new ViewModelProvider(requireActivity()).get(myPostsProfileViewModel.class);
        final Observer<List<Post>> myPostProfileObserver = updatedList -> {
            //load = new TaskCompletionSource<>();
            //loadPage(load.getTask());
            if (myPosts == null) {
                myPosts = updatedList;
                adapter = new myPostProfileAdapter(requireContext(), myPosts, myPostProfileFragment.this);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(),
                        LinearLayoutManager.VERTICAL, false));
            } else {
                Log.e("UPDATErecycler","test");
                Log.e("lists", "\n" + myPosts.stream().map(Post::getId).collect(Collectors.toList()) + "\n" + updatedList.stream().map(Post::getId).collect(Collectors.toList()));
                adapter.update(updatedList);
                Data.updateAdapter(myPosts, updatedList, adapter);
                myPosts = updatedList;
            }
            //load.setResult("getPosts");
        };
        myViewModel.getMyPosts().observe(getViewLifecycleOwner(), myPostProfileObserver);
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