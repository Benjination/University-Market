package com.example.universitymarket.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.universitymarket.R;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.objects.Post;
import com.example.universitymarket.objects.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.squareup.picasso.Picasso;

public class viewPostFragment extends Fragment {

    private final String[] args;
    private final FragmentManager fm;
    private final String postId;
    private View viewSinglePost;
    private int currentIndex = 0;

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

        // Find the button and set the click listener
        Button addWL = view.findViewById(R.id.addwl);
        if(ActiveUser.watch_ids.contains(this.postId))
        {
            addWL.setText("Remove from Watchlist");
        }

        addWL.setOnClickListener(v -> {

            if(ActiveUser.watch_ids.contains(this.postId))
            {
                ActiveUser.watch_ids.remove(String.valueOf(this.postId));
                addWL.setText("Add to Watchlist");
                System.out.println(ActiveUser.watch_ids);
            }
            else {
                ActiveUser.watch_ids.add(String.valueOf(this.postId));
                addWL.setText("Remove from Watchlist");
                System.out.println(ActiveUser.watch_ids);
            }

            Network.setUser(requireActivity(), Data.activeUserToPOJO(), false, new Callback<User>() {
                @Override
                public void onSuccess(User result) {
                    Toast.makeText(requireActivity(), "Updated",
                            Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception error) {
                    Log.e("setUser", error.getMessage());
                }
            });
        });

        // Configure the view post fragment
        configureViewPostFragment(view, postId);

        return view;
    }

    private void configureViewPostFragment(View view, String postID) {
        fetchPostAndPopulate(postID, view);
    }

    private void fetchPostAndPopulate(String postID, View view) {
        // Example fetch operation
        Network.getPost(requireActivity(), postID, new Callback<Post>() {
            @Override
            public void onSuccess(Post result) {
                //populate views
                TextView postTitle = view.findViewById(R.id.post_title_field);
                TextView postDescription = view.findViewById(R.id.post_description_field);
                TextView sellerInfo = view.findViewById(R.id.seller_contact_field);
                ImageSwitcher postImageSwitcher = view.findViewById(R.id.post_image_switcher);

                postTitle.setText("$" + result.getListPrice() + " - " + result.getItemTitle());
                postDescription.setText("Item Description: \n\n\t\t\t" + result.getItemDescription());
                sellerInfo.setText("Seller Contact: \n\n\t\t\t" + result.getAuthorEmail());

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
                }
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("Error", "Failed to fetch post details: " + error.getMessage());
            }
        });
    }
}