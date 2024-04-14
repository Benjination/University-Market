package com.example.universitymarket.fragments;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.universitymarket.R;

public class PopupFragment extends Fragment {

    private View root;
    private Toolbar toolbar;
    private LayoutInflater inflater;
    private ViewGroup container;
    private FragmentManager parentFM;
    private FragmentManager childFM;
    private final String title;
    private final Fragment display;

    public PopupFragment(String title, Fragment display) {
        this.title = title;
        this.display = display;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = container;
        root = inflater.inflate(R.layout.fragment_popup, container, false);
        parentFM = getParentFragmentManager();
        childFM = getChildFragmentManager();
        configure(root);

        return root;
    }

    private void configure(View v) {
        toolbar = v.findViewById(R.id.popup_toolbar);
        toolbar.setTitle(title);

        toolbar.setNavigationOnClickListener((view) -> parentFM
                .beginTransaction()
                .remove(this)
                .commit()
        );

        childFM
                .beginTransaction()
                .replace(R.id.popup_fragment_buffer, display)
                .commit();
    }
}