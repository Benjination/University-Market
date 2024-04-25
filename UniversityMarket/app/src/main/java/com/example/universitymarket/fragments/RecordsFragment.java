package com.example.universitymarket.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.universitymarket.R;
import com.example.universitymarket.adapters.RecordsAdapter;
import com.example.universitymarket.adapters.WatchAdapter;
import com.example.universitymarket.models.Post;
import com.example.universitymarket.models.Transaction;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.viewmodels.RecordsViewModel;
import com.example.universitymarket.viewmodels.WatchViewModel;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;

import java.util.List;
import java.util.stream.Collectors;

public class RecordsFragment extends Fragment {
    private View root;
    private RecyclerView recyclerView;
    private TextView unavailable;
    private RecordsViewModel transactionsVM;
    private List<Transaction> transactions;
    private TaskCompletionSource<String> load;
    private RecordsAdapter adapter;
    private final FragmentManager fm;
    private final Bundle dashMessage = new Bundle();

    public RecordsFragment(FragmentManager fm) {
        this.fm = fm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_watch, container, false);
        recyclerView = root.findViewById(R.id.records_recycle_view);
        unavailable = root.findViewById(R.id.records_unavailable_text);
        transactionsVM = new ViewModelProvider(requireActivity()).get(RecordsViewModel.class);
        final Observer<List<Transaction>> observer = updatedList -> {
            if (transactions == null) {
                transactions = updatedList;
                adapter = new RecordsAdapter(requireContext(), transactions);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(),
                        LinearLayoutManager.VERTICAL, false));
            } else {
                adapter.update(updatedList);
                Data.updateAdapter(transactions, updatedList, adapter);
                transactions = updatedList;
            }

            if(transactions == null || updatedList.size() == 0)
                unavailable.setVisibility(View.VISIBLE);
            else
                unavailable.setVisibility(View.INVISIBLE);
        };
        transactionsVM.getWatchedPosts().observe(getViewLifecycleOwner(), observer);
        return root;
    }

    private void loadPage(Task<String> task) {
        dashMessage.putBoolean("isLoading", true);
        fm.setFragmentResult("setLoading", dashMessage);

        task.addOnCompleteListener(res -> {
            dashMessage.putBoolean("isLoading", false);
            fm.setFragmentResult("setLoading", dashMessage);
        });
    }
}