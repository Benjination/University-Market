package com.example.universitymarket;

import android.annotation.SuppressLint;
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
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.universitymarket.fragments.ChatFragment;
import com.example.universitymarket.fragments.PopupFragment;
import com.example.universitymarket.fragments.SettingsFragment;
import com.example.universitymarket.fragments.TabFragment;
import com.example.universitymarket.globals.Policy;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.example.universitymarket.utilities.PickMultipleVisualMedia;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardActivity extends AppCompatActivity {

    private String currentView = "Market";
    private int currentTab = 0, actionBarSize, extBarSize;
    private View loadScreen;
    private ProgressBar loadAnimation;
    private Toolbar toolbar;
    private Menu menu;
    private MenuItem settings;
    private Drawable gear;
    private ViewGroup.LayoutParams params;
    private TaskCompletionSource<List<Uri>> urisRetrieval = new TaskCompletionSource<>();
    private TaskCompletionSource<Uri> uriRetrieval = new TaskCompletionSource<>();
    private PickMultipleVisualMedia multipleImagePicker = Policy.max_images_per_post > 1 ? new PickMultipleVisualMedia(Policy.max_images_per_post) : null;
    private final ActivityResultContracts.PickVisualMedia singleImagePicker = new ActivityResultContracts.PickVisualMedia();
    private final HashMap<String, ArrayList<Object>> fragMap = new HashMap<>();
    private final FragmentManager fm = getSupportFragmentManager();
    private final Bundle fragResponse = new Bundle();

    ArrayList<Object> toolbarTitles;
    ArrayList<Object> toolbarSubtitles;

    private enum tabGroup {
        Market,
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

        Network.getUser(ActiveUser.email, new Callback<User>() {
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
                        getResources().getString(R.string.dash_toolbar_browse_txt),
                        getResources().getString(R.string.dash_toolbar_filter_txt)
                )),
                new ArrayList<>(Arrays.asList(
                        getResources().getString(R.string.dash_toolbar_create_txt),
                        getResources().getString(R.string.dash_toolbar_view_txt)
                )),
                new ArrayList<>(Arrays.asList(
                        getResources().getString(R.string.dash_toolbar_watch_txt),
                        getResources().getString(R.string.dash_toolbar_bookkeep_txt)
                )),
                getResources().getString(R.string.dash_toolbar_chat_txt),
                ActiveUser.first_name + " " + ActiveUser.last_name
        ));

        toolbarSubtitles = new ArrayList<>(Arrays.asList(
                null,
                null,
                null,
                getResources().getString(R.string.load_txt),
                getResources().getString(R.string.load_txt)
        ));

        // These are the parameters for ArrayList<Object>  // Fragment  isLoading   miniToolbar List<PopupFragment>
        fragMap.put("Market", new ArrayList<>(Arrays.asList(new TabFragment(new String[]{ "Market" }, fm), false, true, new ArrayList<>())));
        fragMap.put("Post", new ArrayList<>(Arrays.asList(new TabFragment(new String[]{ "Post" }, fm), false, true, new ArrayList<>())));
        fragMap.put("Watch", new ArrayList<>(Arrays.asList(new TabFragment(new String[]{ "Watch" }, fm), false, true, new ArrayList<>())));
        fragMap.put("Chat", new ArrayList<>(Arrays.asList(new ChatFragment(), false, false, new ArrayList<>())));
        fragMap.put("Profile", new ArrayList<>(Arrays.asList(new TabFragment(new String[]{ "Profile", ActiveUser.email }, fm), false, false, new ArrayList<>())));

        fm
                .setFragmentResultListener(
                        "requestGallery",
                        this,
                        (requestKey, result) -> {
                            int newMax = Policy.max_images_per_post - result.getInt("numPictures");
                            fragResponse.clear();

                            if(newMax > 1) {
                                urisRetrieval = new TaskCompletionSource<>();
                                multipleImagePicker.updateMaxItems(newMax);
                                multipleGalleryLauncher.launch(
                                        new PickVisualMediaRequest.Builder()
                                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                                .build()
                                );
                            } else {
                                uriRetrieval = new TaskCompletionSource<>();
                                singleGalleryLauncher.launch(
                                        new PickVisualMediaRequest.Builder()
                                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                                .build()
                                );
                            }

                            urisRetrieval.getTask()
                                    .addOnSuccessListener(tasks -> Data.refineImages(this, tasks)
                                            .addOnSuccessListener(res -> {
                                                fragResponse.putStringArrayList("uris", new ArrayList<>(res.stream().map(Uri::toString).collect(Collectors.toList())));
                                                fm.setFragmentResult("retrieveImages", fragResponse);
                                            })
                                            .addOnFailureListener(error -> {
                                                fragResponse.putString("error", error.getMessage());
                                                fm.setFragmentResult("retrieveImages", fragResponse);
                                            })
                                    )
                                    .addOnFailureListener(error -> {
                                        fragResponse.putString("error", error.getMessage());
                                        fm.setFragmentResult("retrieveImages", fragResponse);
                                    });
                            uriRetrieval.getTask()
                                    .addOnSuccessListener(task -> Data.refineImage(this, task)
                                            .addOnSuccessListener(res -> {
                                                fragResponse.putString("uri", res.toString());
                                                fm.setFragmentResult("retrieveImage", fragResponse);
                                            })
                                            .addOnFailureListener(error -> {
                                                fragResponse.putString("error", error.getMessage());
                                                fm.setFragmentResult("retrieveImage", fragResponse);
                                            })
                                    )
                                    .addOnFailureListener(error -> {
                                        fragResponse.putString("error", error.getMessage());
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

                            Object title = toolbarTitles.get(tabGroup.valueOf(currentView).ordinal());
                            Object subtitle = toolbarSubtitles.get(tabGroup.valueOf(currentView).ordinal());
                            toolbar.setTitle(title.getClass() == ArrayList.class ? ((ArrayList<String>) title).get(currentTab) : (String) title);
                            toolbar.setSubtitle(subtitle == null ? "" : subtitle.getClass() == ArrayList.class ? ((ArrayList<String>) subtitle).get(currentTab) : (String) subtitle);
                        }
                );
        fm
                .setFragmentResultListener(
                        "createPopup",
                        this,
                        (requestKey, result) ->
                                createPopup(result.getString("popupTitle"), result.getString("popupSubtitle"), result.getString("popupFragment"), result.getStringArray("popupFragArgs"))
                );
        fm
                .setFragmentResultListener(
                        "updateSubtitle",
                        this,
                        (requestKey, result) -> {
                            String newSubtitle = result.getString("newSubtitle");
                            String[] callingFragmentExt = result.getString("callingFragment").split("\\.");
                            String callingFragment = callingFragmentExt[callingFragmentExt.length - 1];
                            if(fragMap.entrySet().stream().filter(string -> string.getValue().get(0).equals(callingFragment)).map(Map.Entry::getKey).findFirst().orElse(currentView).equals(currentView))
                                toolbar.setSubtitle(newSubtitle);
                            else
                                Log.d("updateSubtitle", "Call ignored, current view changed");
                        }
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
        actionBarSize = Data.convertComplexToPixel(this, actionBarTV.data);
        extBarSize = actionBarSize * 2;

        params = toolbar.getLayoutParams();

        // get and tint the gear icon (app doesn't auto format color)
        gear = AppCompatResources.getDrawable(this, R.drawable.gear_icon);
        if(gear != null) {
            TypedValue colorOnPrimary = new TypedValue();
            getTheme().resolveAttribute(R.attr.colorOnPrimary, colorOnPrimary, true);
            gear.setColorFilter(colorOnPrimary.data, PorterDuff.Mode.SRC_IN);
            menu.findItem(R.id.dash_toolbar_settings).setIcon(gear);
        }

        settings = menu.findItem(R.id.dash_toolbar_settings);
        settings.setOnMenuItemClickListener((menuItem) -> {
            createPopup("Settings", null, SettingsFragment.class.getName(), null);
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
            List<PopupFragment> popups = (List<PopupFragment>) objList.get(3);
            List<PopupFragment> otherPopups = fragMap.entrySet().stream().filter(e -> !e.getKey().equals(name)).map(val -> (List<PopupFragment>) val.getValue().get(3)).flatMap(List::stream).map(obj -> (PopupFragment) obj).collect(Collectors.toList());

            params.height = miniToolbar ? actionBarSize : extBarSize;
            toolbar.setLayoutParams(params);

            popups.removeIf(popup -> !popup.isAdded());
            otherPopups.forEach(popup -> fm.beginTransaction().hide(popup).commit());
            popups.forEach(popup -> fm.beginTransaction().show(popup).commit());

            settings.setVisible(name.equals("Profile"));

            currentView = name;

            Object title = toolbarTitles.get(tabGroup.valueOf(currentView).ordinal());
            Object subtitle = toolbarSubtitles.get(tabGroup.valueOf(currentView).ordinal());
            toolbar.setTitle(title.getClass() == ArrayList.class ? ((ArrayList<String>) title).get(currentTab) : (String) title);
            toolbar.setSubtitle(subtitle == null ? "" : subtitle.getClass() == ArrayList.class ? ((ArrayList<String>) subtitle).get(currentTab) : (String) subtitle);

            fm
                    .beginTransaction()
                    .replace(R.id.dash_fragment_buffer, frag)
                    .commit();

            displayLoading(isLoading);
            return true;
        });
        dash_buttons.setSelectedItemId(R.id.dash_market_button);
    }

    private void createPopup(String title, String subtitle, String fragName, String[] args) {
        Fragment popupDisplay;
        try {
            Class<? extends Fragment> clazz = (Class<? extends Fragment>) Class.forName(fragName);
            Class<?>[] types = new Class[]{ String[].class, FragmentManager.class };
            Constructor<? extends Fragment> cons = clazz.getConstructor(types);
            popupDisplay = cons.newInstance(args, fm);
        } catch(Exception e) {
            Log.e("createPopup", e.toString());
            return;
        }

        PopupFragment popupWindow = new PopupFragment(title, subtitle, popupDisplay);
        fm
                .beginTransaction()
                .add(R.id.dash_popup_buffer, popupWindow)
                .commit();
        ((List<PopupFragment>) fragMap.get(currentView).get(3)).add(popupWindow);
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

    private final ActivityResultLauncher<PickVisualMediaRequest> multipleGalleryLauncher =
            registerForActivityResult(multipleImagePicker, uris -> {
                if(uris.isEmpty()) {
                    urisRetrieval.setException(new Exception("No image was selected"));
                } else {
                    urisRetrieval.setResult(uris);
                }
            });

    private final ActivityResultLauncher<PickVisualMediaRequest> singleGalleryLauncher =
            registerForActivityResult(singleImagePicker, uri -> {
                if(uri == null) {
                    uriRetrieval.setException(new Exception("No image was selected"));
                } else {
                    uriRetrieval.setResult(uri);
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