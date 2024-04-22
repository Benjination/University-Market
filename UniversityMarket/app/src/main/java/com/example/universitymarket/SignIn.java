package com.example.universitymarket;
import com.example.universitymarket.utilities.*;

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
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Data;
import com.example.universitymarket.utilities.Network;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Date;

public class SignIn extends AppCompatActivity
{

    private static final String TAG = "Signin";
    String email, password;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        //ActionCodeSettings actionCodeSettings =
          //      ActionCodeSettings.newBuilder()
            //            // URL you want to redirect back to. The domain (www.example.com) for this
              //          // URL must be whitelisted in the Firebase Console.
                //        .setUrl("https://www.example.com/finishSignUp?cartId=1234")
                  //      // This must be true
                    //    .setHandleCodeInApp(true)
                      //  .setIOSBundleId("com.example.ios")
                        //.setAndroidPackageName(
                          //      "com.example.android",
                            //    true, /* installIfNotAvailable */
                              //  "12"    /* minimumVersion */)
                      //  .build();
        //Found error in Static Analysis. This code block was not being used for anything in the SignIn Class.

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
        Button resend = findViewById(R.id.resend);
        resend.setVisibility(View.INVISIBLE);
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
            email = emailBox.getText().toString();
            password = passwordBox.getText().toString();
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
                               // String userEmail = firebaseUser.getEmail();
                               // ActiveUser.email = userEmail;
                                //Replaced userEmail with ActiveUser.Email going forward
                                ActiveUser.email = firebaseUser.getEmail();

                                // Update UI with user information
                                Toast.makeText(SignIn.this, "Sign in successful.",
                                        Toast.LENGTH_SHORT).show();

                                if(firebaseUser.isEmailVerified()) {
                                    //Data.setActiveUser(SignIn.this, firebaseUser);
                                    Network.getUser(ActiveUser.email, new Callback<User>() {
                                        @Override
                                        public void onSuccess(User result) {
                                            Data.setActiveUser(SignIn.this, result);
                                            Intent intent = new Intent(SignIn.this, DashboardActivity.class);
                                            startActivity(intent);
                                        }

                                        @Override
                                        public void onFailure(Exception error) {
                                            Log.e("getUser", error.getMessage());
                                        }
                                    });

                                }
                                else
                                {
                                    Toast.makeText(SignIn.this, "Please Verify your email and try again.",
                                            Toast.LENGTH_SHORT).show();
                                    resend.setVisibility(View.VISIBLE);
                                }
                                //Intent intent = new Intent(SignIn.this, SignIn.class);
                                //startActivity(intent);
                                resend.setVisibility(View.VISIBLE);
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
                //invalid.setVisibility(View.VISIBLE);
                //gotit.setVisibility(View.VISIBLE);
                System.out.println("Email not accepted");
                    Toast.makeText(SignIn.this, "Invalid Email or Password",
                            Toast.LENGTH_LONG).show();
                    resend.setVisibility(View.VISIBLE);
            }

        });
        resend.setOnClickListener(v->{
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(SignIn.this, "Verification Email Sent",
                                            Toast.LENGTH_SHORT);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("sendEmailVerification", e.getMessage());
                                }
                            });
                        }
                    });
                });

    }

}