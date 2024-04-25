package com.example.universitymarket.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.universitymarket.R;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.utilities.Data;

import java.util.List;

public class myPostProfileAdapter extends RecyclerView.Adapter<myPostProfileAdapter.ViewHolder> {
    private final List<Post> myposts;
    private OnItemClickListener itemClickListener;


    public myPostProfileAdapter(Context context, List<Post> posts,
                                OnItemClickListener item) {
        myposts = posts;
        this.itemClickListener = item;
    }

    public void update(List<Post> posts) {
        myposts.clear();
        if(posts != null) { myposts.addAll(posts); }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public myPostProfileAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_mypost_profile_item,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myPostProfileAdapter.ViewHolder holder, int position) {
        Post post = myposts.get(position);
        holder.bind(post, itemClickListener);
    }

    @Override
    public int getItemCount() {
        return myposts.size();
    }

    public interface OnItemClickListener {
        void onItemClicked(Post post);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView price;
        private final TextView date;
        private final TextView genre;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.mypost_profile_title_text);
            price = itemView.findViewById(R.id.mypost_profile_price_text);
            date = itemView.findViewById(R.id.mypost_profile_date_text);
            genre = itemView.findViewById(R.id.mypost_profile_genre_text);
        }

        public void bind(final Post post, final OnItemClickListener clickListener) {
            title.setText(post.getItemTitle());
            price.setText("$"+post.getListPrice());
            date.setText(Data.formatDate(Data.parseDate(post.getDateCreated()), "MMM dd, yyyy"));
            genre.setText(post.getGenre());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClicked(post);
                }
            });
        }
    }
}