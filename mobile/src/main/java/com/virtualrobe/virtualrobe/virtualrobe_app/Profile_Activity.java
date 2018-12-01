package com.virtualrobe.virtualrobe.virtualrobe_app;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import com.google.firebase.firestore.EventListener;
import com.medialablk.easygifview.EasyGifView;
import com.virtualrobe.virtualrobe.virtualrobe_app.Profile_layout.wardrobe;
import com.virtualrobe.virtualrobe.virtualrobe_app.Registration.Sign_up_Activity;
import com.virtualrobe.virtualrobe.virtualrobe_app.SocialFeatures.CategoryDialogFragment;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.MainActivityViewModel;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.user_model;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.wardrobe_model;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

public class Profile_Activity extends AppCompatActivity
        implements  NavigationView.OnNavigationItemSelectedListener,FABProgressListener,BottomNavigation.OnMenuItemSelectionListener,CategoryDialogFragment.CategoryListener {

    private static final int LIMIT = 1;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.fab)
    FloatingActionButton image_fab;

    @BindView(R.id.profile_image)
    CircleImageView profile_image;

    @BindView(R.id.user)
    TextView user_name;

    @BindView(R.id.no_of_friends)
    TextView no_of_friends;

    @BindView(R.id.edit)
    TextView edit;

    @BindView(R.id.flContent)
    FrameLayout content;

    @BindView(R.id.BottomNavigation)
    BottomNavigation bottomNavigation;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    List<user_model> user;
    private Fragment fragment;

    private MainActivityViewModel mViewModel;
    private static final int RC_SIGN_IN = 9001;
    private String profilepic = "";
    DocumentSnapshot snapshot;
    private String User_database_status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.app_base_layout);
        ButterKnife.bind(this);

        // View model
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        /*get user databse status to know if a users information is in the databse*/
        User_database_status = getIntent().getStringExtra("Status");

        initFirestore();
        initView(savedInstanceState);
        setNavheader();

        /*update user account details*/
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(),Sign_up_Activity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (User_database_status==null|| User_database_status.isEmpty()) {
           /*Do nothing*/
        }
        else if(User_database_status.equalsIgnoreCase("Success")) {
            /*Welcome user*/
            welcome_dialog();
        }
        else if (User_database_status.equalsIgnoreCase("Failure")){
            /*Try again to save user details*/
            register_details_dialog();
        }
    }

    private void display_default(Bundle savedInstanceState) {
        fragment = null;
        fragment = new wardrobe();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.flContent, fragment,"wardrobe").commit();
        }
    }

    private void initFirestore() {
        mFirestore = FirebaseFirestore.getInstance();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email !=null) {
            mQuery = mFirestore.collection("Users")
                    .whereEqualTo("email",email)
                    .limit(LIMIT);
        }
    }

    private void initView(final Bundle savedInstanceState) {
        if (mQuery != null) {
            mQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    // Handle errors
                    if (e == null) {
                        user = documentSnapshots.toObjects(user_model.class);

                        //update UI
                        if (user.size()!=0) {
                            profilepic = user.get(0).getProfilePic();
                            Glide.with(profile_image.getContext())
                                    .load(profilepic)
                                    .error(R.drawable.default_profile)
                                    .into(profile_image);

                            user_name.setText(user.get(0).getUsername());

                            /*save image url and user name in sharedpreference*/
                            SharedPreferences sharedPreferences = getSharedPreferences("virtualrobe", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("profile_image_url", profilepic);
                            editor.putString("username", user.get(0).getUsername());
                            editor.apply();


                            /*send user id to fragments*/
                            snapshot = documentSnapshots.getDocuments().get(0);
                            display_default(savedInstanceState);
                        }
                        else {
                            startActivity(new Intent(getApplicationContext(), Sign_up_Activity.class));
                            finish();
                        }

                    }
                    else {
                        startActivity(new Intent(getApplicationContext(), Sign_up_Activity.class));
                        finish();
                    }

                }
            });
        }

        /*toolbar*/
        setSupportActionBar(toolbar);

        /*Navigation Drawer*/
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        /*Navigation View*/
        navigationView.setNavigationItemSelectedListener(this);

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

        /*Bottom navigation*/
        bottomNavigation.setOnMenuItemClickListener(this);
    }

    public String user_id() {
        return snapshot.getId();
    }

    private void setNavheader() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = LayoutInflater.from(this).inflate(R.layout.nav_header_main, null);
        navigationView.addHeaderView(header);

        String user_name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        if (user_name!=null){
            TextView username = (TextView)header.findViewById(R.id.user);
            username.setText(user_name);
        }

        SharedPreferences sharedPreferences=getSharedPreferences("virtualrobe",MODE_PRIVATE);
        profilepic = sharedPreferences.getString("profile_image_url","");
        if(!profilepic.equals("")) {
            final CircleImageView profileimg = (CircleImageView) header.findViewById(R.id.imageView);
            Glide.with(profileimg.getContext())
                    .load(profilepic)
                    .error(R.drawable.default_profile)
                    .into(profileimg);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onFABProgressAnimationEnd() {

    }

    @Override
    public void onMenuItemSelect(int i, int i1) {

    }

    @Override
    public void onMenuItemReselect(int i, int i1) {

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

    @Override
    public void onAdd(wardrobe_model wardrobe) {
        /*get user ID*/
        String user_id = user_id();
        DocumentReference mUserRef = mFirestore.collection("Users").document(user_id);

        //call add category method from wardrobe Fragment
        wardrobe fragment = (wardrobe) getSupportFragmentManager().findFragmentByTag("wardrobe");
        fragment.addCategory(mUserRef,wardrobe).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Hide keyboard and scroll to top
                hideKeyboard();
                Snackbar.make(content, "Category Added",
                        Snackbar.LENGTH_SHORT).show();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Show failure message and hide keyboard
                        hideKeyboard();
                        Snackbar.make(content, "Failed to add category",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            try {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            catch (Exception ex){
            }
        }
    }

    private void welcome_dialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(Profile_Activity.this);
        LayoutInflater inflater = LayoutInflater.from(Profile_Activity.this);
        View view = inflater.inflate(R.layout.profile_dialog,null);
        builder.setView(view);

        /*Assigning custom dialog button*/
        Button Ok = view.findViewById(R.id.ok_button);

        final AlertDialog dialog = builder.create();
        /*call on actions*/
        Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        /*Assign gif animation*/
        EasyGifView easyGifView = view.findViewById(R.id.easyGifView);
        easyGifView.setGifFromResource(R.drawable.boy);

        dialog.show();
    }

    private void register_details_dialog(){
        MainActivity activity = new MainActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(Profile_Activity.this);
        LayoutInflater inflater = LayoutInflater.from(Profile_Activity.this);
        View view = inflater.inflate(R.layout.welcome_dialog_pesonal_stylist,null);
        builder.setView(view);

        /*Assigning custom dialog button*/
        Button Ok = view.findViewById(R.id.ok_button);
        Button Cancel = view.findViewById(R.id.cancel_button);

        final AlertDialog dialog = builder.create();
        /*call on actions*/
        Ok.setOnClickListener(view1 -> {
            dialog.dismiss();
            if (activity.fullname!=null){
                if (!activity.fullname.isEmpty()){
                    Intent intent = new Intent(getApplicationContext(),Sign_up_Activity.class);
                    intent.putExtra("fullname",activity.fullname);
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
            if (!activity.fullname.isEmpty()){
                Intent intent = new Intent(getApplicationContext(),Sign_up_Activity.class);
                intent.putExtra("fullname",activity.fullname);
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

