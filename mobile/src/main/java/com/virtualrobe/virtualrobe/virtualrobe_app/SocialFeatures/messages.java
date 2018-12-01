package com.virtualrobe.virtualrobe.virtualrobe_app.SocialFeatures;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.virtualrobe.virtualrobe.virtualrobe_app.MainActivity;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;

import java.io.FileInputStream;
import java.util.ArrayList;

public class messages extends AppCompatActivity{

    DrawerLayout drawer;
    String temp="";
    private ArrayList<String> labels = null;
    ListView listView;
    RelativeLayout notificationCount1;
    TextView notification_no;
    Button message;
    String no="";

    //cognito
    CognitoCachingCredentialsProvider credentialsProvider;
    private AsyncTask<String, Void, Void> getnotifications;
    private ArrayList<String> text_notify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.message_main);


    }

    //method to get stored file in memory
    public void retrieve (String fileName) {
        FileInputStream fos;
        int c;
        try {
            fos = openFileInput(fileName);
            while( (c = fos.read()) != -1){
                temp = temp + Character.toString((char)c);
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item1 = menu.findItem(R.id.action_favorite);
        MenuItemCompat.setActionView(item1, R.layout.notification_update_count_layout);
        notificationCount1 = (RelativeLayout) MenuItemCompat.getActionView(item1);
        notification_no = (TextView) MenuItemCompat.getActionView(item1).findViewById(R.id.badge_notification_1);
        message = (Button)MenuItemCompat.getActionView(item1).findViewById(R.id.button1);
        retrieve_no("message");
        notification_no.setText(no);
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent messa = new Intent(getApplicationContext(),messages.class);
                messa.putExtra("user",temp);
                startActivity(messa);
                finish();
            }
        });
        return super.onCreateOptionsMenu(menu);
    }


    public void retrieve_no (String fileName) {
        FileInputStream fos;
        int c;
        try {
            fos = openFileInput(fileName);
            while( (c = fos.read()) != -1){
                no = no + Character.toString((char)c);
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(messages.this,MainActivity.class));
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            return true;
        }
    }
}
