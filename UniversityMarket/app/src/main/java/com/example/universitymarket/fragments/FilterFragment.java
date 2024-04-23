package com.example.universitymarket.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.universitymarket.R;
import com.example.universitymarket.globals.Policy;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.utilities.SortByField;

public class FilterFragment extends Fragment {

    private View root;
    private LayoutInflater inflater;
    private ViewGroup container;
    private final FragmentManager fm;
    private RadioGroup genre_radio_filter;
    private RadioGroup price_radio_filter;
    private RadioGroup uploadDate_radio_filter;

    public static RadioButton selected_genre_filter = null;
    public static RadioButton selected_price_filter = null;
    public static RadioButton selected_uploadDate_filter = null;

    private Button clearFiltersBtn;

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
        root = inflater.inflate(R.layout.fragment_filter, container, false);

        // Initialize clear filters button
        clearFiltersBtn = root.findViewById(R.id.clear_filters_button);

        //initialize radio buttons and corresponding listeners
        genre_radio_filter = root.findViewById(R.id.genre_filter_group);
        for(String genre : Policy.genre_filters) {
            RadioButton newGenre = new RadioButton(requireContext());
            newGenre.setText(genre);
            genre_radio_filter.addView(newGenre);
        }
        genre_radio_filter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the ID of the selected radio button
                selected_genre_filter = root.findViewById(checkedId);
                if (selected_genre_filter != null) {
                    // Get the text of the selected radio button
                    String selectedGenre = selected_genre_filter.getText().toString();
                    Log.d("selected genre filter: ", selected_genre_filter.getText().toString());
                    Toast.makeText(requireContext(), "Selected Genre: " + selectedGenre, Toast.LENGTH_SHORT).show();
                }
            }
        });

        price_radio_filter = root.findViewById(R.id.price_filter_group);
        for(String priceFilter : Policy.prices) {
            RadioButton newPriceFilter = new RadioButton(requireContext());
            newPriceFilter.setText(priceFilter);
            price_radio_filter.addView(newPriceFilter);
        }
        price_radio_filter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the ID of the selected radio button
                selected_price_filter = root.findViewById(checkedId);
                if (selected_price_filter != null) {
                    // Get the text of the selected radio button
                    String selectedPrice = selected_price_filter.getText().toString();
                    Log.d("selected price filter: ", selected_price_filter.getText().toString());
                    Toast.makeText(requireContext(), "Selected price: " + selectedPrice, Toast.LENGTH_SHORT).show();
                }
            }
        });

        uploadDate_radio_filter = root.findViewById(R.id.uploadDate_filter_group);
        for(String uploadFilter : Policy.upload_date_filters) {
            RadioButton newUploadDateFilter = new RadioButton(requireContext());
            newUploadDateFilter.setText(uploadFilter);
            uploadDate_radio_filter.addView(newUploadDateFilter);
        }
        uploadDate_radio_filter.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the ID of the selected radio button
                selected_uploadDate_filter = root.findViewById(checkedId);
                if (uploadDate_radio_filter != null) {
                    // Get the text of the selected radio button
                    String selectedUploadDate = selected_uploadDate_filter.getText().toString();
                    Log.d("selected Upload Date filter: ", selectedUploadDate);
                    Toast.makeText(requireContext(), "Selected Upload Date: " + selectedUploadDate, Toast.LENGTH_SHORT).show();
                }
            }
        });

        //clear filters button listener
        clearFiltersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear selected genre filter
                if (FilterFragment.selected_genre_filter != null) {
                    FilterFragment.selected_genre_filter.setChecked(false);
                    FilterFragment.selected_genre_filter = null;
                }

                // Clear selected price filter
                if (FilterFragment.selected_price_filter != null) {
                    FilterFragment.selected_price_filter.setChecked(false);
                    FilterFragment.selected_price_filter = null;
                }

                // Clear selected upload date filter
                if (FilterFragment.selected_uploadDate_filter != null) {
                    FilterFragment.selected_uploadDate_filter.setChecked(false);
                    FilterFragment.selected_uploadDate_filter = null;
                }

                // Show toast or perform any other action as needed
                Toast.makeText(requireActivity(), "Filters cleared", Toast.LENGTH_SHORT).show();
            }
        });
        return root;
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