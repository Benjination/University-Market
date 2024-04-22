package com.example.universitymarket.viewmodels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;

import java.util.ArrayList;
import java.util.List;

public class WatchViewModel extends ViewModel {
    private final MutableLiveData<List<Post>> watchedPosts = new MutableLiveData<>();

    public WatchViewModel() {}

    public MutableLiveData<List<Post>> getWatchedPosts() {
        loadWatchPosts();
        return watchedPosts;
    }

    private void loadWatchPosts() {
        Network.getPosts(ActiveUser.watch_ids, null, new Callback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                watchedPosts.setValue(result);
            }
            @Override
            public void onFailure(Exception error) {
                if(error.getMessage() != null && error.getMessage().contains("No documents are available")) {
                    watchedPosts.setValue(new ArrayList<>());
                }
                Log.e("getWatchPostsModel", error.getMessage());
            }
        });
    }

    public void addWatchPost(String postId) {
        ActiveUser.watch_ids.add(String.valueOf(postId));
        Network.setUser(Data.activeUserToPOJO(), false, new Callback<User>() {
            @Override
            public void onSuccess(User result) {
                loadWatchPosts();
            }
            @Override
            public void onFailure(Exception error) {
                Log.e("setUserWatchModel", error.getMessage());
            }
        });
    }

    public void removeWatchPost(String postId) {
        ActiveUser.watch_ids.remove(String.valueOf(postId));
        Network.setUser(Data.activeUserToPOJO(), false, new Callback<User>() {
            @Override
            public void onSuccess(User result) {
                loadWatchPosts();
            }
            @Override
            public void onFailure(Exception error) {
                Log.e("setUserWatchModel", error.getMessage());
            }
        });
    }
}
