package com.example.universitymarket.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.universitymarket.R;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.objects.Post;
import com.example.universitymarket.objects.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class myPostAdapter extends RecyclerView.Adapter<myPostAdapter.ViewHolder> {
    private final List<Post> myposts;
    private final boolean isActiveUser;
    private OnItemClickListener itemClickListener;
    private OnItemBtnClickListener itemBtnClickListener;


    public myPostAdapter(Context context, List<Post> posts,
                         OnItemClickListener item, OnItemBtnClickListener itemBtn,
                         boolean isActiveUser) {
        myposts = posts;
        this.isActiveUser = isActiveUser;
        this.itemClickListener = item;
        this.itemBtnClickListener = itemBtn;
    }

    @NonNull
    @Override
    public myPostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_mypost_item,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myPostAdapter.ViewHolder holder, int position) {
        Post post = myposts.get(position);
        holder.bind(post, itemClickListener, itemBtnClickListener, isActiveUser);
    }

    @Override
    public int getItemCount() {
        return myposts.size();
    }

    public interface OnItemClickListener {
        void onItemClicked(Post post);
    }

    public interface OnItemBtnClickListener {
        void onItemBtnClicked(Post post);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView price;
        private final TextView date;
        private final TextView genre;
        private final Button trash;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.mypost_title_text);
            price = itemView.findViewById(R.id.mypost_price_text);
            date = itemView.findViewById(R.id.mypost_date_text);
            genre = itemView.findViewById(R.id.mypost_genre_text);
            trash = itemView.findViewById(R.id.mypost_trash_btn);
        }

        public void bind(final Post post, final OnItemClickListener clickListener,
                         final OnItemBtnClickListener clickBtnListener,
                         boolean isActiveUser) {
            title.setText(post.getItemTitle());
            price.setText("$"+post.getListPrice());
            date.setText(Data.formatDate(Data.parseDate(post.getDateCreated()), "MMM dd, yyyy"));
            genre.setText(post.getGenre());
            if(!isActiveUser)
                trash.setVisibility(View.INVISIBLE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClicked(post);
                }
            });

            trash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) { clickBtnListener.onItemBtnClicked(post); }
            });
        }
    }
}