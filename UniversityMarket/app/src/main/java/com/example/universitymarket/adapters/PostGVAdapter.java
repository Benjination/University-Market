package com.example.universitymarket.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;

import com.example.universitymarket.R;
import com.example.universitymarket.globals.Policy;
import com.example.universitymarket.utilities.PostModel;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostGVAdapter extends ArrayAdapter<PostModel> {

    public PostGVAdapter( Context context, ArrayList<PostModel> postModelArrayList) {
        super(context, 0, postModelArrayList);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listitemView = convertView;
        if (listitemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listitemView = LayoutInflater.from(getContext()).inflate(R.layout.card_item, parent, false);
        }

        PostModel postModel = getItem(position);
        TextView postTitle = listitemView.findViewById(R.id.postTitle);
        ImageView postIMG = listitemView.findViewById(R.id.postIMG);

        postTitle.setText(postModel.getPost_name());
        Picasso.get().load(postModel.getImageURL()).into(postIMG, new Callback() {
            @Override
            public void onSuccess() {}

            @Override
            public void onError(Exception ignored) {
                Picasso.get().load(Policy.invalid_image.get(0)).into(postIMG);
            }
        });
        return listitemView;
    }
}
