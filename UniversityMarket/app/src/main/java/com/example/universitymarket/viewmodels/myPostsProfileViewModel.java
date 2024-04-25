package com.example.universitymarket.viewmodels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.universitymarket.models.Post;
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Network;

import java.util.ArrayList;
import java.util.List;

public class myPostsProfileViewModel extends ViewModel {
    private final MutableLiveData<List<Post>> myPosts = new MutableLiveData<>();

    public myPostsProfileViewModel() {}

    public MutableLiveData<List<Post>> getUserPosts(String userClickedEmail) {
        loadUserPosts(userClickedEmail);
        return myPosts;
    }

    //get all posts from a general user
    private  void loadUserPosts(String email) {
        Network.getUser(email, new Callback<User>() {
            @Override
            public void onSuccess(User result) {
                Network.getPosts(result.getPostIds(), null, new Callback<List<Post>>() {
                    @Override
                    public void onSuccess(List<Post> result) {
                        myPosts.setValue(result);
                    }

                    @Override
                    public void onFailure(Exception error) {
                        if(error.getMessage() != null &&
                                (error.getMessage().contains("Collection 'posts' does not exist") ||
                                (error.getMessage().contains("No documents are available")))) {
                            myPosts.setValue(new ArrayList<>());
                        }
                        Log.e("myPostsProfileViewModel", "GET UserPosts: "+error.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("myPostsProfileViewModel", "GET User"+error.getMessage());
            }
        });
    }

    //a user email is passed so load that user's posts
    public void viewUserPosts(String userEmail) { loadUserPosts(userEmail); }

    //activeuser added a post so update posts
    public void addUserPost(String activeEmail) {
        loadUserPosts(activeEmail);
    }

    //activeuser remove a post so update posts
    public void removeUserPost(String activeEmail) { loadUserPosts(activeEmail); }
}
