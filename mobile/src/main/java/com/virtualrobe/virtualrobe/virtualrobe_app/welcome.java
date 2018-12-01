package com.virtualrobe.virtualrobe.virtualrobe_app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.virtualrobe.virtualrobe.virtualrobe_app.help.helpActivity;

public class welcome extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.welcome_page);

        Button button;
        button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent next = new Intent(getApplicationContext(), helpActivity.class);
                next.putExtra("welcome","welcome");
                startActivity(next);
                finish();
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });
        initTypeface();

    }

    private void initTypeface() {
        Typeface lobster = Typeface.createFromAsset(getAssets(),"fonts/LobsterTwo-Bold.ttf");
        TextView text2 = (TextView)findViewById(R.id.textView3);
        text2.setTypeface(lobster);
    }
}
