package com.virtualrobe.virtualrobe.virtualrobe_app.Styling_board;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.virtualrobe.virtualrobe.virtualrobe_app.MainActivity;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.Styling_board.menu.DrawerAdapter;
import com.virtualrobe.virtualrobe.virtualrobe_app.Styling_board.menu.DrawerItem;
import com.virtualrobe.virtualrobe.virtualrobe_app.Styling_board.menu.Outfit_board_Fragment;
import com.virtualrobe.virtualrobe.virtualrobe_app.Styling_board.menu.SimpleItem;
import com.virtualrobe.virtualrobe.virtualrobe_app.Styling_board.menu.SpaceItem;
import com.virtualrobe.virtualrobe.virtualrobe_app.TypefaceSpan;
import com.virtualrobe.virtualrobe.virtualrobe_app.Utility.color_picker_dialog;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.wardrobe_model;
import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelySaveStateHandler;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;
import com.yarolegovich.slidingrootnav.SlidingRootNav;
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class Styling_activity extends AppCompatActivity implements DrawerAdapter.OnItemSelectedListener,color_picker_dialog.OnCompleteListener {
    private SlidingRootNav slidingRootNav;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private String[] screenTitles;
    private Drawable[] screenIcons;

    private static final int POS_DASHBOARD = 0;
    private static final int POS_ACCOUNT = 1;
    private static final int POS_MESSAGES = 2;
    private static final int POS_CART = 3;
    private static int POS_LOGOUT = 5;
    private FirebaseFirestore mFirestore;
    private Query mQuery;
    String[] list;
    private String UserId;
    private ArrayList<String> category;
    private ArrayList<String> category_id;
    private boolean first;

    private static final int ID_MULTI_CHOICE_DIALOG = R.id.btn_multi_choice_dialog;
    private LovelySaveStateHandler saveStateHandler;
    Bundle bundle;
    String selected_event;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.outfit_styling_activity);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        category = new ArrayList<String>();
        category_id = new ArrayList<String>();

        SpannableString s = new SpannableString("Style Outfit");
        s.setSpan(new TypefaceSpan(this, "LobsterTwo-Bold.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //Update the action bar title with the TypefaceSpan instance
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(s);
        }
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#efc04a")));
        }

        slidingRootNav = new SlidingRootNavBuilder(this)
                .withToolbarMenuToggle(toolbar)
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.menu_left_drawer)
                .inject();

        invalidateOptionsMenu();

        //set first instance
        first = true;

        getCategories(Styling_activity.this);
        bundle = savedInstanceState;
    }

    private void getCategories(final Styling_activity activity){
        mFirestore = FirebaseFirestore.getInstance();
        String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (email !=null) {
            mQuery = mFirestore.collection("Users")
                    .whereEqualTo("email",email)
                    .limit(1);
        }

        if (mQuery != null) {
            mQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    // Handle errors
                    if (e == null) {
                        final DocumentSnapshot snapshot = documentSnapshots.getDocuments().get(0);
                        // Get reference to the User
                        DocumentReference mUserRef = mFirestore.collection("Users").document(snapshot.getId());
                        UserId = snapshot.getId();

                        //get categories
                        Query query = mUserRef
                                .collection("Wardrobe")
                                .limit(50);
                        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                            @Override
                            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                                //set titles with category
                                for (int i=0; i<documentSnapshots.getDocuments().size(); i++) {
                                    wardrobe_model model = documentSnapshots.getDocuments().get(i).toObject(wardrobe_model.class);
                                    category.add(model.getImage_name());
                                    category_id.add(documentSnapshots.getDocuments().get(i).getId());
                                }
                                category.add("");
                                category.add("Close Styling Board");
                                list = category.toArray(new String[0]);
                                screenTitles = list;

                                if (screenTitles != null) {
                                    ArrayList<Drawable> drawables = new ArrayList<>();
                                    //get all icons except last and second to last
                                    for(int i=0; i<screenTitles.length-2; i++) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            drawables.add(getDrawable(R.drawable.ic_wadrobe_icon));
                                        }
                                        else {
                                            drawables.add(getResources().getDrawable(R.drawable.ic_wadrobe_icon));
                                        }
                                    }
                                    drawables.add(getResources().getDrawable(R.drawable.divider));
                                    drawables.add(getResources().getDrawable(R.drawable.ic_home));

                                    //add icons
                                    Drawable[] icons = drawables.toArray(new Drawable[0]);
                                    screenIcons = icons;

                                    List<DrawerItem> items;
                                    ArrayList<DrawerItem> list_item = new ArrayList<>();

                                    /*add first item and set as clicked*/
                                    list_item.add(createItemFor(0));

                                    /*add remaining items except from first and last*/
                                    for(int i=1; i<screenTitles.length-1; i++) {
                                        list_item.add(createItemFor(i));
                                    }

                                    /*add last item*/
                                    list_item.add( new SpaceItem(48).setChecked(true));
                                    list_item.add(createItemFor(screenTitles.length-1));
                                    POS_LOGOUT = screenTitles.length;

                                    //set adapter
                                    items = list_item;
                                    DrawerAdapter adapter = new DrawerAdapter(items);
                                    adapter.setListener(activity);

                                    //set recycleview
                                    RecyclerView list = findViewById(R.id.list);
                                    list.setNestedScrollingEnabled(false);
                                    list.setLayoutManager(new LinearLayoutManager(activity));
                                    list.setAdapter(adapter);

                                    //set user details
                                    TextView username = findViewById(R.id.username);
                                    String user_name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                                    if (user_name!=null) {
                                        username.setText(user_name);
                                    }

                                    //set Profile image
                                    CircleImageView profileimg = findViewById(R.id.profile_image);
                                    SharedPreferences sharedPreferences=getSharedPreferences("virtualrobe",MODE_PRIVATE);
                                    String profilepic = sharedPreferences.getString("profile_image_url", "");
                                    if(!profilepic.equals("")) {
                                        Glide.with(profileimg.getContext())
                                                .load(profilepic)
                                                .placeholder(R.drawable.user)
                                                .error(R.drawable.user)
                                                .into(profileimg);
                                    }


                                    adapter.setSelected(POS_DASHBOARD);
                                }
                            }
                        });
                    }
                }
            });
        }

    }

    private DrawerItem createItemFor(int position) {
        return new SimpleItem(screenIcons[position], screenTitles[position])
                .withIconTint(color(R.color.colorPrimary))
                .withTextTint(color(R.color.colorPrimaryDark))
                .withSelectedIconTint(color(R.color.colorAccent))
                .withSelectedTextTint(color(R.color.colorAccent));
    }

    @ColorInt
    private int color(@ColorRes int res) {
        return ContextCompat.getColor(this, res);
    }

    @Override
    public void onItemSelected(int position) {
        if (position == POS_LOGOUT) {
            finish();
        }
        else {
            try {
                if (position == POS_DASHBOARD){
                    int no_of_clicks = 0;
                    if (first){
                        first = false;
                        no_of_clicks = 1;
                    }
                    if (no_of_clicks==0){
                        String category_name = category.get(position);
                        String category_Id = category_id.get(position);
                        String user_id = UserId;
                        Intent intent = new Intent(Styling_activity.this, Cloth_selection_activity.class);
                        intent.putExtra("Category", category_name);
                        intent.putExtra("Category_ID", category_Id);
                        intent.putExtra("User_ID", user_id);
                        startActivity(intent);
                    }
                }
                else {
                        String category_name = category.get(position);
                        String category_Id = category_id.get(position);
                        String user_id = UserId;
                        Intent intent = new Intent(Styling_activity.this, Cloth_selection_activity.class);
                        intent.putExtra("Category", category_name);
                        intent.putExtra("Category_ID", category_Id);
                        intent.putExtra("User_ID", user_id);
                        startActivity(intent);
                }
            }
            catch (Exception e){

            }
        }
        slidingRootNav.closeMenu();
        Fragment fragment = new Outfit_board_Fragment();
        showFragment(fragment);
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.outfit_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                AskWhereTo();
                return true;

            case R.id.action_add:
                slidingRootNav.openMenu();
                return true;

            case R.id.remove:
                return true;

            case R.id.background:
                android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                color_picker_dialog overlay = new color_picker_dialog();
                overlay.show(fm,"background_picker");
                return true;

            case R.id.touch_mode:
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /*show when help or outfit assistant is clicked*/
    private void AskWhereTo(){
        String[] items = getResources().getStringArray(R.array.events);
        new LovelyChoiceDialog(this, R.style.CheckBoxTintTheme)
                .setTopColorRes(R.color.list_background_pressed)
                .setTitle(R.string.where_are_you_going)
                .setIcon(R.drawable.ic_outfit)
                .setItemsMultiChoice(items, (positions, items1) -> {
                            Toast.makeText(Styling_activity.this,
                                    getString(R.string.you_ordered, TextUtils.join("\n", items1)),
                                    Toast.LENGTH_SHORT)
                                    .show();
                            selected_event = items1.toString();
                            startActivity(new Intent(getApplicationContext(), com.virtualrobe.virtualrobe.virtualrobe_app.Profile_layout.Calendar.class));

                })
                .setConfirmButtonText(R.string.confirm)
                .setSavedInstanceState(bundle)
                .show();
    }

    /* *
    * Called when invalidateOptionsMenu() is triggered
    */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
        boolean drawerOpen = slidingRootNav.isMenuOpened();
        menu.findItem(R.id.background).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onComplete(int color) {
        Fragment fragment = new Outfit_board_Fragment();
        showFragment(fragment);
        Toast.makeText(Styling_activity.this,"Background has been changed successfully",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences preferences = getSharedPreferences("Cloth_details", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        finish();
    }
}
