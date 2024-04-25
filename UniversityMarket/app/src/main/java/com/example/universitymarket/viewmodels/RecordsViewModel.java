package com.example.universitymarket.viewmodels;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.models.Transaction;
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;

import java.util.ArrayList;
import java.util.List;

public class RecordsViewModel extends ViewModel {
    private final MutableLiveData<List<Transaction>> transactions = new MutableLiveData<>();

    public RecordsViewModel() {}

    public MutableLiveData<List<Transaction>> getWatchedPosts() {
        loadTransactions();
        return transactions;
    }

    private void loadTransactions() {
        Network.getTransactions(ActiveUser.transact_ids, null, new Callback<List<Transaction>>() {
            @Override
            public void onSuccess(List<Transaction> result) {
                transactions.setValue(result);
            }
            @Override
            public void onFailure(Exception error) {
                if(error.getMessage() != null && error.getMessage().contains("No documents are available")) {
                    transactions.setValue(new ArrayList<>());
                }
                Log.e("getTransactions", error.getMessage());
            }
        });
    }

    public void addTransaction(String tsctId) {
        ActiveUser.transact_ids.add(String.valueOf(tsctId));
        Network.setUser(Data.activeUserToPOJO(), false, new Callback<User>() {
            @Override
            public void onSuccess(User result) {
                loadTransactions();
            }
            @Override
            public void onFailure(Exception error) {
                Log.e("setUser", error.getMessage());
            }
        });
    }

    public void removeTransaction(String tsctId) {
        ActiveUser.transact_ids.remove(String.valueOf(tsctId));
        Network.setUser(Data.activeUserToPOJO(), false, new Callback<User>() {
            @Override
            public void onSuccess(User result) {
                loadTransactions();
            }
            @Override
            public void onFailure(Exception error) {
                Log.e("setUser", error.getMessage());
            }
        });
    }
}
