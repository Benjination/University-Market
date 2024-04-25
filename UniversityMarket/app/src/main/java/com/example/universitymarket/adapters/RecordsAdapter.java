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
import com.example.universitymarket.models.Transaction;
import com.example.universitymarket.utilities.Data;

import java.util.List;

public class RecordsAdapter extends RecyclerView.Adapter<RecordsAdapter.ViewHolder> {
    private final List<Transaction> transactions;

    public RecordsAdapter(Context context, List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void update(List<Transaction> tscts) {
        transactions.clear();
        if(tscts != null) { transactions.addAll(tscts); }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecordsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_record_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordsAdapter.ViewHolder holder, int position) {
        Transaction tsct = transactions.get(position);
        holder.bind(tsct);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
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

        public void bind(final Transaction tsct) {
            title.setText(tsct.getTitle());
            price.setText("$"+tsct.getFinalAmount());
            date.setText(Data.formatDate(Data.parseDate(tsct.getClosingDate()), "MMM dd, yyyy"));
            seller.setText(tsct.getSellerEmail());
        }
    }
}