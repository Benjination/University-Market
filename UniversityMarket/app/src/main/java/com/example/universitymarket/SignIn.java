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

import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.objects.User;
import com.example.universitymarket.utilities.Network;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Date;

public class SignIn extends AppCompatActivity
{

    private static final String TAG = "Signin";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        FirebaseAuth mAuth;
        // ...
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.isEmailVerified())
        {
            Log.d(TAG, "User is signed in");
            Intent intent = new Intent(SignIn.this, DashboardActivity.class);
            startActivity(intent);
            currentUser.reload();
        }
        else
        {
            Log.d(TAG, "User is not signed in");

        }


        EditText emailBox = findViewById(R.id.email);
        EditText passwordBox = findViewById(R.id.password);
        // needs code to change entered password to asterisks

        TextView invalid = findViewById(R.id.invalidEmail);
        invalid.setVisibility(View.INVISIBLE);


        //Buttons
        Button gotit = findViewById(R.id.got_it);
        gotit.setVisibility(View.INVISIBLE);
        Button login = findViewById(R.id.login);
        Button back = findViewById(R.id.back);

        //Button functionality


        back.setOnClickListener(v ->
        {
            mAuth.signOut();
            Intent intent = new Intent(SignIn.this, Login.class);
            startActivity(intent);
        });

        gotit.setOnClickListener(v ->
        {
            invalid.setVisibility(View.INVISIBLE);
            gotit.setVisibility(View.INVISIBLE);
            emailBox.setVisibility(View.VISIBLE);
            passwordBox.setVisibility(View.VISIBLE);
            login.setVisibility(View.VISIBLE);
            emailBox.setText("");
            passwordBox.setText("");
        });

        login.setOnClickListener(v ->
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            //emailBox.setVisibility(View.INVISIBLE);
            //passwordBox.setVisibility(View.INVISIBLE);
            //login.setVisibility(View.INVISIBLE);
            String domain1 = "mavs.uta.edu", domain2 = "uta.edu";
            String email = emailBox.getText().toString();
            String password = passwordBox.getText().toString();
            String domain = email.substring(email.indexOf("@") + 1);

            if ((domain.equals(domain2) || domain.equals(domain1)) && !password.equals(""))
            {
                // Check if the user already exists in Firebase Authentication
                mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful()) {
                            // User exists, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userEmail = firebaseUser.getEmail();
                                String Password = firebaseUser.getDisplayName();
                                ActiveUser.email = userEmail;
                                System.out.println(Password + " " + userEmail);
                                // Update UI with user information
                                Toast.makeText(SignIn.this, "Sign in successful.",
                                        Toast.LENGTH_SHORT).show();

                                if(firebaseUser.isEmailVerified()) {
                                    Intent intent = new Intent(SignIn.this, DashboardActivity.class);
                                    startActivity(intent);
                                }
                                else
                                {
                                    Toast.makeText(SignIn.this, "Please Verify your email and try again.",
                                            Toast.LENGTH_SHORT).show();
                                }
                                Intent intent = new Intent(SignIn.this, SignIn.class);
                                startActivity(intent);
                            }
                        }
                        else
                        {
                            // User does not exist or authentication failed
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignIn.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            Toast.makeText(SignIn.this, "If you have not created an account, please sign up.",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });
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