package com.example.universitymarket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Login extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);




        EditText emailBox = findViewById(R.id.email);
        EditText passwordBox = findViewById(R.id.password);
        TextView invalid = findViewById(R.id.invalidEmail);
        invalid.setVisibility(View.INVISIBLE);
        TextView emailStudent = findViewById(R.id.emailSent);
        emailStudent.setVisibility(View.INVISIBLE);
        TextView emailMod = findViewById(R.id.emailSentMod);
        emailMod.setVisibility(View.INVISIBLE);


        //Buttons
        Button gotit = findViewById(R.id.got_it);
        gotit.setVisibility(View.INVISIBLE);
        Button login = findViewById(R.id.login);
        Button backToMain = findViewById(R.id.back);

        //Button functionality

        backToMain.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, MainActivity.class);
            startActivity(intent);
        });

        gotit.setOnClickListener(v -> {
            emailStudent.setVisibility(View.INVISIBLE);
            invalid.setVisibility(View.INVISIBLE);
            gotit.setVisibility(View.INVISIBLE);
            emailBox.setVisibility(View.VISIBLE);
            passwordBox.setVisibility(View.VISIBLE);
            login.setVisibility(View.VISIBLE);
            emailMod.setVisibility(View.INVISIBLE);
        });

        login.setOnClickListener(v -> {
            emailBox.setVisibility(View.INVISIBLE);
            passwordBox.setVisibility(View.INVISIBLE);
            login.setVisibility(View.INVISIBLE);
            String domain1 = "mavs.uta.edu", domain2 = "uta.edu";
            String email = emailBox.getText().toString();
            String password = passwordBox.getText().toString();
            String domain = email.substring(email.indexOf("@") + 1);
            if(domain.equals(domain1) && !password.equals(""))
            {
                emailBox.setVisibility(View.INVISIBLE);
                passwordBox.setVisibility(View.INVISIBLE);
                login.setVisibility(View.INVISIBLE);
                emailStudent.setVisibility(View.VISIBLE);
                gotit.setVisibility(View.VISIBLE);
                System.out.println("User is a student.");
            }
            else if(domain.equals(domain2))
            {
                emailBox.setVisibility(View.INVISIBLE);
                passwordBox.setVisibility(View.INVISIBLE);
                login.setVisibility(View.INVISIBLE);
                emailMod.setVisibility(View.VISIBLE);
                gotit.setVisibility(View.VISIBLE);
                System.out.println("User is a faculty/staff.");
            }
            else
            {
                invalid.setVisibility(View.VISIBLE);
                gotit.setVisibility(View.VISIBLE);
                System.out.println("Email not accepted");
            }
        });


    }

}