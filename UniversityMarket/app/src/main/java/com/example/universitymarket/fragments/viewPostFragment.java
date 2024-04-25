package com.example.universitymarket.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.universitymarket.R;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.Chat;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.models.Transaction;
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.example.universitymarket.viewmodels.RecordsViewModel;
import com.example.universitymarket.viewmodels.WatchViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class viewPostFragment extends Fragment {

    private final String[] args;
    private final FragmentManager fm;
    private WatchViewModel watchViewModel;
    private RecordsViewModel recordsViewModel;
    private final String postId;
    private String chatId;
    private View viewSinglePost;
    private Button createConvo;
    private RatingBar ratingBar;
    private int currentIndex = 0;
    private final Bundle dashMessage = new Bundle();

    public viewPostFragment(String[] args, FragmentManager fm) {
        this.args = args;
        this.fm = fm;
        postId = this.args[0];
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for the fragment
        View view = inflater.inflate(R.layout.fragment_view_post, container, false);
        watchViewModel = new ViewModelProvider(requireActivity()).get(WatchViewModel.class);
        recordsViewModel = new ViewModelProvider(requireActivity()).get(RecordsViewModel.class);

        // Find the button and set the click listener
        Button addWL = view.findViewById(R.id.addwl);

        if(ActiveUser.watch_ids.contains(this.postId))
        {
            addWL.setText("Remove from Watchlist");
        }
        if(ActiveUser.post_ids.contains(this.postId))
        {
            addWL.setText("Delete Post");
        }

        addWL.setOnClickListener(v -> {
            if(!ActiveUser.post_ids.contains(this.postId)) {
                if (ActiveUser.watch_ids.contains(this.postId)) {
                    watchViewModel.removeWatchPost(this.postId);
                    addWL.setText("Add to Watchlist");
                    System.out.println(ActiveUser.watch_ids);
                } else {
                    watchViewModel.addWatchPost(this.postId);
                    addWL.setText("Remove from Watchlist");
                    System.out.println(ActiveUser.watch_ids);
                }
            }
            else
            {
                //Implement code to delete post from database
                Network.getPost(postId, new Callback<Post>() {
                    @Override
                    public void onSuccess(Post result) {
                        deletePost(result);
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e("getPost", error.getMessage());
                    }
                });
            }
        });

        // Configure the view post fragment
        configureViewPostFragment(view, postId);

        return view;
    }

    private void configureViewPostFragment(View view, String postID) {
        createConvo = view.findViewById(R.id.viewpost_create_conversation_button);
        if(ActiveUser.post_ids.contains(this.postId))
        {
            createConvo.setText("Mark as Sold");
        }
        ratingBar = view.findViewById(R.id.viewpost_rating_indicator);

        fetchPostAndPopulate(postID, view);
        if(ActiveUser.post_ids.contains(postID))
        {
            setupCreateConvoButton(createConvo, ActiveUser.email);
        }
    }

    private void deletePost(Post post) {
        Network.setPost(post, true, new Callback<Post>() {
            @Override
            public void onSuccess(Post result) {
                ActiveUser.post_ids.remove(postId);
                Network.setUser(Data.activeUserToPOJO(), false, new Callback<User>() {
                    @Override
                    public void onSuccess(User result) {
                        //Toast.makeText(requireActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(Exception error) { Log.e("setUser", error.getMessage()); }
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
                                        if (postId.matches(watch_id)) {
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

                // Get fragment manager of the popup
                getParentFragmentManager().setFragmentResult("closePopup", new Bundle());
            }
            @Override
            public void onFailure(Exception error) { Toast.makeText(requireActivity(), "Try Again Later", Toast.LENGTH_SHORT).show(); }
        });
    }

    private void setupCreateConvoButton(Button createConvo, String authorEmail) {
        createConvo.setEnabled(true);

        createConvo.setOnClickListener(l -> {
            if(!ActiveUser.post_ids.contains(this.postId)) {
                createConvo.setEnabled(false);
                chatId = Data.generateID("chat");
                Chat chat = new Chat(
                        new Date().toString(),
                        new ArrayList<>(Arrays.asList(ActiveUser.email, authorEmail)),
                        new ArrayList<>(),
                        chatId
                );
                Network.setChat(chat, false, new Callback<Chat>() {
                    @Override
                    public void onSuccess(Chat newChat) {
                        ActiveUser.chat_ids.add(chatId);

                        Network.setUser(ActiveUser.toPOJO(), false, null);
                        Network.getUser(authorEmail, new Callback<User>() {
                            @Override
                            public void onSuccess(User author) {
                                author.setChatIds((ArrayList<String>) Stream.concat(author.getChatIds().stream(), Stream.of(chatId)).collect(Collectors.toList()));

                                Network.setUser(author, false, null);
                            }

                            @Override
                            public void onFailure(Exception error) {
                                Log.e("getUser", error.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception error) {
                        createConvo.setEnabled(true);
                        Log.e("setChat", error.getMessage());
                    }
                });
            }
            else
            {

                Network.getPost(postId, new Callback<Post>() {
                    @Override
                    public void onSuccess(Post post) {
                        Transaction transaction = new Transaction(
                                post.getDescriptors(),
                                post.getId(),
                                post.getGenre(),
                                false,
                                post.getItemDescription(),
                                post.getId() + post.getQuantity(),
                                post.getImageContexts(),
                                post.getItemTitle(),
                                null,
                                null,
                                ActiveUser.email,
                                new Date().toString(),
                                ActiveUser.email,
                                post.getListPrice(),
                                "closed",
                                Data.generateID("tsct"));

                        Network.setTransaction(transaction, false, new Callback<Transaction>() {
                            @Override
                            public void onSuccess(Transaction result) {
                                //Log.e("String", "Entered Else Statement");
                                ActiveUser.transact_ids.add(transaction.getId());
                                Toast.makeText(requireActivity(), "Transaction Updated", Toast.LENGTH_LONG).show();
                                deletePost(post);
                                recordsViewModel.addTransaction(result.getId());
                            }

                            @Override
                            public void onFailure(Exception error) {
                                Log.e("Fail", error.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e("Auto-Fail", error.getMessage());
                    }
                });
            }
        });
    }

    private void fetchPostAndPopulate(String postID, View view) {
        // Example fetch operation
        Network.getPost(postID, new Callback<Post>() {
            @Override
            public void onSuccess(Post result) {
                //populate views
                TextView postTitle = view.findViewById(R.id.post_title_field);
                TextView postDescription = view.findViewById(R.id.post_description_field);
                ImageSwitcher postImageSwitcher = view.findViewById(R.id.post_image_switcher);
                Button authorButton = view.findViewById(R.id.viewpost_author_button);

                postTitle.setText("$" + result.getListPrice() + " - " + result.getItemTitle());
                postDescription.setText("Item Description: \n\n\t\t\t" + result.getItemDescription());

                // Set up ImageSwitcher
                postImageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
                    public View makeView() {
                        ImageView imageView = new ImageView(getContext());
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        imageView.setLayoutParams(new ImageSwitcher.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        return imageView;
                    }
                });

                postImageSwitcher.setInAnimation(getContext(), android.R.anim.slide_in_left);
                postImageSwitcher.setInAnimation(getContext(), android.R.anim.slide_out_right);

                // Load the image
                if (!result.getImageUrls().isEmpty()) {
                    Picasso.get().load(result.getImageUrls().get(0)).into((ImageView) postImageSwitcher.getCurrentView());
                } else {
                    Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/university-market-e4aa7.appspot.com/o/invalid.png?alt=media&token=4034f579-5c6f-4ac9-a38b-29e3a2b005bb").into((ImageView) postImageSwitcher.getCurrentView());
                }

                // Set up click listener for image switcher to switch to next image
                postImageSwitcher.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentIndex++;
                        if (currentIndex >= result.getImageUrls().size()) {
                            currentIndex = 0; // Wrap around to the first image
                        }
                        Picasso.get().load(result.getImageUrls().get(currentIndex)).into((ImageView) postImageSwitcher.getCurrentView());
                    }
                });

                // Get the uploader's rating and setup author button
                Network.getUser(result.getAuthorEmail(), new Callback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        String firstLast = user.getFirstName() + " " + user.getLastName();
                        String buttonText = "From " + firstLast;
                        authorButton.setText(buttonText);
                        ratingBar.setRating((float) user.getRatingsMap().values().stream().map(val -> (Float) val).collect(Collectors.toList()).stream().mapToDouble(Float::doubleValue).average().orElse(3.0F));

                        authorButton.setOnClickListener(l -> {
                            dashMessage.putString("popupTitle", firstLast);
                            dashMessage.putString("popupSubtitle", user.getEmail());
                            dashMessage.putString("popupFragment", TabFragment.class.getName());
                            dashMessage.putStringArray("popupFragArgs", new String[]{ "Profile", user.getEmail() });

                            fm.setFragmentResult("createPopup", dashMessage);
                        });

                        for(String s : user.getChatIds()) {
                            if(ActiveUser.chat_ids.contains(s)) {
                                return;
                            }
                        }
                        setupCreateConvoButton(createConvo, result.getAuthorEmail());
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e("getUser", error.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("Error", "Failed to fetch post details: " + error.getMessage());
            }
        });
    }
}