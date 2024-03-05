package com.example.universitymarket;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
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

        //Ben
        Button login = findViewById(R.id.login);
        login.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
        });
        //
    }


    @Override
    public void onClick(View v) {
        Intent btn_i;
        switch (v.getId()) {
            case R.id.main_sample_button0:
                btn_i = new Intent(this, SampleActivity.class);
                startActivity(btn_i);
                break;
            case R.id.main_sample_button1:
                btn_i = new Intent(this, DashboardActivity.class);
                startActivity(btn_i);
                break;
            default:
                Log.i("Console msg", "Non-event triggering press");
                break;
        }
    }
}