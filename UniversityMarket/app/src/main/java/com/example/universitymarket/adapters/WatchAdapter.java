package com.example.universitymarket.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.universitymarket.R;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.utilities.Data;

import java.util.List;

public class WatchAdapter extends RecyclerView.Adapter<WatchAdapter.ViewHolder> {
    private final List<Post> watched_posts;
    private OnItemClickListener itemClickListener;

    public WatchAdapter(Context context, List<Post> posts, OnItemClickListener itemClickListener) {
        watched_posts = posts;
        this.itemClickListener = itemClickListener;
    }

    public void update(List<Post> posts) {
        watched_posts.clear();
        if(posts != null) { watched_posts.addAll(posts); }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WatchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_watch_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchAdapter.ViewHolder holder, int position) {
        Post post = watched_posts.get(position);
        holder.bind(post, itemClickListener);
    }

    @Override
    public int getItemCount() {
        return watched_posts.size();
    }

    public interface OnItemClickListener {
        void onItemClicked(Post post);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView price;
        private final TextView date;
        private final TextView seller;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.watch_title_text);
            price = itemView.findViewById(R.id.watch_price_text);
            date = itemView.findViewById(R.id.watch_date_text);
            seller = itemView.findViewById(R.id.watch_seller_text);
        }

        public void bind(final Post post, final OnItemClickListener clickListener) {
            title.setText(post.getItemTitle());
            price.setText("$"+post.getListPrice());
            date.setText(Data.formatDate(Data.parseDate(post.getDateCreated()), "MMM dd, yyyy"));
            seller.setText(post.getAuthorEmail());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClicked(post);
                }
            });
        }
    }
}