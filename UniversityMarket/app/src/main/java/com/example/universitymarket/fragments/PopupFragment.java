package com.example.universitymarket.fragments;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.universitymarket.R;
import com.example.universitymarket.utilities.Data;

public class PopupFragment extends Fragment {

    private View root;
    private Toolbar toolbar;
    private LayoutInflater inflater;
    private ViewGroup container;
    private FragmentManager parentFM;
    private FragmentManager childFM;
    private final String title;
    private final String subtitle;
    private final Fragment display;

    public PopupFragment(String title, String subtitle, Fragment display) {
        this.title = title;
        this.subtitle = subtitle;
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
        if(subtitle != null) {
            TypedValue actionBarTV = new TypedValue();
            requireActivity().getTheme().resolveAttribute(R.attr.actionBarSize, actionBarTV, true);
            ViewGroup.LayoutParams params = toolbar.getLayoutParams();
            params.height = Data.convertComplexToPixel(requireActivity(), actionBarTV.data) * 2;

            toolbar.setLayoutParams(params);
            toolbar.setSubtitle(subtitle);
        }

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