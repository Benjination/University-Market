package com.example.universitymarket;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.android.volley.Response;
import com.google.common.collect.Sets;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.ktx.Firebase;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAccount;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.ICurrentAccountResult;
import com.microsoft.identity.client.IPublicClientApplication;
import com.microsoft.identity.client.ISingleAccountPublicClientApplication;
import com.microsoft.identity.client.PublicClientApplication;
import com.microsoft.identity.client.SignInParameters;
import com.microsoft.identity.client.exception.MsalException;
import com.microsoft.identity.common.java.commands.parameters.SilentTokenCommandParameters;

import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    //Initialize Firebase authentication
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configureButtons();
    }

    private void configureButtons() {
        Button sampleButton0 = findViewById(R.id.main_sample_button0);
        Button sampleButton1 = findViewById(R.id.main_sample_button1);

        sampleButton0.setOnClickListener(this);
        sampleButton1.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_sample_button0:
                Intent btn_i = new Intent(this, SampleActivity.class);
                startActivity(btn_i);
                break;
            case R.id.main_sample_button1:
                Log.i("Console msg", "Hello there, from console button!");
                break;
            default:
                Log.i("Console msg", "Non-event triggering press");
                break;
        }
    }
}