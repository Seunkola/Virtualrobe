package com.virtualrobe.virtualrobe.virtualrobe_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.medialablk.easygifview.EasyGifView;
import com.rahimlis.badgedtablayout.BadgedTabLayout;
import com.virtualrobe.virtualrobe.virtualrobe_app.Registration.Sign_up_Activity;
import com.virtualrobe.virtualrobe.virtualrobe_app.Styling_board.Styling_activity;
import com.virtualrobe.virtualrobe.virtualrobe_app.Utility.NetworkUtil;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.MainActivityViewModel;
import com.virtualrobe.virtualrobe.virtualrobe_app.newsfeed_layout.Mall_root;
import com.virtualrobe.virtualrobe.virtualrobe_app.newsfeed_layout.MyGallery;
import com.virtualrobe.virtualrobe.virtualrobe_app.newsfeed_layout.MyOutfits;
import com.virtualrobe.virtualrobe.virtualrobe_app.newsfeed_layout.favourites;
import com.virtualrobe.virtualrobe.virtualrobe_app.newsfeed_layout.feed;
import com.github.jorgecastilloprz.listeners.FABProgressListener;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,FABProgressListener,BottomNavigation.OnMenuItemSelectionListener {

    private static final String TAG = "MainActivity";

    private static final int RC_SIGN_IN = 9001;

    private static final int LIMIT = 50;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.flContent)
    FrameLayout frameLayout;

    @BindView(R.id.BottomNavigation)
    BottomNavigation bottomNavigation;

    @BindView(R.id.tabs)
    BadgedTabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    private int[] tabIcons = {
            R.drawable.ic_home_black_24dp,
            R.drawable.ic_shopping_cart_black_24dp,
            R.drawable.ic_favorite_black_24dp
    };

    public String fullname = "";
    int no_of_login;

    private MainActivityViewModel mViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        /*Subscribe to welcome message service*/
        FirebaseMessaging.getInstance().subscribeToTopic("newusers");

        // View model
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        /*toolbar*/
        setSupportActionBar(toolbar);

        /*Navigation Drawer*/
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        /*Navigation View*/
        navigationView.setNavigationItemSelectedListener(this);

        /*Top tablayout*/
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        /*Set App name and font type*/
        SpannableString s = new SpannableString("Virtualrobe");
        s.setSpan(new TypefaceSpan(this, "LobsterTwo-Bold.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        /*Update the action bar title with the TypefaceSpan instance*/
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(s);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#efc04a")));
        }

        /*Navigation header*/
        setNavheader();

        /*Bottom Navigation*/
        bottomNavigation.setOnMenuItemClickListener(this);

        /*get user fullname from registration activity*/
        fullname = getIntent().getStringExtra("Fullname");
    }

    private void setupTabIcons() {
        try {
                tabLayout.setIcon(0,tabIcons[0]);
                tabLayout.setIcon(1,tabIcons[1]);
                tabLayout.setIcon(2,tabIcons[2]);

                tabLayout.setBadgeText(1,"1");
        }
        catch (Exception e){
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new feed(), "Feed");
        adapter.addFragment(new Mall_root(), "Shopping Mall");
        adapter.addFragment(new favourites(), "Favourites");
        viewPager.setAdapter(adapter);
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<android.support.v4.app.Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(android.support.v4.app.FragmentManager manager) {
            super(manager);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(android.support.v4.app.Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        Fragment fragment = null;

        int id = item.getItemId();

        if (id == R.id.newsfeed) {
            displayView(0);

        } else if (id == R.id.outfits) {//go to profile
            displayView(1);

        } else if (id == R.id.friends) {
        } else if (id == R.id.calendar) {//go to user calendar

        } else if (id == R.id.support) {

        } else if (id == R.id.nav_send) {
        }
        else if (id == R.id.create_outfit){
            startActivity(new Intent(this, Styling_activity.class));
        }
        else if(id == R.id.invite){
        }
        // update selected item and title, then close the drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFABProgressAnimationEnd() {

    }

    @Override
    public void onMenuItemSelect(@IdRes int itemid, int position) {
        int tab =  bottomNavigation.getMenuItemId(position);

        if (tab == R.id.tab_home){
            displayView(0);
        }
        else if (tab == R.id.tab_profile){
            startActivity(new Intent(MainActivity.this,Profile_Activity.class));
        }
        else if (tab == R.id.tab_outfit){
            displayView(1);
        }
        else if (tab == R.id.tab_gallery){
            displayView(2);
        }

    }

    @Override
    public void onMenuItemReselect(@IdRes int itemid, int position) {
        int tab =  bottomNavigation.getMenuItemId(position);

        if (tab == R.id.tab_home){
            displayView(0);
        }
        else if (tab == R.id.tab_profile){
            startActivity(new Intent(MainActivity.this,Profile_Activity.class));
        }
        else if (tab == R.id.tab_outfit){
            displayView(1);
        }
        else if (tab == R.id.tab_message){
            displayView(2);
        }
    }

    private void displayView(int position) {
        // update the main content by replacing fragments
        android.support.v4.app.Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new feed();
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.flContent, fragment).addToBackStack("").commit();
                break;
            case 1:
                startActivity(new Intent(getApplicationContext(),MyOutfits.class));
                break;
            case 2:
                //fragment = new MyGallery();
                break;
            default:
                fragment = null;
                break;
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item1 = menu.findItem(R.id.action_favorite);
        item1.setActionView(R.layout.notification_update_count_layout);
        RelativeLayout notificationCount1 = (RelativeLayout) item1.getActionView();
        TextView notification_no = (TextView) item1.getActionView().findViewById(R.id.badge_notification_1);
        Button message = (Button) item1.getActionView().findViewById(R.id.button1);
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });



        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_favorite) {
            return true;
        } else if (id == R.id.action_logout) {
            AuthUI.getInstance().signOut(this);
            startSignIn();
            return true;
        } else if (id == R.id.action_search) {
            onSearchRequested();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            mViewModel.setIsSigningIn(false);

            if (resultCode != RESULT_OK && shouldStartSignIn()) {
                startSignIn();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn();
        }

        /*User just sign up*/
        if (fullname!=null) {
            if (!fullname.isEmpty()) {
                /*open dialog*/
                welcome_dialog();
            }
        }
    }

    private boolean shouldStartSignIn() {
        return (!mViewModel.getIsSigningIn() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    private void startSignIn() {
        //saving user login details on device
        SharedPreferences sharedPreferences = getSharedPreferences("virtualrobe", MODE_PRIVATE);
        SharedPreferences.Editor e = sharedPreferences.edit();
        e.putString("username", "");
        e.putInt("status", 1);
        e.apply();

        // Sign in with FirebaseUI
        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(Collections.singletonList(
                        new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
                .setIsSmartLockEnabled(false)
                .build();

        startActivityForResult(intent, RC_SIGN_IN);
        mViewModel.setIsSigningIn(true);
    }

    private void setNavheader() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = LayoutInflater.from(this).inflate(R.layout.nav_header_main, null);
        navigationView.addHeaderView(header);

        TextView username = (TextView) header.findViewById(R.id.user);
        final CircleImageView profileimg = (CircleImageView) header.findViewById(R.id.imageView);
        if (!shouldStartSignIn()) {
            String user_name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            if (user_name != null) {
                username.setText(user_name);
            }
        }
        else {
            username.setText("");
        }

        SharedPreferences sharedPreferences=getSharedPreferences("virtualrobe",MODE_PRIVATE);
        String profilepic = sharedPreferences.getString("profile_image_url", "");
        if(!profilepic.equals("")) {
            Glide.with(profileimg.getContext())
                    .load(profilepic)
                    .error(R.drawable.default_profile)
                    .into(profileimg);
        }

        /*Go to User Profile*/
        header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Profile_Activity.class));
            }
        });

        profileimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Profile_Activity.class));
            }
        });
    }

    public static class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {

            String status = NetworkUtil.getConnectivityStatusString(context);

            Toast.makeText(context, status, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
        }
        else {
            super.onBackPressed();
        }
    }

    private void welcome_dialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View view = inflater.inflate(R.layout.welcome_dialog_pesonal_stylist,null);
        builder.setView(view);

        /*Assigning custom dialog button*/
        Button Ok = view.findViewById(R.id.ok_button);
        Button Cancel = view.findViewById(R.id.cancel_button);

        final AlertDialog dialog = builder.create();
        /*call on actions*/
        Ok.setOnClickListener(view1 -> {
            dialog.dismiss();
            if (fullname!=null){
                if (!fullname.isEmpty()){
                    Intent intent = new Intent(getApplicationContext(),Sign_up_Activity.class);
                    intent.putExtra("fullname",fullname);
                    startActivity(intent);
                }
            }
            else {
                startActivity(new Intent(getApplicationContext(), Sign_up_Activity.class));
            }
        });

        Cancel.setOnClickListener(view12 -> {
            Snackbar.make(view12,"Tutorial session succesfully cancelled",Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            if (!fullname.isEmpty()){
                Intent intent = new Intent(getApplicationContext(),Sign_up_Activity.class);
                intent.putExtra("fullname",fullname);
                startActivity(intent);
            }
            else {
            startActivity(new Intent(getApplicationContext(), Sign_up_Activity.class));
            }
        });


        /*Assign gif animation*/
        EasyGifView easyGifView = view.findViewById(R.id.easyGifView);
        easyGifView.setGifFromResource(R.drawable.boy);

        dialog.show();
    }
}
