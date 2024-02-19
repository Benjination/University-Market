package com.example.universitymarket.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.universitymarket.R;
import com.example.universitymarket.utilities.PostModel;

import java.util.ArrayList;

public class PostGVAdapter extends ArrayAdapter<PostModel> {

    public PostGVAdapter(@NonNull Context context, ArrayList<PostModel> postModelArrayList) {
        super(context, 0, postModelArrayList);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View listitemView = convertView;
        if (listitemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listitemView = LayoutInflater.from(getContext()).inflate(R.layout.card_item, parent, false);
        }

        PostModel postModel = getItem(position);
        TextView postTitle = listitemView.findViewById(R.id.postTitle);
        ImageView postIMG = listitemView.findViewById(R.id.postIMG);

        postTitle.setText(postModel.getPost_name());
        postIMG.setImageResource(postModel.getImgid());
        return listitemView;
    }
}
