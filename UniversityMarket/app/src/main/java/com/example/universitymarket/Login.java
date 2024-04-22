package com.example.universitymarket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telecom.Call;
import android.util.Log;
import android.util.StatsLog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.universitymarket.globals.actives.ActiveUser;
import com.example.universitymarket.models.User;
import com.example.universitymarket.utilities.Callback;
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

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;



public class Login extends AppCompatActivity
{

    private static final String TAG = "Login";
    String cache;
    String delim;




    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionCodeSettings actionCodeSettings =
                ActionCodeSettings.newBuilder()
                        // URL you want to redirect back to. The domain (www.example.com) for this
                        // URL must be whitelisted in the Firebase Console.
                        .setUrl("https://www.example.com/finishSignUp?cartId=1234")
                        // This must be true
                        .setHandleCodeInApp(true)
                        .setIOSBundleId("com.example.ios")
                        .setAndroidPackageName(
                                "com.example.android",
                                true, /* installIfNotAvailable */
                                "12"    /* minimumVersion */)
                        .build();



        FirebaseAuth mAuth;
        // ...
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null && currentUser.isEmailVerified())
        {

            currentUser.reload();
            ActiveUser.email = currentUser.getEmail();
            Network.getUser(currentUser.getEmail(), new Callback<User>() {
                @Override
                public void onSuccess(User result) {
                    Data.setActiveUser(Login.this, result);
                    Intent intent = new Intent(Login.this, DashboardActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onFailure(Exception error) {
                    Log.e("getUser", error.getMessage());
                }
            });
        }




        EditText emailBox = findViewById(R.id.email);
        EditText passwordBox = findViewById(R.id.password);
        EditText passwordBox2 = findViewById(R.id.password2);
        EditText fname = findViewById(R.id.fname);
        EditText lname = findViewById(R.id.lname);
        // needs code to change entered password to asterisks

        TextView invalid = findViewById(R.id.invalidEmail);
        invalid.setVisibility(View.INVISIBLE);



        //Buttons
        Button gotit = findViewById(R.id.got_it);
        gotit.setVisibility(View.INVISIBLE);
        Button login = findViewById(R.id.login);
        Button signIn = findViewById(R.id.signin);

        //Button functionality

        signIn.setOnClickListener(v ->
        {
            Intent intent = new Intent(Login.this, SignIn.class);
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
            String password2 = passwordBox2.getText().toString();
            String first = fname.getText().toString();
            String last = lname.getText().toString();
            String domain = email.substring(email.indexOf("@") + 1);

            if ((domain.equals(domain2) || domain.equals(domain1)) && !password.equals(""))
            {
                //emailBox.setVisibility(View.INVISIBLE);
                //passwordBox.setVisibility(View.INVISIBLE);
                //login.setVisibility(View.INVISIBLE);
                //gotit.setVisibility(View.VISIBLE);
                System.out.println("User is a student.");

                if (password.equals(password2)) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(Login.this, "Verification Email Sent",
                                                        Toast.LENGTH_SHORT);
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(Exception e) {
                                                Log.e("sendEmailVerification", e.getMessage());
                                            }
                                        });
                                        //updateUI(user);


                                        User currUser = new User(email);
                                        currUser.setId(email);
                                        Date date = new Date();
                                        currUser.setAbout(date.toString(), null, last, null, first, email);
                                        Data.setActiveUser(Login.this, currUser);
                                        Network.setUser(currUser, false, new Callback<User>() {
                                            @Override
                                            public void onSuccess(User result) {

                                                Toast.makeText(Login.this, "Account Created.",
                                                        Toast.LENGTH_SHORT).show();
                                                ////////////
                                                FirebaseAuth auth = FirebaseAuth.getInstance();
                                                auth.sendSignInLinkToEmail(email, actionCodeSettings)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Log.d(TAG, "Email sent.");
                                                                    FirebaseUser currentUser = auth.getCurrentUser();
                                                                    /*
                                                                    if (currentUser != null && currentUser.isEmailVerified()) {
                                                                        Intent intent = new Intent(Login.this, SignIn.class);
                                                                        startActivity(intent);
                                                                    } else {
                                                                        Intent intent = new Intent(Login.this, SignIn.class);
                                                                        startActivity(intent);
                                                                    }
                                                                    Found this error with Static Analysis. If/Else Executed identical code.
                                                                    */
                                                                    Intent intent = new Intent(Login.this, SignIn.class);
                                                                    startActivity(intent);

                                                                } else {
                                                                    Intent intent = new Intent(Login.this, SignIn.class);
                                                                    startActivity(intent);
                                                                }
                                                            }
                                                        });
                                                ////////////

                                                        }

                                                        @Override
                                                        public void onFailure(Exception error) {
                                                            Log.e("setUser", error.getMessage());
                                                        }
                                                    });

                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.e(TAG, "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(Login.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                        Toast.makeText(Login.this, "If you have already created an account, Try signing in instead",
                                                Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(Login.this, Login.class);
                                        startActivity(intent);
                                    }

                                }
                            });

                }

            }
             else
            {
                //invalid.setVisibility(View.VISIBLE);
                //gotit.setVisibility(View.VISIBLE);
                //emailBox.setVisibility(View.INVISIBLE);
                //passwordBox.setVisibility(View.INVISIBLE);
                //login.setVisibility(View.INVISIBLE);
                System.out.println("Email not accepted");
                Toast.makeText(Login.this, "Invalid Email or Password",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
