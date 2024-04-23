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
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.Filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class myPostsViewModel extends ViewModel {
    private final MutableLiveData<List<Post>> myPosts = new MutableLiveData<>();

    public myPostsViewModel() {}

    public MutableLiveData<List<Post>> getMyPosts() {
        loadMyPosts();
        return myPosts;
    }

    private void loadMyPosts() {
        Network.getPosts(ActiveUser.post_ids, null, new Callback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                myPosts.setValue(result);
            }
            @Override
            public void onFailure(Exception error) {
                if(error.getMessage() != null && error.getMessage().contains("A non-empty docID list is required")) {
                    myPosts.setValue(new ArrayList<>());
                }
                Log.e("myPostsViewModel", "GET UserPosts: "+error.getMessage());
            }
        });
    }

    public void addMyPost() {
        loadMyPosts();
    }

    public void removeMyPost(Post post) {
        String postID = post.getId();
        List<String> imgURLS = post.getImageUrls();
        //remove post
        Network.setPost(post, true, new Callback<Post>() {
            @Override
            public void onSuccess(Post result) {
                //remove post from activeuser and update user in db
                ActiveUser.post_ids.remove(String.valueOf(postID));
                Network.setUser(Data.activeUserToPOJO(), false, new Callback<User>() {
                    @Override
                    public void onSuccess(User result) {
                        loadMyPosts();
                        Log.e("myPostsViewModel", "DEL UserPostID SUCCESS");
                    }
                    @Override
                    public void onFailure(Exception error) { Log.e("myPostsViewModel", "DEL UserPostID: "+error.getMessage()); }
                });
                // remove post from ALL users Watchlists
                // needs improvement. need to filter and possibly remove IN db
                Network.getAllUsers(null, new Callback<List<User>>() {
                    @Override
                    public void onSuccess(List<User> result) {
                        List<User> updatedUsers = new ArrayList<>();
                        if(result != null) {
                            for (User user : result) {
                                List<String> user_watch_ids = user.getWatchIds();
                                if(user_watch_ids != null) {
                                    Iterator<String> iterator = user_watch_ids.iterator();
                                    while (iterator.hasNext()) {
                                        String watch_id = iterator.next();
                                        if (postID.matches(watch_id)) {
                                            iterator.remove();
                                            updatedUsers.add(user);
                                        }
                                    }
                                }
                            }
                        }
                        if(!updatedUsers.isEmpty()) {
                            User[] array = new User[updatedUsers.size()];
                            updatedUsers.toArray(array);
                            Network.setUsers(array, false, null);
                            Log.e("myPostsViewModel", "SET AllUsers SUCCESS");
                        }
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e("myPostsViewModel", "GET AllUsers: "+error.getMessage());
                    }
                });
            }
            @Override
            public void onFailure(Exception error) { Log.e("myPostsViewModel", "DEL Post: "+error.getMessage());  }
        });
        //remove images attached to deleted post
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
