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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MarketFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
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
        postModelArrayList.add(new PostModel("$50 - Post Title 1", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 2", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 3", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 4", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 5", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 6", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 7", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 8", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 9", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 10", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 11", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 12", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 13", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 14", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 15", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 16", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 17", R.drawable.image_placeholder));
        postModelArrayList.add(new PostModel("$50 - Post Title 18", R.drawable.image_placeholder));

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