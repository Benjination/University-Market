package com.example.universitymarket.fragments;

import android.os.Bundle;

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

import com.example.universitymarket.R;
import com.example.universitymarket.adapters.WatchAdapter;
import com.example.universitymarket.objects.Post;
import com.example.universitymarket.viewmodels.WatchViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.List;

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
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false));
        watchViewModel = new ViewModelProvider(requireActivity()).get(WatchViewModel.class);
        final Observer<List<Post>> watchObserver = updatedList -> {
            if(watchedPosts == null) {
                Log.e("UPDATErecycler","2.testObserver OLD: NULL");
            } else {
                for (Post post : watchedPosts) {
                    Log.e("UPDATErecycler","2.testObserver OLD: "+post.getItemTitle());
                }
            }
            if(updatedList == null) {
                Log.e("UPDATErecycler","2.testObserver NEW: NULL");
            } else {
                for (Post post : updatedList) {
                    Log.e("UPDATErecycler","2.testObserver NEW: "+post.getItemTitle());
                }
            }
            //load = new TaskCompletionSource<>();
            //loadPage(load.getTask());
            if (watchedPosts == null) {
                watchedPosts = updatedList;
                adapter = new WatchAdapter(requireContext(), watchedPosts, WatchFragment.this);
                recyclerView.setAdapter(adapter);
            } else {
                Log.e("UPDATErecycler","3.testObserverElse");
                DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                    @Override
                    public int getOldListSize() {
                        return watchedPosts.size();
                    }
                    @Override
                    public int getNewListSize() {
                        return updatedList.size();
                    }
                    @Override
                    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                        return watchedPosts.get(oldItemPosition).getId().equals(
                                updatedList.get(newItemPosition).getId());
                    }
                    @Override
                    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                        Post oldPost = watchedPosts.get(oldItemPosition);
                        Post newPost = updatedList.get(newItemPosition);
                        return oldPost.equals(newPost);
                    }
                });
                //result.dispatchUpdatesTo(adapter);
                watchedPosts.clear();
                watchedPosts.addAll(updatedList);
                adapter.setPosts(updatedList);
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