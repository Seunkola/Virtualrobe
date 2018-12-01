package com.virtualrobe.virtualrobe.virtualrobe_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;



public class splash extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SharedPreferences sharedPreferences;
        super.onCreate(savedInstanceState);
        //check if looged in
        sharedPreferences=getSharedPreferences("virtualrobe",MODE_PRIVATE);
        if (sharedPreferences.contains("username") && sharedPreferences.contains("status")){
            startActivity(new Intent(this,MainActivity.class));
            finish();
        }
        else {
            Intent intent = new Intent(this, welcome.class);
            startActivity(intent);
            finish();
        }
    }
}

