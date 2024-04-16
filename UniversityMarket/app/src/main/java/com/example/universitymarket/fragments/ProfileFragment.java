package com.example.universitymarket.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import com.example.universitymarket.R;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.objects.User;
import com.example.universitymarket.utilities.Callback;
import com.example.universitymarket.utilities.Network;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

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
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        configure(root);
        return root;
    }

    private void configure(View v) {
        created = v.findViewById(R.id.profile_created_text);
        description = v.findViewById(R.id.profile_description_field);
        saveButton = v.findViewById(R.id.profile_save_button);
        ratingBar = v.findViewById(R.id.profile_rating_bar);

        if(ActiveUser.email.equals(userEmail)) {
            user = ActiveUser.toPOJO();
            callback();
        } else {
            ratingBar.setIsIndicator(false);
            saveButton.setVisibility(View.INVISIBLE);
            description.setEnabled(false);
            load = new TaskCompletionSource<>();
            loadPage(load.getTask());
            Network.getUser(requireActivity(), userEmail, new Callback<User>() {
                @Override
                public void onSuccess(User result) {
                    user = result;
                    callback();
                    load.setResult("getUser");
                }

                @Override
                public void onFailure(Exception error) {
                    Log.e("getUser", error.getMessage());
                }
            });
        }
    }

    private void callback() {
        SimpleDateFormat parser = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy", Locale.US);
        try {
            Date parsed = parser.parse(user.getDateCreated());
            if(parsed != null) {
                String createdText = "Joined on " + new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(parsed);
                created.setText(createdText);
            }
        } catch(ParseException e) {
            Log.e("callback", e.getMessage());
        }

        saveButton.setEnabled(true);
        ratingBar.setRating(user.getRating());
        description.setText(user.getDescription());
        dashMessage.putString("newSubtitle", user.getId());
        dashMessage.putString("callingFragment", ProfileFragment.class.getName());
        fm.setFragmentResult("updateSubtitle", dashMessage);
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
}