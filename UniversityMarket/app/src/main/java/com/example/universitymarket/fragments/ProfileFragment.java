package com.example.universitymarket.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.example.universitymarket.R;
import com.example.universitymarket.globals.actives.ActiveUser;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private View root;
    private TextView created;
    private EditText description;
    private final FragmentManager fm;

    public ProfileFragment(FragmentManager fm) {
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

        SimpleDateFormat parser = new SimpleDateFormat("EEE MMM d HH:mm:ss zzz yyyy", Locale.US);
        Date parsed = null;
        try {
            parsed = parser.parse(ActiveUser.date_created);
            if(parsed != null) {
                String createdText = "Joined on " + new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(parsed);
                created.setText(createdText);
            }
        } catch(ParseException e) {
            Log.e("configure", e.getMessage());
        }
    }
}