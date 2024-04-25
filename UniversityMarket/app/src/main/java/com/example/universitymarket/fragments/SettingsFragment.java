package com.example.universitymarket.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.universitymarket.Login;
import com.example.universitymarket.R;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private View root;
    private final String[] args;
    private final FragmentManager fm;

    public SettingsFragment(String[] args, FragmentManager fm) {
        this.args = args;
        this.fm = fm;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_settings, container, false);
        configureButtons(root);

        return root;
    }

    private void configureButtons(View view) {
        Button signoutButton = view.findViewById(R.id.settings_signout_button);
        signoutButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent btn_i;
        if(v.getId() == R.id.settings_signout_button) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            btn_i = new Intent(getContext(), Login.class);
            startActivity(btn_i);
        }
    }
}