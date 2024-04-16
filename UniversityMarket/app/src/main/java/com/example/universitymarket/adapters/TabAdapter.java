package com.example.universitymarket.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.universitymarket.fragments.AnalyticsFragment;
import com.example.universitymarket.fragments.FilterFragment;
import com.example.universitymarket.fragments.MarketFragment;
import com.example.universitymarket.fragments.PostFragment;
import com.example.universitymarket.fragments.ProfileFragment;
import com.example.universitymarket.fragments.WatchFragment;
import com.example.universitymarket.fragments.myPostFragment;
import com.example.universitymarket.globals.actives.ActiveUser;

public class TabAdapter extends FragmentStateAdapter {

    FragmentManager fm;
    String context;

    public TabAdapter(@NonNull FragmentActivity fragment, @NonNull FragmentManager fm, @NonNull String context) {
        super(fragment);
        this.fm = fm;
        this.context = context;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 0) {
            if(context.equals("Market")) {
                return new MarketFragment(fm);
            } else if(context.equals("Post")) {
                return new PostFragment(fm);
            } else if(context.equals("Watch")) {
                return new WatchFragment(fm);
            } else {
                return new ProfileFragment(fm, ActiveUser.email);
            }
        } else {
            if(context.equals("Market")) {
                return new FilterFragment(fm);
            } else if(context.equals("Post")) {
                return new myPostFragment(fm);
            } else if(context.equals("Watch")) {
                return new AnalyticsFragment(fm);
            } else {
                return new myPostFragment(fm);
            }
        }
    }

    @Override
    public int getItemCount() { return 2; }
}
