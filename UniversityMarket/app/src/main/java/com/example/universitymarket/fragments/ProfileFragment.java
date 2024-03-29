package com.example.universitymarket.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.universitymarket.Login;
import com.example.universitymarket.R;
import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.objects.User;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private View root;
    User user;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.fragment_profile, container, false);
        configureButtons(root);



        TextView name = root.findViewById(R.id.name);
        name.setText(ActiveUser.first_name + " " + ActiveUser.last_name);
        TextView email = root.findViewById(R.id.email);
        email.setText(ActiveUser.email);


        return root;
    }

    private void configureButtons(View view) {
        Button signoutButton = view.findViewById(R.id.profile_signout_button);
        signoutButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent btn_i;
        switch (v.getId()) {
            case R.id.profile_signout_button:
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                mAuth.signOut();
                btn_i = new Intent(getContext(), Login.class);
                startActivity(btn_i);
                break;
        }
    }
}