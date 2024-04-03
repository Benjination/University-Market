package com.example.universitymarket.adapters;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.universitymarket.R;
import com.example.universitymarket.fragments.MarketFragment;
import com.example.universitymarket.fragments.PostFragment;

public class HomepageAdapter extends FragmentStateAdapter {

    FragmentManager fm;

    public HomepageAdapter(@NonNull FragmentActivity fragment, @NonNull FragmentManager fm) {
        super(fragment);
        this.fm = fm;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0:
                return new MarketFragment(fm);
            default :
                return new PostFragment(fm);
        }
    }

    @Override
    public int getItemCount() { return 2; }
}
