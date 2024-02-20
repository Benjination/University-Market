package com.example.universitymarket.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.example.universitymarket.R;
import com.example.universitymarket.adapters.PostGVAdapter;
import com.example.universitymarket.utilities.PostModel;

import java.util.ArrayList;

//public class MarketFragment extends Fragment {
//
//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
//
//    public MarketFragment() {
//        // Required empty public constructor
//    }
public class MarketFragment extends Fragment {
    private String mParam1;
    private String mParam2;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_market, container, false);

        // Find the GridView in your layout
        GridView postsGV = view.findViewById(R.id.idGVposts);

        // Create an ArrayList of PostModel objects
        ArrayList<PostModel> postModelArrayList = new ArrayList<PostModel>();
        postModelArrayList.add(new PostModel("$15 - Code Complete Textbook", R.drawable.code_complete_thumbnail));
        postModelArrayList.add(new PostModel("$5 - Raspberry Pi Charger", R.drawable.raspberry_pi_charger_thumbnail));
        postModelArrayList.add(new PostModel("$10 - Object Oriented Software Engineering Textbook", R.drawable.ooswe_thumbnail));
        postModelArrayList.add(new PostModel("$18 - Fundamentals of Software Engineering Textbook", R.drawable.fswe_thumbnail));
        postModelArrayList.add(new PostModel("$20 - MacBook Charger", R.drawable.macbook_charger_thumbnail));
        postModelArrayList.add(new PostModel("$10 - Program Development in Java Textbook", R.drawable.java_programming_thumbnail));
        postModelArrayList.add(new PostModel("$30 - Mini Fridge", R.drawable.mini_fridge_thumbnail));
        postModelArrayList.add(new PostModel("$40 - Raspberry Pi", R.drawable.raspberry_pi_thumbnail));
        postModelArrayList.add(new PostModel("$5 - Software Engineering Textbook", R.drawable.software_eng_thumbnail));
        postModelArrayList.add(new PostModel("$25 - Applying UML and Patterns Textbook", R.drawable.uml_thumbnail));
        postModelArrayList.add(new PostModel("$45 - TI-84", R.drawable.ti84_thumbnail));
        postModelArrayList.add(new PostModel("$10 - TI-30", R.drawable.ti30_thumbnail));
//        postModelArrayList.add(new PostModel("$50 - Post Title 13", R.drawable.image_placeholder));
//        postModelArrayList.add(new PostModel("$50 - Post Title 14", R.drawable.image_placeholder));
//        postModelArrayList.add(new PostModel("$50 - Post Title 15", R.drawable.image_placeholder));
//        postModelArrayList.add(new PostModel("$50 - Post Title 16", R.drawable.image_placeholder));
//        postModelArrayList.add(new PostModel("$50 - Post Title 17", R.drawable.image_placeholder));
//        postModelArrayList.add(new PostModel("$50 - Post Title 18", R.drawable.image_placeholder));

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

        return view;
    }
}
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MarketFragment.
     */
//    // TODO: Rename and change types and number of parameters
//    public static MarketFragment newInstance(String param1, String param2) {
//        MarketFragment fragment = new MarketFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_market, container, false);
//    }
//}