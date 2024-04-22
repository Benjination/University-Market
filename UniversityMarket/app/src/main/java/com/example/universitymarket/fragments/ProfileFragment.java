package com.example.universitymarket.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.universitymarket.R;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private View root;
    private TextView created;
    private Button saveButton;
    private EditText description;
    private User user;
    private RatingBar ratingBar;
    private TaskCompletionSource<String> load;
    private final Bundle dashMessage = new Bundle();
    private final FragmentManager fm;
    private final String userEmail;

    public ProfileFragment(FragmentManager fm, String userEmail) {
        this.userEmail = userEmail;
        this.fm = fm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        configure(root);
        return root;
    }

    private void configure(View v) {
        created = v.findViewById(R.id.profile_created_text);
        description = v.findViewById(R.id.profile_description_field);
        saveButton = v.findViewById(R.id.profile_save_button);
        ratingBar = v.findViewById(R.id.profile_rating_bar);

        saveButton.setOnClickListener(this);

        if(ActiveUser.email.equals(userEmail)) {
            user = ActiveUser.toPOJO();
            dashMessage.putString("newSubtitle", user.getId());
            dashMessage.putString("callingFragment", ProfileFragment.class.getName());
            fm.setFragmentResult("updateSubtitle", dashMessage);
            callback();
        } else {
            ratingBar.setIsIndicator(false);
            saveButton.setVisibility(View.INVISIBLE);
            description.setEnabled(false);
            load = new TaskCompletionSource<>();
            loadPage(load.getTask());

            Network.getUser(userEmail, new Callback<User>() {
                @Override
                public void onSuccess(User result) {
                    user = result;
                    callback();
                    load.setResult("getUser");

                    ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                        Network.getUser(userEmail, new Callback<User>() {
                            @Override
                            public void onSuccess(User current) {
                                current.setRatings((ArrayList<HashMap>) Stream.concat(current.getRatings().stream().filter(cur -> !cur.containsKey(ActiveUser.email)), Stream.of(new HashMap<>(Collections.singletonMap(ActiveUser.email, ratingBar.getRating())))).collect(Collectors.toList()));
                            }

                            @Override
                            public void onFailure(Exception error) {
                                Log.e("getUser", error.getMessage());
                            }
                        });
                    });
                }

                @Override
                public void onFailure(Exception error) {
                    Log.e("getUser", error.getMessage());
                }
            });
        }
    }

    private void callback() {
        String createdText = "Joined on " + Data.formatDate(Data.parseDate(user.getDateCreated()),"MMM dd, yyyy");
        created.setText(createdText);

        saveButton.setEnabled(true);
        ratingBar.setRating((float) user.getRatingsMap().values().stream().map(val -> (Float) val).collect(Collectors.toList()).stream().mapToDouble(Float::doubleValue).average().orElse(3.0));
        description.setText(user.getDescription());
    }

    private void loadPage(Task<String> task) {
        dashMessage.putBoolean("isLoading", true);
        fm.setFragmentResult("setLoading", dashMessage);

        task.addOnCompleteListener(res -> {
            String val = res.getResult();
            dashMessage.putBoolean("isLoading", false);
            fm.setFragmentResult("setLoading", dashMessage);
        });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.profile_save_button) {
            user.setDescription(description.getText().toString());

            Network.setUser(user, false, new Callback<User>() {
                @Override
                public void onSuccess(User result) {
                    Toast.makeText(
                            getContext(),
                            "Profile description updated",
                            Toast.LENGTH_SHORT
                    ).show();
                }

                @Override
                public void onFailure(Exception error) {
                    Toast.makeText(
                            getContext(),
                            "Profile description could not be updated",
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        }
    }
}