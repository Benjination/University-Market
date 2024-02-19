package com.example.universitymarket.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.universitymarket.fragments.EventsFragment;
import com.example.universitymarket.fragments.MarketFragment;
import com.example.universitymarket.fragments.PostFragment;

public class HomepageAdapter extends FragmentStateAdapter {

    public HomepageAdapter(@NonNull FragmentActivity fragment) {
        super(fragment);
    }

    @NonNull @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0:
                return new PostFragment();
            default :
                return new MarketFragment();
        }
    }

    @Override public int getItemCount() { return 2; }
}
