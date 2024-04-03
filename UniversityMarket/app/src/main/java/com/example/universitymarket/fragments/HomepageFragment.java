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
import com.example.universitymarket.adapters.HomepageAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HomepageFragment extends Fragment {

    private View root;
    private LayoutInflater inflater;
    private ViewGroup container;
    private FragmentManager fm;
    private final Bundle dashMessage = new Bundle();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.inflater = inflater;
        this.container = container;
        root = inflater.inflate(R.layout.fragment_homepage, container, false);
        configure(root);
        return root;
    }

    private void configure(View view) {
        fm = getParentFragmentManager();

        TabLayout tabs = view.findViewById(R.id.home_tabs);
        ViewPager2 pager = view.findViewById(R.id.home_pager);

        TypedValue colorPrimary = new TypedValue();
        TypedValue colorOnTertiary = new TypedValue();
        requireActivity().getTheme().resolveAttribute(R.attr.colorPrimary, colorPrimary, true);
        requireActivity().getTheme().resolveAttribute(R.attr.colorOnTertiary, colorOnTertiary, true);

        View market = inflater.inflate(R.layout.layout_homepage_tab, container, false);
        ((ImageView) market.findViewById(R.id.tab_icon)).setImageResource(R.drawable.market_icon);
        ((TextView) market.findViewById(R.id.tab_text)).setText(R.string.home_market_tab_txt);

        View post = inflater.inflate(R.layout.layout_homepage_tab, container, false);
        ((ImageView) post.findViewById(R.id.tab_icon)).setImageResource(R.drawable.post_icon);
        ((TextView) post.findViewById(R.id.tab_text)).setText(R.string.home_post_tab_txt);

        HomepageAdapter adapter = new HomepageAdapter(requireActivity(), getParentFragmentManager());
        pager.setAdapter(adapter);
        pager.setUserInputEnabled(true);

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View custView = tab.getCustomView();
                if(custView == null)
                    return;

                ((ImageView) custView.findViewById(R.id.tab_icon)).setColorFilter(colorPrimary.data);
                ((TextView) custView.findViewById(R.id.tab_text)).setTextColor(colorPrimary.data);
                dashMessage.putString("currentTab", (String) ((TextView) custView.findViewById(R.id.tab_text)).getText());
                fm.setFragmentResult("homeTab", dashMessage);
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
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        new TabLayoutMediator(tabs, pager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setCustomView(market);
                    tab.select();
                    break;
                case 1:
                    tab.setCustomView(post);
            }
        }).attach();
    }
}