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

public class myPostsViewModel extends ViewModel {
    private final MutableLiveData<List<Post>> myPosts = new MutableLiveData<>();

    public myPostsViewModel() {}

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
        Network.getPosts(ActiveUser.post_ids, new Callback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                myPosts.setValue(result);
            }
            @Override
            public void onFailure(Exception error) {
                if(error.getMessage() != null && error.getMessage().contains("No documents are available")) {
                    myPosts.setValue(new ArrayList<>());
                }
                Log.e("myPostsViewModel", "GET UserPosts: "+error.getMessage());
            }
        });
    }

    public void removeMyPost(Post post) {
        String postID = post.getId();
        List<String> imgURLS = post.getImageUrls();
        Network.setPost(post, true, new Callback<Post>() {
            @Override
            public void onSuccess(Post result) {
                ActiveUser.post_ids.remove(String.valueOf(postID));
                Network.setUser(Data.activeUserToPOJO(), false, new Callback<User>() {
                    @Override
                    public void onSuccess(User result) { loadMyPosts(); }
                    @Override
                    public void onFailure(Exception error) { Log.e("myPostsViewModel", "DEL UserPostID: "+error.getMessage()); }
                });
                // add remove postID from ALL user Watchlists here
            }
            @Override
            public void onFailure(Exception error) { Log.e("myPostsViewModel", "DEL Post: "+error.getMessage());  }
        });
        Network.removeImages(imgURLS, new Callback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {}
            @Override
            public void onFailure(Exception error) {
                Log.e("myPostsViewModel", "DEL PostImgs: "+error.getMessage());
            }
        });
    }
}
