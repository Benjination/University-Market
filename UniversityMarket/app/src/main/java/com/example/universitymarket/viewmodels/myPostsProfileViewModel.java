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
                        if(error.getMessage() != null && error.getMessage().contains("No documents are available")) {
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
/*
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

 */
}
