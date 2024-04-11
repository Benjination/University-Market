package com.example.universitymarket.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.universitymarket.R;

public class FilterFragment extends Fragment {

    private LayoutInflater inflater;
    private ViewGroup container;
    private final FragmentManager fm;

    public FilterFragment(FragmentManager fm) {
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
        return inflater.inflate(R.layout.fragment_filter, container, false);
    }

    /*
        // Configure submenus
        SubMenu stars, genres, prices, expiring;
        if(Policy.max_stars != 0)
            stars = menu.addSubMenu(R.id.dash_toolbar_filter, R.id.dash_toolbar_stars, Menu.NONE, "Stars");
        genres = menu.addSubMenu(R.id.dash_toolbar_filter, R.id.dash_toolbar_genres, Menu.NONE, "Genres");
        prices = menu.addSubMenu(R.id.dash_toolbar_filter, R.id.dash_toolbar_prices, Menu.NONE, "Prices");
        expiring = menu.addSubMenu(R.id.dash_toolbar_filter, R.id.dash_toolbar_expiring, Menu.NONE, "Expiring");
        for(int i = Policy.max_stars; i > 0; i--) {
            String title = "";
            for(int z = i; z > 0; z--)
                title = title + "★";
            for(int z = Policy.max_stars - i; z > 0; z--)
                title = title + "☆";
            stars.add(title);
        }
        for(String s : Policy.genres)
            genres.add(s);
        for(String s : Policy.prices)
            prices.add(s);
        for(String s : Policy.expiring)
            expiring.add(s);
         */
}