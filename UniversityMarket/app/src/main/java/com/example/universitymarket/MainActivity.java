package com.example.universitymarket;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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