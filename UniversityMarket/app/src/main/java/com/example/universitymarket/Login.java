package com.example.universitymarket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.universitymarket.objects.User;
import com.example.universitymarket.utilities.Network;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Date;

public class Login extends AppCompatActivity {

    private static final String TAG = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        FirebaseAuth mAuth;
        // ...
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }



        EditText emailBox = findViewById(R.id.email);
        EditText passwordBox = findViewById(R.id.password);
        // needs code to change entered password to asterisks

        TextView invalid = findViewById(R.id.invalidEmail);
        invalid.setVisibility(View.INVISIBLE);
        //TextView emailStudent = findViewById(R.id.emailSent);
        //emailStudent.setVisibility(View.INVISIBLE);
        //TextView emailMod = findViewById(R.id.emailSentMod);
        //emailMod.setVisibility(View.INVISIBLE);


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
            //emailStudent.setVisibility(View.INVISIBLE);
            invalid.setVisibility(View.INVISIBLE);
            gotit.setVisibility(View.INVISIBLE);
            emailBox.setVisibility(View.VISIBLE);
            passwordBox.setVisibility(View.VISIBLE);
            login.setVisibility(View.VISIBLE);
            //emailMod.setVisibility(View.INVISIBLE);
            emailBox.setText("");
            passwordBox.setText("");
        });

        login.setOnClickListener(v -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
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
                //emailStudent.setVisibility(View.VISIBLE);
                gotit.setVisibility(View.VISIBLE);
                System.out.println("User is a student.");

                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                //updateUI(user);
                                User currUser = new User(email);
                                currUser.setId(email);
                                Date date = new Date();
                                currUser.setAbout(date.toString(),null, null, null, email, email);
                                Network.setUser(Login.this, currUser, false, null);
                                Intent intent = new Intent(Login.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(Login.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                Toast.makeText(Login.this, "If you have already created an account, Try signing in instead",
                                        Toast.LENGTH_LONG).show();

                                //updateUI(null);
                            }
                        }
                    });
            }
            else if(domain.equals(domain2) && !password.equals(""))
            {
                emailBox.setVisibility(View.INVISIBLE);
                passwordBox.setVisibility(View.INVISIBLE);
                login.setVisibility(View.INVISIBLE);
                //emailMod.setVisibility(View.VISIBLE);
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