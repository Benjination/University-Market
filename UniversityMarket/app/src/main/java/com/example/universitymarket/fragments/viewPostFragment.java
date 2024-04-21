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
import com.example.universitymarket.objects.Chat;
import com.example.universitymarket.objects.Post;
import com.example.universitymarket.objects.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.example.universitymarket.viewmodels.WatchViewModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class viewPostFragment extends Fragment {

    private final String[] args;
    private final FragmentManager fm;
    private WatchViewModel watchViewModel;
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

        // Find the button and set the click listener
        Button addWL = view.findViewById(R.id.addwl);
        if(ActiveUser.watch_ids.contains(this.postId))
        {
            addWL.setText("Remove from Watchlist");
        }

        addWL.setOnClickListener(v -> {

            if(ActiveUser.watch_ids.contains(this.postId))
            {
                watchViewModel.removeWatchPost(this.postId);
                addWL.setText("Add to Watchlist");
                System.out.println(ActiveUser.watch_ids);
            }
            else {
                watchViewModel.addWatchPost(this.postId);
                addWL.setText("Remove from Watchlist");
                System.out.println(ActiveUser.watch_ids);
            }
        });

        // Configure the view post fragment
        configureViewPostFragment(view, postId);

        return view;
    }

    private void configureViewPostFragment(View view, String postID) {
        createConvo = view.findViewById(R.id.viewpost_create_conversation_button);
        ratingBar = view.findViewById(R.id.viewpost_rating_indicator);

        fetchPostAndPopulate(postID, view);
    }

    private void setupCreateConvoButton(Button createConvo, String authorEmail) {
        createConvo.setOnClickListener(l -> {
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
            createConvo.setEnabled(true);
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

                // Load the image
                if (!result.getImageUrls().isEmpty()) {
                    Picasso.get().load(result.getImageUrls().get(0)).into((ImageView) postImageSwitcher.getCurrentView());
                } else {
                    Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/university-market-e4aa7.appspot.com/o/invalid.png?alt=media&token=4034f579-5c6f-4ac9-a38b-29e3a2b005bb").into((ImageView) postImageSwitcher.getCurrentView());
                }

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
                    }

                    @Override
                    public void onFailure(Exception error) {
                        Log.e("getUser", error.getMessage());
                    }
                });

                // Check if a chat has been opened
                if(!result.getAuthorEmail().equals(ActiveUser.email)) {
                    createConvo.setEnabled(true);
                    Network.getChats(ActiveUser.chat_ids, new Callback<List<Chat>>() {
                        @Override
                        public void onSuccess(List<Chat> chats) {
                            for (Chat chat : chats) {
                                // Check to see if theres a chat with the seller and ActiveUser
                                if (chat.getParticipantEmails().stream().filter(s -> s.equals(ActiveUser.email) || s.equals(result.getAuthorEmail())).count() == 2) {
                                    return;
                                }
                                setupCreateConvoButton(createConvo, result.getAuthorEmail());
                            }
                        }

                        @Override
                        public void onFailure(Exception error) {
                            Log.e("getChats", error.getMessage());
                            setupCreateConvoButton(createConvo, result.getAuthorEmail());
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("Error", "Failed to fetch post details: " + error.getMessage());
            }
        });
    }
}