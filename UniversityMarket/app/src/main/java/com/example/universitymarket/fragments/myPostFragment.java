package com.example.universitymarket.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.universitymarket.R;
import com.example.universitymarket.adapters.myPostAdapter;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.List;

public class myPostFragment extends Fragment implements myPostAdapter.OnItemClickListener, myPostAdapter.OnItemBtnClickListener {
    private View root;
    private RecyclerView recyclerView;
    private TaskCompletionSource<String> load;
    private myPostAdapter adapter;
    private FragmentManager fm;
    private final String userEmail;
    private final Bundle dashMessage = new Bundle();

    public myPostFragment(FragmentManager fm, String userEmail) {
        this.userEmail = userEmail;
        this.fm = fm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_mypost, container, false);
        configure(root);
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
        Network.setPost(post, true, new Callback<Post>() {
            @Override
            public void onSuccess(Post result) {
                ActiveUser.post_ids.remove(String.valueOf(post.getId()));
                Network.setUser(Data.activeUserToPOJO(), false, new Callback<User>() {
                    @Override
                    public void onSuccess(User result) { Toast.makeText(requireActivity(), "Deleted", Toast.LENGTH_SHORT).show(); }
                    @Override
                    public void onFailure(Exception error) { Log.e("setUser", error.getMessage()); }
                });
            }
            @Override
            public void onFailure(Exception error) { Toast.makeText(requireActivity(), "Try Again Later", Toast.LENGTH_SHORT).show(); }
        });
    }

    private void configure(View v) {
        recyclerView = v.findViewById(R.id.mypost_recyclerView);

        load = new TaskCompletionSource<>();
        loadPage(load.getTask());
        if(userEmail.equals(ActiveUser.email)) {
            Network.getPosts(ActiveUser.post_ids, null, new Callback<List<Post>>() {
                @Override
                public void onSuccess(List<Post> result) {
                    adapter = new myPostAdapter(requireContext(), result, myPostFragment.this, myPostFragment.this, true);
                    load.setResult("getMyPosts");
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(),
                            LinearLayoutManager.VERTICAL, false));
                }

                @Override
                public void onFailure(Exception error) {
                    Log.e("getMyPosts", error.getMessage());
                    Toast.makeText(
                            getContext(),
                            error.getMessage(),
                            Toast.LENGTH_SHORT
                    ).show();
                    load.setResult("getMyPosts");
                }
            });
        } else {
            Network.getUser(userEmail, new Callback<User>() {
                @Override
                public void onSuccess(User result) {
                    Network.getPosts(result.getPostIds(), null, new Callback<List<Post>>() {
                        @Override
                        public void onSuccess(List<Post> result) {
                            adapter = new myPostAdapter(requireContext(), result, myPostFragment.this, myPostFragment.this, false);
                            load.setResult("getMyPosts");
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(),
                                    LinearLayoutManager.VERTICAL, false));
                        }

                        @Override
                        public void onFailure(Exception error) {
                            Log.e("getPosts", error.getMessage());
                        }
                    });
                }

                @Override
                public void onFailure(Exception error) {
                    Log.e("getUser", error.getMessage());
                    load.setResult("getMyPosts");
                }
            });
        }
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