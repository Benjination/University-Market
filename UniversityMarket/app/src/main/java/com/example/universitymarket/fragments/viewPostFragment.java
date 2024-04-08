package com.example.universitymarket.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.example.universitymarket.DashboardActivity;
import com.example.universitymarket.R;
import com.example.universitymarket.SignIn;
import com.example.universitymarket.adapters.PostGVAdapter;
import com.example.universitymarket.objects.Post;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Network;
import com.example.universitymarket.utilities.PostModel;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class viewPostFragment extends Fragment {

    private final String postId;

    public viewPostFragment(String postId) {
        this.postId = postId;
    }

    private View viewSinglePost;
    private int currentIndex = 0;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate layout for frag
        viewSinglePost = inflater.inflate(R.layout.fragment_view_post, container, false);
        // Check if args are available
        configureViewPostFragment(viewSinglePost, postId);

        return viewSinglePost;
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