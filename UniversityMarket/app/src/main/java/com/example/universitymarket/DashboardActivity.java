package com.example.universitymarket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.universitymarket.fragments.ChatFragment;
import com.example.universitymarket.fragments.SettingsFragment;
import com.example.universitymarket.fragments.TabFragment;
import com.example.universitymarket.fragments.viewPostFragment;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.objects.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DashboardActivity extends AppCompatActivity {

    private String currentView, currentTabGroup = "Home";
    private int currentTab = 0;
    private View loadScreen;
    private ProgressBar loadAnimation;
    private Toolbar toolbar;
    private Menu menu;
    private MenuItem settings;
    private MenuItem search;
    private Drawable gear;
    private ViewGroup.LayoutParams params;
    private TaskCompletionSource<Uri> uriRetrieval = new TaskCompletionSource<>();
    private final HashMap<String, ArrayList<Object>> fragMap = new HashMap<>();
    private final FragmentManager fm = getSupportFragmentManager();
    private final Bundle fragResponse = new Bundle();

    ArrayList<Object> toolbarTitles;
    ArrayList<Object> toolbarSubtitles;

    private enum tabGroup {
        Home,
        Post,
        Watch,
        Chat,
        Profile
    }

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

        toolbarTitles = new ArrayList<>(Arrays.asList(
                new ArrayList<>(Arrays.asList(
                        getResources().getString(R.string.dash_toolbar_market_txt),
                        getResources().getString(R.string.dash_toolbar_filter_txt)
                )),
                new ArrayList<>(Arrays.asList(
                        getResources().getString(R.string.dash_toolbar_compose_txt),
                        getResources().getString(R.string.dash_toolbar_created_txt)
                )),
                new ArrayList<>(Arrays.asList(
                        getResources().getString(R.string.dash_toolbar_watchlist_txt),
                        getResources().getString(R.string.dash_toolbar_analytics_txt)
                )),
                getResources().getString(R.string.dash_toolbar_messages_txt),
                ActiveUser.first_name + " " + ActiveUser.last_name
        ));

        toolbarSubtitles = new ArrayList<>(Arrays.asList(
                null,
                null,
                null,
                null,
                ActiveUser.email
        ));

        // These are the parameters for ArrayList<Object>  // Fragment  isLoading   miniToolbar
        fragMap.put("Home", new ArrayList<>(Arrays.asList(new TabFragment("Home"), false, true)));
        fragMap.put("Post", new ArrayList<>(Arrays.asList(new TabFragment("Post"), false, true)));
        fragMap.put("Watch", new ArrayList<>(Arrays.asList(new TabFragment("Watch"), false, true)));
        fragMap.put("Chat", new ArrayList<>(Arrays.asList(new ChatFragment(), false, false)));
        fragMap.put("Profile", new ArrayList<>(Arrays.asList(new TabFragment("Profile"), false, false)));

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
                            ArrayList<Object> objList = fragMap.get(currentView);
                            if(objList == null) {
                                Log.e("setLoading", currentView + " is not a valid fragment");
                                return;
                            }

                            boolean isLoading = result.getBoolean("isLoading");
                            objList.set(1, isLoading);
                            fragMap.replace(currentView, fragMap.get(currentView), objList);
                            displayLoading(isLoading);
                        }
                );
        fm
                .setFragmentResultListener(
                        "tabSwitch",
                        this,
                        (requestKey, result) -> {
                            currentTab = result.getInt("currentTab");
                            currentTabGroup = result.getString("currentTabGroup");

                            Object title = toolbarTitles.get(tabGroup.valueOf(currentTabGroup).ordinal());
                            Object subtitle = toolbarSubtitles.get(tabGroup.valueOf(currentTabGroup).ordinal());
                            toolbar.setTitle(title.getClass() == ArrayList.class ? ((ArrayList<String>) title).get(currentTab) : (String) title);
                            toolbar.setSubtitle(subtitle == null ? "" : subtitle.getClass() == ArrayList.class ? ((ArrayList<String>) subtitle).get(currentTab) : (String) subtitle);
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

        params = toolbar.getLayoutParams();

        // get and tint the gear icon (app doesn't auto format color)
        gear = AppCompatResources.getDrawable(this, R.drawable.gear_icon);
        if(gear != null) {
            TypedValue colorOnPrimary = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorOnPrimary, colorOnPrimary, true);
            gear.setColorFilter(colorOnPrimary.data, PorterDuff.Mode.SRC_IN);
            menu.findItem(R.id.dash_toolbar_settings).setIcon(gear);
        }

        search = menu.findItem(R.id.dash_toolbar_search);
        settings = menu.findItem(R.id.dash_toolbar_settings);
        settings.setOnMenuItemClickListener((menuItem) -> {
            createPopup("Settings", "SettingsFragment", null);
            return false;
        });

        dash_buttons.setOnItemSelectedListener(item -> {
            String name = item.toString();

            ArrayList<Object> objList = fragMap.get(name);
            if(objList == null) {
                Log.e("configureActionBar", name + " is not a valid fragment");
                return false;
            }

            Fragment frag = (Fragment) objList.get(0);
            boolean isLoading = (boolean) objList.get(1);
            boolean miniToolbar = (boolean) objList.get(2);

            params.height = miniToolbar ? actionBarSize : actionBarSize * 2;
            toolbar.setLayoutParams(params);

            if(name.equals("Chat")) {
                currentTabGroup = "Chat";
            } else {
                if(name.equals("Home")) {
                    search.setVisible(true);
                } else {
                    settings.setVisible(name.equals("Profile"));
                    search.setVisible(false);
                }
            }

            Object title = toolbarTitles.get(tabGroup.valueOf(currentTabGroup).ordinal());
            Object subtitle = toolbarSubtitles.get(tabGroup.valueOf(currentTabGroup).ordinal());
            toolbar.setTitle(title.getClass() == ArrayList.class ? ((ArrayList<String>) title).get(currentTab) : (String) title);
            toolbar.setSubtitle(subtitle == null ? "" : subtitle.getClass() == ArrayList.class ? ((ArrayList<String>) subtitle).get(currentTab) : (String) subtitle);

            currentView = name;
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
        popupToolbar.setNavigationOnClickListener((view) -> root.removeAllViews());
        Fragment popupFragment;

        if(fragName.equals("viewPostFragment")) {
            popupFragment = new viewPostFragment(argument);
        } else {
            popupFragment = new SettingsFragment();
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