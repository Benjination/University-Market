package com.example.universitymarket.viewmodels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.objects.Post;
import com.example.universitymarket.objects.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;

import java.util.List;

public class WatchViewModel extends ViewModel {
    private MutableLiveData<List<Post>> watchedPosts;

    public WatchViewModel() {}

    public MutableLiveData<List<Post>> getWatchedPosts() {
        if (watchedPosts == null) {
            watchedPosts = new MutableLiveData<>();
            loadWatchPosts();
        }
        return watchedPosts;
    }

    private void loadWatchPosts() {
        Network.getPosts(ActiveUser.watch_ids, new Callback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                if(result == null) {
                    Log.e("UPDATErecycler", "1.GETPOSTS SUCCESS: NULL");
                } else {
                    for (Post post : result) {
                        Log.e("UPDATErecycler", "1.GETPOSTS SUCCESS: " + post);
                    }
                }
                watchedPosts.setValue(result);
            }
            @Override
            public void onFailure(Exception error) {
                Log.e("getWatchPostsModel", error.getMessage());
            }
        });
    }

    public void addWatchPost(String postId) {
        Log.e("n","addWatchPost beforeActUser: "+ActiveUser.watch_ids);
        ActiveUser.watch_ids.add(String.valueOf(postId));
        Log.e("n","addWatchPost afterActUser: "+ActiveUser.watch_ids);
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
        Log.e("n","removeWatchPost beforeActUser: "+ActiveUser.watch_ids);
        ActiveUser.watch_ids.remove(String.valueOf(postId));
        Log.e("n","removeWatchPost afterActUser: "+ActiveUser.watch_ids);
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
