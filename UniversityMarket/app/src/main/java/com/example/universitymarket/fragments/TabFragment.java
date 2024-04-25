package com.example.universitymarket.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.universitymarket.R;
import com.example.universitymarket.adapters.TabAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TabFragment extends Fragment {

    private View root;
    private LayoutInflater inflater;
    private ViewGroup container;
    private final FragmentManager fm;
    private final String[] args;
    private final Bundle dashMessage = new Bundle();

    public TabFragment(String[] args, FragmentManager fm) {
        this.args = args;
        this.fm = fm;
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
        root = inflater.inflate(R.layout.fragment_tab, container, false);
        configure(root);
        return root;
    }

    private void configure(View view) {
        TabLayout tabs = view.findViewById(R.id.home_tabs);
        ViewPager2 pager = view.findViewById(R.id.home_pager);

        TypedValue colorPrimary = new TypedValue();
        TypedValue colorOnTertiary = new TypedValue();
        requireActivity().getTheme().resolveAttribute(R.attr.colorPrimary, colorPrimary, true);
        requireActivity().getTheme().resolveAttribute(R.attr.colorOnTertiary, colorOnTertiary, true);

        View left = inflater.inflate(R.layout.layout_tab_item, container, false);
        View right = inflater.inflate(R.layout.layout_tab_item, container, false);

        if(args[0].equals("Market")) {
            ((ImageView) left.findViewById(R.id.tab_icon)).setImageResource(R.drawable.cart_icon);
            ((TextView) left.findViewById(R.id.tab_text)).setText(R.string.tab_browse_txt);
            ((ImageView) right.findViewById(R.id.tab_icon)).setImageResource(R.drawable.filter_icon);
            ((TextView) right.findViewById(R.id.tab_text)).setText(R.string.tab_filter_txt);
        } else if(args[0].equals("Post")) {
            ((ImageView) left.findViewById(R.id.tab_icon)).setImageResource(R.drawable.post_icon);
            ((TextView) left.findViewById(R.id.tab_text)).setText(R.string.tab_create_txt);
            ((ImageView) right.findViewById(R.id.tab_icon)).setImageResource(R.drawable.record_icon);
            ((TextView) right.findViewById(R.id.tab_text)).setText(R.string.tab_view_txt);
        } else if(args[0].equals("Watch")) {
            ((ImageView) left.findViewById(R.id.tab_icon)).setImageResource(R.drawable.clock_icon);
            ((TextView) left.findViewById(R.id.tab_text)).setText(R.string.tab_watch_txt);
            ((ImageView) right.findViewById(R.id.tab_icon)).setImageResource(R.drawable.receipt_icon);
            ((TextView) right.findViewById(R.id.tab_text)).setText(R.string.tab_bookkeep_txt);
        } else {
            ((ImageView) left.findViewById(R.id.tab_icon)).setImageResource(R.drawable.profile_icon);
            ((TextView) left.findViewById(R.id.tab_text)).setText(R.string.tab_portfolio_txt);
            ((ImageView) right.findViewById(R.id.tab_icon)).setImageResource(R.drawable.record_icon);
            ((TextView) right.findViewById(R.id.tab_text)).setText(R.string.tab_view_txt);
        }

        TabAdapter adapter = new TabAdapter(requireActivity(), fm, args);
        pager.setAdapter(adapter);
        pager.setUserInputEnabled(false);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View custView = tab.getCustomView();
                if(custView == null)
                    return;

                int position = Math.max(0, tab.getPosition());
                ((ImageView) custView.findViewById(R.id.tab_icon)).setColorFilter(colorPrimary.data);
                ((TextView) custView.findViewById(R.id.tab_text)).setTextColor(colorPrimary.data);
                dashMessage.putInt("currentTab", position);
                dashMessage.putString("currentTabGroup", args[0]);
                fm.setFragmentResult("tabSwitch", dashMessage);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View custView = tab.getCustomView();
                if(custView == null)
                    return;

                ((ImageView) custView.findViewById(R.id.tab_icon)).setColorFilter(colorOnTertiary.data);
                ((TextView) custView.findViewById(R.id.tab_text)).setTextColor(colorOnTertiary.data);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        new TabLayoutMediator(tabs, pager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setCustomView(left);
                    tab.select();
                    break;
                case 1:
                    tab.setCustomView(right);
            }
        }).attach();
    }
}