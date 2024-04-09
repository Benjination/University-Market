package com.example.universitymarket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.universitymarket.fragments.WatchFragment;
import com.example.universitymarket.fragments.HomepageFragment;
import com.example.universitymarket.fragments.ProfileFragment;
import com.example.universitymarket.fragments.RecordsFragment;
import com.example.universitymarket.fragments.TestFragment;
import com.example.universitymarket.fragments.viewPostFragment;
import com.example.universitymarket.globals.Policy;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.objects.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.HashMap;

public class DashboardActivity extends AppCompatActivity {

    private String currentView, currentTab = "Market";
    private View loadScreen;
    private ProgressBar loadAnimation;
    private Toolbar toolbar;
    private Menu menu;
    private TaskCompletionSource<Uri> uriRetrieval = new TaskCompletionSource<>();
    private final HashMap<String, Pair<Fragment, Boolean>> fragMap = new HashMap<>();
    private final FragmentManager fm = getSupportFragmentManager();
    private final Bundle fragResponse = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        updateUserInformation();
        configureFragmentManager();
        configureViews();
    }

    @SuppressLint("DiscouragedApi")
    private void updateUserInformation() {
        if(Data.isAnyObjectNull(Data.activeUserToPOJO().getAbout().values())) {
            Data.setActiveUser(
                    this,
                    new User(Data.getCachedToPOJO(this, "ActiveUser"))
            );
        }

        Network.getUser(this, ActiveUser.email, new Callback<User>() {
            @Override
            public void onSuccess(User result) {
                Data.setActiveUser(DashboardActivity.this, result);
            }

            @Override
            public void onFailure(Exception error) {
                Toast.makeText(
                        DashboardActivity.this,
                        error.getMessage(),
                        Toast.LENGTH_SHORT
                ).show();
                Log.e("getUser", error.getMessage());
            }
        });
    }

    private void configureFragmentManager() {
        Intent photoAlbum = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        photoAlbum.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");

        fragMap.put("Records", new Pair<>(new RecordsFragment(), false));
        fragMap.put("Watchlist", new Pair<>(new WatchFragment(), false));
        fragMap.put("Home", new Pair<>(new HomepageFragment(), false));
        fragMap.put("Test", new Pair<>(new TestFragment(), false));
        fragMap.put("Profile", new Pair<>(new ProfileFragment(), false));

        fm
                .setFragmentResultListener(
                        "requestGallery",
                        this,
                        (requestKey, result) -> {
                            uriRetrieval = new TaskCompletionSource<>();
                            galleryLauncher.launch(photoAlbum);

                            uriRetrieval.getTask()
                                    .addOnSuccessListener(task -> Data.refineImage(this, task)
                                            .addOnSuccessListener(res -> {
                                                fragResponse.putString("uri", res.toString());
                                                fm.setFragmentResult("retrieveImage", fragResponse);
                                            })
                                            .addOnFailureListener(error -> {
                                                fragResponse.putString("uri", "failure~" + error.getMessage());
                                                fm.setFragmentResult("retrieveImage", fragResponse);
                                            }))
                                    .addOnFailureListener(error -> {
                                        fragResponse.putString("uri", "failure~" + error.getMessage());
                                        fm.setFragmentResult("retrieveImage", fragResponse);
                                    });
                        });
        fm
                .setFragmentResultListener(
                        "setLoading",
                        this,
                        (requestKey, result) -> {
                            Pair<Fragment, Boolean> frag_pair = fragMap.get(currentView);
                            if(frag_pair == null) {
                                Log.e("setLoading", currentView + " is not a valid fragment");
                                return;
                            }

                            Fragment frag = frag_pair.first;
                            boolean isLoading = result.getBoolean("isLoading");
                            fragMap.replace(currentView, frag_pair, new Pair<>(frag, isLoading));
                            displayLoading(isLoading);
                        }
                );
        fm
                .setFragmentResultListener(
                        "homeTab",
                        this,
                        (requestKey, result) -> {
                            currentTab = result.getString("currentTab");
                            toolbar.setTitle(getResources().getIdentifier(
                                    "dash_toolbar_" + currentTab.toLowerCase() + "_txt",
                                    "string",
                                    getPackageName()
                                    )
                            );
                        }
                );
        fm
                .setFragmentResultListener(
                        "createPopup",
                        this,
                        (requestKey, result) ->
                                createPopup(result.getString("popupTitle"), result.getString("popupFragment"), result.getString("popupArgument"))
                );
    }

    private void configureViews() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.hide();
        BottomNavigationView dash_buttons = findViewById(R.id.dash_buttons);
        toolbar = findViewById(R.id.dash_toolbar);
        menu = toolbar.getMenu();
        loadScreen = findViewById(R.id.load_screen);
        loadAnimation = findViewById(R.id.load_animation);

        TypedValue actionBarTV = new TypedValue();
        getTheme().resolveAttribute(R.attr.actionBarSize, actionBarTV, true);
        int actionBarSize = Data.convertComplexToPixel(this, actionBarTV.data);

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

        dash_buttons.setOnItemSelectedListener(item -> {
            String name = item.toString();
            ViewGroup.LayoutParams params = toolbar.getLayoutParams();
            Pair<Fragment, Boolean> frag_pair = fragMap.get(name);
            if(frag_pair == null) {
                Log.e("configureActionBar", name + " is not a valid fragment");
                return false;
            }

            // get and tint the gear icon (app doesn't auto format color)
            Drawable gear = AppCompatResources.getDrawable(this, R.drawable.gear_icon);
            if(gear != null) {
                TypedValue colorOnPrimary = new TypedValue();
                getTheme().resolveAttribute(R.attr.colorOnPrimary, colorOnPrimary, true);
                gear.setColorFilter(colorOnPrimary.data, PorterDuff.Mode.SRC_IN);
                menu.findItem(R.id.dash_toolbar_settings).setIcon(gear);
            }

            menu.setGroupVisible(R.id.dash_toolbar_toggle, false);
            menu.findItem(R.id.dash_toolbar_settings).setVisible(false);
            if(name.equals("Home")) {
                toolbar.setTitle(getResources().getIdentifier(
                                "dash_toolbar_" + currentTab.toLowerCase() + "_txt",
                                "string",
                                getPackageName()
                        )
                );
                toolbar.setSubtitle("");
                params.height = actionBarSize;
                menu.setGroupVisible(R.id.dash_toolbar_filter, true);
            } else {
                if(name.equals("Records") || name.equals("Watchlist"))
                    menu.setGroupVisible(R.id.dash_toolbar_toggle, false);
                menu.setGroupVisible(R.id.dash_toolbar_filter, false);
                params.height = actionBarSize * 2;
                if(name.equals("Profile")) {
                    menu.findItem(R.id.dash_toolbar_settings).setVisible(true);
                    toolbar.setTitle(
                            ActiveUser.first_name + " " +
                            ActiveUser.last_name
                    );
                    toolbar.setSubtitle(ActiveUser.email);
                } else {
                    toolbar.setTitle(name);
                    toolbar.setSubtitle("");
                }
            }
            toolbar.setLayoutParams(params);

            currentView = name;
            Fragment frag = frag_pair.first;
            boolean isLoading = frag_pair.second;

            fm
                    .beginTransaction()
                    .replace(R.id.dash_fragment_buffer, frag)
                    .commit();

            ViewGroup root = findViewById(R.id.dash_popup_buffer);
            root.removeAllViews();
            displayLoading(isLoading);
            return true;
        });
        dash_buttons.setSelectedItemId(R.id.dash_home_button);
    }

    private void createPopup(String title, String fragName, String argument) {
        ViewGroup root = findViewById(R.id.dash_popup_buffer);
        View popupView = getLayoutInflater().inflate(R.layout.layout_popup, root);
        Toolbar popupToolbar = popupView.findViewById(R.id.popup_toolbar);
        popupToolbar.setTitle(title);
        popupToolbar.setNavigationOnClickListener((view) -> {
            root.removeAllViews();
        });
        Fragment popupFragment;

        if(fragName.equals("viewPostFragment")) {
            popupFragment = new viewPostFragment(argument);
        } else {
            popupFragment = new Fragment();
        }
        fm
                .beginTransaction()
                .replace(R.id.popup_fragment_buffer, popupFragment)
                .commit();
    }

    private void displayLoading(boolean isLoading) {
        if(isLoading) {
            loadScreen.setVisibility(View.VISIBLE);
            loadAnimation.setVisibility(View.VISIBLE);
        } else {
            loadScreen.setVisibility(View.INVISIBLE);
            loadAnimation.setVisibility(View.INVISIBLE);
        }
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == Activity.RESULT_OK) {
            Intent response = result.getData();
            if(response == null) {
                uriRetrieval.setException(new Exception("No image was selected"));
                return;
            }
            uriRetrieval.setResult(response.getData());
        } else {
            uriRetrieval.setException(new Exception("No image was selected"));
        }
    });

    private void setMargins (View view, int left, int top, int right, int bottom) {
        if (view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            view.requestLayout();
        }
    }
}