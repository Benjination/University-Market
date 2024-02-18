package com.example.universitymarket;

import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.universitymarket.fragments.CartFragment;
import com.example.universitymarket.fragments.HomepageFragment;
import com.example.universitymarket.fragments.ProfileFragment;
import com.example.universitymarket.fragments.RecordsFragment;
import com.example.universitymarket.fragments.TestFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    RecordsFragment records = new RecordsFragment();
    CartFragment cart = new CartFragment();
    HomepageFragment home = new HomepageFragment();
    TestFragment test = new TestFragment();
    ProfileFragment profile = new ProfileFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        configureActionBar();
    }

    private void configureActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        BottomNavigationView dash_buttons = findViewById(R.id.dash_buttons);
        getSupportFragmentManager().beginTransaction().replace(R.id.dash_fragment_buffer, home).commit();
        dash_buttons.setSelectedItemId(R.id.dash_home_button);

        dash_buttons.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.dash_records_button:
                    getSupportFragmentManager().beginTransaction().replace(R.id.dash_fragment_buffer, records).commit();
                    break;
                case R.id.dash_cart_button:
                    getSupportFragmentManager().beginTransaction().replace(R.id.dash_fragment_buffer, cart).commit();
                    break;
                case R.id.dash_home_button:
                    getSupportFragmentManager().beginTransaction().replace(R.id.dash_fragment_buffer, home).commit();
                    break;
                case R.id.dash_test_button:
                    getSupportFragmentManager().beginTransaction().replace(R.id.dash_fragment_buffer, test).commit();
                    break;
                case R.id.dash_profile_button:
                    getSupportFragmentManager().beginTransaction().replace(R.id.dash_fragment_buffer, profile).commit();
                    break;
                default:
                    return false;
            }
            return true;
        });
    }
}