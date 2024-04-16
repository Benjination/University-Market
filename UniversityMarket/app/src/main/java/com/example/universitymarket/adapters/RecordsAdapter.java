package com.example.universitymarket.adapters;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.universitymarket.R;
import com.example.universitymarket.objects.Post;
import com.example.universitymarket.objects.Transaction;
import com.example.universitymarket.objects.User;
import java.util.HashMap;
import java.util.List;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.ViewHolder> {

    private final HashMap<Transaction, Pair<User, Post>> transactionMap = new HashMap<>();
    private final List<Transaction> transactions;

    public RecordsAdapter(Context context, List<Transaction> transactions, List<Post> posts, List<User> users) {
        this.transactions = transactions;
        for(int i = 0; i < transactions.size(); i++)
            transactionMap.put(transactions.get(i), new Pair<>(users.get(i), posts.get(i)));
    }

    @NonNull
    @Override
    public RecordsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_record_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordsAdapter.ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        Pair<User, Post> pair = transactionMap.get(transactions.get(position));
        if(pair == null)
            return;

        String name = pair.first.getFirstName() + " " + pair.first.getLastName();
        holder.title.setText(pair.second.getItemTitle());
        holder.price.setText(transaction.getFinalAmount());
        holder.date.setText(transaction.getClosingDate());
        holder.seller.setText(name);
    }

    @Override
    public int getItemCount() {
        return transactionMap.keySet().size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView price;
        private final TextView date;
        private final TextView seller;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.record_title_text);
            price = itemView.findViewById(R.id.record_price_text);
            date = itemView.findViewById(R.id.record_date_text);
            seller = itemView.findViewById(R.id.record_seller_text);
        }
    }
}