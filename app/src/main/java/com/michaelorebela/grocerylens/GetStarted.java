package com.michaelorebela.grocerylens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class GetStarted extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_get_started);

        Button getStartedBtn = findViewById(R.id.get_started_button);
        getStartedBtn.setOnClickListener(view ->
                startActivity(new Intent(this, LoginPage.class)));





    }
}
