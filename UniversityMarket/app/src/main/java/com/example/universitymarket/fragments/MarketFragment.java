package com.example.universitymarket.fragments;

import android.os.Bundle;

//import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
//import android.widget.Filter;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.universitymarket.R;
import com.example.universitymarket.adapters.PostGVAdapter;
import com.example.universitymarket.globals.Policy;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Network;
import com.example.universitymarket.utilities.PostModel;
import com.example.universitymarket.utilities.SortByField;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.Query;


import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MarketFragment extends Fragment {

    private FragmentManager fm;
    private GridView postsGV;  // Declare GridView as a class member
    public ArrayList<Post> postsArrayList = new ArrayList<>();
    public ArrayList <PostModel> postModelArrayList = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    public TextView refine_filter_message;

    public MarketFragment(FragmentManager fm) {
        this.fm = fm;
    }

    public LayoutInflater viewPostInflater;
    public ViewGroup viewSinglePostContainer;

    static SortByField upload_sort_by = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_market, container, false);
        postsGV = view.findViewById(R.id.idGVposts); // Find the GridView in your layout
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        refine_filter_message = view.findViewById(R.id.refine_filters);

        // Create the adapter and set it to the GridView with current posts
        PostGVAdapter adapter1 = new PostGVAdapter(getActivity(), postModelArrayList);
        postsGV.setAdapter(adapter1);

        // Set an refresh listener
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //assign direction from users sort choice
                upload_sort_by = null;
                if(FilterFragment.selected_uploadDate_filter != null){
                    upload_sort_by = new SortByField();
                    if(FilterFragment.selected_uploadDate_filter.getText().toString().equals("Newest to Oldest")){
                        upload_sort_by.direction = Query.Direction.DESCENDING;
                    }
                    else{
                        upload_sort_by.direction = Query.Direction.ASCENDING;
                    }
                    upload_sort_by.fieldName = "about.date_created";
                }

                //check if there are filters to be applied
                if(FilterFragment.selected_genre_filter != null || FilterFragment.selected_price_filter != null || FilterFragment.selected_uploadDate_filter != null) {
                    getFilteredPosts(FilterFragment.selected_genre_filter, FilterFragment.selected_price_filter, upload_sort_by);
                }
                else
                    getAllPosts();
            }
        });

        getAllPosts();//initial fetch of posts in DB
        postsGridViewlistener(postsGV);
        return view;
    }


    private void getFilteredPosts(RadioButton selected_genre_filter, RadioButton selected_price_filter, SortByField upload_sort_by){
        Filter priceFilter = new Filter();
        priceFilter = null;

        Filter postsFilter = null;
        if (selected_genre_filter != null) {
            Filter genreFilter = Filter.equalTo("about.genre", selected_genre_filter.getText().toString());
            postsFilter = postsFilter == null ? genreFilter : Filter.and(postsFilter, genreFilter);
        }
        if (selected_price_filter != null) {
            String tempPriceString = selected_price_filter.getText().toString();
            String selectedPriceFilterString = tempPriceString.substring(3);
            if (tempPriceString.charAt(0) == '>'){
                priceFilter = Filter.greaterThanOrEqualTo("about.list_price", Float.parseFloat(selectedPriceFilterString));
            }
            else {
                priceFilter = Filter.lessThanOrEqualTo("about.list_price", Float.parseFloat(selectedPriceFilterString));
            }
            postsFilter = postsFilter == null ? priceFilter : Filter.and(postsFilter, priceFilter);
        }

        Log.i("upload_sort_by: ", upload_sort_by == null ? "null" : upload_sort_by.direction.toString());

        Network.getPosts(postsFilter, upload_sort_by, 1, new Callback<List<Post>>() {

            @Override
            public void onSuccess(List<Post> result) {
                postsArrayList.clear();
                postModelArrayList.clear();
                postsArrayList.addAll(result);
                postsGV.setVisibility(View.VISIBLE);
                refine_filter_message.setVisibility(View.INVISIBLE);

                //put all post into post model form
                for(Post p : postsArrayList){
                    // Create a DecimalFormat object with pattern for two decimal places
                    DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                    // Format the list price to two decimal places
                    String formattedPrice = decimalFormat.format(p.getListPrice());
                    // Create the title string with the formatted price
                    String title = "$" + formattedPrice + " - " + p.getItemTitle();
                    // Add the formatted title to the postModelArrayList
                    List<String> imageUrls = p.getImageUrls().isEmpty() ? Policy.invalid_image : p.getImageUrls();
                    postModelArrayList.add(new PostModel(title, imageUrls.get(0)));
                    Log.d("added with filter" + p.getItemTitle(), "success");
                }
                if(postsGV != null){
                    PostGVAdapter adapter = (PostGVAdapter) postsGV.getAdapter();
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);//stop refreshing animation
                }
            }
            @Override
            public void onFailure(Exception error) {
                swipeRefreshLayout.setRefreshing(false);// Stop the refreshing animation
                Log.e("Error loading posts with filter", error.getMessage());
                postsGV.setVisibility(View.INVISIBLE);
                refine_filter_message.setVisibility(View.VISIBLE);
            }
        });
    }

    private void postsGridViewlistener(GridView postsGV){
        postsGV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
                Post selectedPost = postsArrayList.get(position); // Assuming this matches your data

                Bundle popupArgs = new Bundle();
                popupArgs.putString("popupTitle", selectedPost.getItemTitle());
                popupArgs.putString("popupFragment", viewPostFragment.class.getName());
                popupArgs.putStringArray("popupFragArgs", new String[]{ selectedPost.getId() });
                getParentFragmentManager().setFragmentResult("createPopup", popupArgs);
            }
        });
    }

    private void getAllPosts(){
        Network.getPosts(null, null, 1, new Callback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> result) {
                postsGV.setVisibility(View.VISIBLE);
                refine_filter_message.setVisibility(View.INVISIBLE);
                postsArrayList.clear();
                postModelArrayList.clear();

                postsArrayList.addAll(result);

                //put all post into post model form
                for(Post p : postsArrayList){
                    // Create a DecimalFormat object with pattern for two decimal places
                    DecimalFormat decimalFormat = new DecimalFormat("#0.00");
                    // Format the list price to two decimal places
                    String formattedPrice = decimalFormat.format(p.getListPrice());
                    // Create the title string with the formatted price
                    String title = "$" + formattedPrice + " - " + p.getItemTitle();
                    // Add the formatted title to the postModelArrayList
                    List<String> imageUrls = p.getImageUrls().isEmpty() ? Policy.invalid_image : p.getImageUrls();
                    postModelArrayList.add(new PostModel(title, imageUrls.get(0)));
                }
                if(postsGV != null){
                    PostGVAdapter adapter = (PostGVAdapter) postsGV.getAdapter();
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);//stop refreshing animation
                }
            }
            @Override
            public void onFailure(Exception error) {
                swipeRefreshLayout.setRefreshing(false);// Stop the refreshing animation
                refine_filter_message.setVisibility(View.VISIBLE);
                Log.e("Error loading posts no filter", error.getMessage());
            }
        });
    }
}