package com.example.universitymarket.viewmodels;

import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.universitymarket.adapters.myPostAdapter;
import com.example.universitymarket.fragments.myPostFragment;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;

import java.util.ArrayList;
import java.util.List;

public class myPostsProfileViewModel extends ViewModel {
    private final MutableLiveData<List<Post>> myPosts = new MutableLiveData<>();

    public myPostsProfileViewModel() {}

    public MutableLiveData<List<Post>> getMyPosts() {
        loadMyPosts();
        return myPosts;
    }

    /*
    43. Deleting a post in My Posts does not remove the associated pictures from storage.
        Solution: call Network.removeImages after post removal
    42. IMPORTANT Deleting a post in My Posts does not remove the post ID from ALL users' watchlistIDs
    31. MyPosts does not automatically refresh with the latest database results on reload,
        nor does it refresh after database action
    33. MyPosts does not have the ability to view each post (similar to marketplace), and only deletes
     */
    private void loadMyPosts() {
        Network.getPosts(ActiveUser.watch_ids, new Callback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                myPosts.setValue(result);
            }
            @Override
            public void onFailure(Exception error) {
                if(error.getMessage() != null && error.getMessage().contains("No documents are available")) {
                    myPosts.setValue(new ArrayList<>());
                }
                Log.e("getMyPostsModel", error.getMessage());
            }
        });
        if(userEmail.equals(ActiveUser.email)) {
            Network.getPosts(ActiveUser.post_ids, new Callback<List<Post>>() {
                @Override
                public void onSuccess(List<Post> result) {
                }

                @Override
                public void onFailure(Exception error) {
                    Log.e("getMyPosts", error.getMessage());
                }
            });
        } else {
            Network.getUser(userEmail, new Callback<User>() {
                @Override
                public void onSuccess(User result) {
                    Network.getPosts(result.getPostIds(), new Callback<List<Post>>() {
                        @Override
                        public void onSuccess(List<Post> result) {
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
                }
            });
        }
    }

    public void removeMyPost(String postId) {
        ActiveUser.watch_ids.remove(String.valueOf(postId));
        Network.setUser(Data.activeUserToPOJO(), false, new Callback<User>() {
            @Override
            public void onSuccess(User result) {
                loadMyPosts();
            }
            @Override
            public void onFailure(Exception error) {
                Log.e("setUsermyPostsModel", error.getMessage());
            }
        });
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
}
