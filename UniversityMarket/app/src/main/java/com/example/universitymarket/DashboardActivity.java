package com.example.universitymarket;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.universitymarket.fragments.WatchFragment;
import com.example.universitymarket.fragments.HomepageFragment;
import com.example.universitymarket.fragments.ProfileFragment;
import com.example.universitymarket.fragments.RecordsFragment;
import com.example.universitymarket.fragments.TestFragment;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.utilities.Data;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;

public class DashboardActivity extends AppCompatActivity {

    private TaskCompletionSource<Uri> uriRetrieval = new TaskCompletionSource<>();
    private HashMap<String, Fragment> fragMap = new HashMap<>();
    private FragmentManager fm = getSupportFragmentManager();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        configureFragmentManager();
        configureActionBar();
    }

    private void configureFragmentManager() {
        Intent photoAlbum = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        photoAlbum.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");

        fragMap.put("Records", new RecordsFragment());
        fragMap.put("Watch", new WatchFragment());
        fragMap.put("Home", new HomepageFragment());
        fragMap.put("Test", new TestFragment());
        fragMap.put("Profile", new ProfileFragment());

        fm
                .setFragmentResultListener(
                        "requestGallery",
                        this,
                        (requestKey, result) -> {
                            uriRetrieval = new TaskCompletionSource<>();
                            galleryLauncher.launch(photoAlbum);
                            Bundle response = new Bundle();

                            uriRetrieval.getTask()
                                    .addOnSuccessListener(task -> {
                                        Data.refineImage(this, task)
                                                .addOnSuccessListener(res -> {
                                                    response.putString(null, res.toString());
                                                    fm.setFragmentResult("retrieveImage", response);
                                                })
                                                .addOnFailureListener(error -> {
                                                    response.putString(null, "uriRetreival~" + error.getMessage());
                                                    fm.setFragmentResult("retrieveImage", response);
                                                });
                                    })
                                    .addOnFailureListener(error -> {
                                        response.putString(null, "uriRetreival~" + error.getMessage());
                                        fm.setFragmentResult("retrieveImage", response);
                                    });
                        });
    }

    private void configureActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
            actionBar.hide();

        BottomNavigationView dash_buttons = findViewById(R.id.dash_buttons);

        dash_buttons.setOnItemSelectedListener(item -> {
            String name = item.toString();
            Fragment frag = fragMap.get(name);
            if(frag == null)
                return false;

            fm
                    .beginTransaction()
                    .replace(R.id.dash_fragment_buffer, frag)
                    .commit();

            return true;
        });
        dash_buttons.setSelectedItemId(R.id.dash_home_button);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if(result.getResultCode() == Activity.RESULT_OK) {
            Intent response = result.getData();
            if(response == null) {
                Toast.makeText(
                        this,
                        "Photo retrieval failed",
                        Toast.LENGTH_SHORT
                ).show();
                uriRetrieval.setException(new Exception("No image was selected"));
                return;
            }
            uriRetrieval.setResult(response.getData());
        } else {
            uriRetrieval.setException(new Exception("No image was selected"));
        }
    });
}