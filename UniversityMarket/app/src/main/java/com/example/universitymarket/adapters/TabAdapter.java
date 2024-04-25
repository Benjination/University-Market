package com.example.universitymarket.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.universitymarket.fragments.FilterFragment;
import com.example.universitymarket.fragments.MarketFragment;
import com.example.universitymarket.fragments.PostFragment;
import com.example.universitymarket.fragments.ProfileFragment;
import com.example.universitymarket.fragments.RecordsFragment;
import com.example.universitymarket.fragments.WatchFragment;
import com.example.universitymarket.fragments.myPostFragment;
import com.example.universitymarket.fragments.myPostProfileFragment;

public class TabAdapter extends FragmentStateAdapter {

    FragmentManager fm;
    String[] args;

    public TabAdapter(@NonNull FragmentActivity fragment, @NonNull FragmentManager fm, @NonNull String[] args) {
        super(fragment);
        this.fm = fm;
        this.args = args;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if(position == 0) {
            if(args[0].equals("Market")) {
                return new MarketFragment(fm);
            } else if(args[0].equals("Post")) {
                return new PostFragment(fm);
            } else if(args[0].equals("Watch")) {
                return new WatchFragment(fm);
            } else {
                return new ProfileFragment(fm, args[1]);
            }
        } else {
            if(args[0].equals("Market")) {
                return new FilterFragment(fm);
            } else if(args[0].equals("Post")) {
                return new myPostFragment(fm);
            } else if(args[0].equals("Watch")) {
                return new RecordsFragment(fm);
            } else {
                return new myPostProfileFragment(fm, args[1]);
            }
        }
    }

    @Override
    public int getItemCount() { return 2; }
}
