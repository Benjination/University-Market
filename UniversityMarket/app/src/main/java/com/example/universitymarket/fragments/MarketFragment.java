package com.example.universitymarket.fragments;

import android.os.Bundle;

//import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import com.example.universitymarket.R;
import com.example.universitymarket.adapters.PostGVAdapter;
import com.example.universitymarket.objects.Post;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Network;
import com.example.universitymarket.utilities.PostModel;

import java.util.ArrayList;
import java.util.List;

public class MarketFragment extends Fragment {

    FragmentManager fm;

    public MarketFragment(FragmentManager fm) {
        this.fm = fm;
    }

    ArrayList<Post> postsArrayList = new ArrayList<>();
    ArrayList <PostModel> postModelArrayList = new ArrayList<>();
    LayoutInflater viewPostInflater;
    ViewGroup viewSinglePostContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_market, container, false);

        // Find the GridView in your layout
        GridView postsGV = view.findViewById(R.id.idGVposts);

        //find refresh button
        Button refreshButton = view.findViewById(R.id.refreshBtn);

        // Set an OnClickListener for the button
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllPosts(v);
            }
        });


        //get all posts
        getAllPosts(view);

        // Set maximum length for post name
        int maxLength = 20; // Change this value as needed

        for (PostModel postModel : postModelArrayList) {
            String originalTitle = postModel.getPost_name();
            if (originalTitle.length() > maxLength) {
                // Truncate the post name if it's too long
                String truncatedTitle = originalTitle.substring(0, maxLength) + "...";
                postModel.setPost_name(truncatedTitle);
            }
        }
        // Create the adapter and set it to the GridView
        PostGVAdapter adapter1 = new PostGVAdapter(getActivity(), postModelArrayList);
        postsGV.setAdapter(adapter1);
        postsGridViewlistener(postsGV);

        return view;
    }

    /////
    private void postsGridViewlistener(GridView postsGV){
        postsGV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
                Post selectedPost = postsArrayList.get(position); // Assuming this matches your data

                Bundle popupArgs = new Bundle();
                popupArgs.putString("popupTitle", selectedPost.getItemTitle());
                popupArgs.putString("popupFragment", "viewPostFragment");
                popupArgs.putString("popupArgument", selectedPost.getId());
                getParentFragmentManager().setFragmentResult("createPopup", popupArgs);
            }
        });
    }

    private void getAllPosts(View view){
        //get all posts
        Network.getPosts(requireActivity(), 1, new Callback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                postsArrayList.addAll(result);
                // Optionally notify your adapter or update UI here
                Log.d("GETTING FIREBASE POSTS", "SUCCESS");

                //check if posts are in postsArrayList
                for(Post p : postsArrayList){
                    Log.d("current post:" , p.getItemTitle());
                    postModelArrayList.add(new PostModel("$"+ p.getListPrice() + " - " + p.getItemTitle(), p.getImageUrls().get(0)));
                    Log.d("added " + p.getItemTitle() , "success");
                }
                PostGVAdapter adapter1 = new PostGVAdapter(getActivity(), postModelArrayList);
                GridView postsGV = view.findViewById(R.id.idGVposts);
                postsGV.setAdapter(adapter1);
            }
            @Override
            public void onFailure(Exception error) {
                Log.e("Error loading posts", error.getMessage());
            }
        });
    }

}