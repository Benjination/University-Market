package com.example.universitymarket.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.example.universitymarket.R;
import com.example.universitymarket.adapters.HomepageAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomepageFragment extends Fragment {

    private View root;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_homepage, container, false);
        configureTabView(root);
        return root;
    }

    private void configureTabView(View view) {
        TabLayout tabs = view.findViewById(R.id.home_tabs);
        ViewPager2 pager = view.findViewById(R.id.home_pager);

        Drawable market_icon = ContextCompat.getDrawable(view.getContext(), R.drawable.market_icon);
        Drawable events_icon = ContextCompat.getDrawable(view.getContext(), R.drawable.event_icon);
        Drawable post_icon = ContextCompat.getDrawable(view.getContext(), R.drawable.post_icon);

        HomepageAdapter adapter = new HomepageAdapter((FragmentActivity) getContext());
        pager.setAdapter(adapter);
        pager.setUserInputEnabled(true);
        new TabLayoutMediator(tabs, pager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("EVENTS");
                    tab.setIcon(events_icon);
                    break;
                case 1:
                    tab.setText("MARKET");
                    tab.setIcon(market_icon);
                    break;
                case 2:
                    tab.setText("POST");
                    tab.setIcon(post_icon);
            }
        }
        ).attach();
    }


}