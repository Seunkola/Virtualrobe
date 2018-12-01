package com.virtualrobe.virtualrobe.virtualrobe_app.Styling_board;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.Styling_board.Adapter.Selection_Adapter;
import com.virtualrobe.virtualrobe.virtualrobe_app.TypefaceSpan;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Clothes;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Cloth_selection_activity extends AppCompatActivity implements Selection_Adapter.OnFeedSelectedListener {
    public static final String KEY_CATEGORY_ID = "Category_ID";
    public static final String KEY_CATEGORY_NAME = "Category";
    public static final String KEY_USER_ID = "User_ID";

    @BindView(R.id.selected)
    TextView selected;

    @BindView(R.id.recycler_wardrobe)
    RecyclerView recyclerView;

    @BindView(R.id.view_empty)
    LinearLayout mEmptyView;

    private FirebaseFirestore mFirestore;
    private DocumentReference mCategoryRef;
    private ListenerRegistration mCategoryRegistration;
    private Selection_Adapter adapter;
    String Category_ID;
    String User_ID;
    private String Category_name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clothe_selection);
        ButterKnife.bind(this);

        /*get Category name*/
        Category_name = getIntent().getExtras().getString(KEY_CATEGORY_NAME);
        if (Category_name == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_CATEGORY_NAME);
        }
        else {
            /*Set Category name and font type*/
            SpannableString s = new SpannableString("Select "+Category_name);
            s.setSpan(new TypefaceSpan(this, "LobsterTwo-Bold.ttf"), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            selected.setText(s);
        }

        /*get Category ID*/
        Category_ID = getIntent().getExtras().getString(KEY_CATEGORY_ID);
        if (Category_ID == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_CATEGORY_ID);
        }

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        //get refrence to the user
        User_ID = getIntent().getExtras().getString(KEY_USER_ID);
        if (User_ID == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_USER_ID);
        }
        DocumentReference user = mFirestore.collection("Users").document(User_ID);

        // Get reference to the category
        mCategoryRef = user.collection("Wardrobe").document(Category_ID);

        // Get category items
        Query items_Query = mCategoryRef
                .collection("Category_item");

        // RecyclerView
        adapter = new Selection_Adapter(items_Query,this){
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        };

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onfeedSelected(DocumentSnapshot cloth) {
        SharedPreferences preferences = getSharedPreferences("Cloth_details", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        final Clothes clothes = cloth.toObject(Clothes.class);
        if (cloth!=null){
            SpannableString s = new SpannableString("1 "+Category_name+ " selected");
            s.setSpan(new TypefaceSpan(this, "LobsterTwo-Bold.ttf"), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            selected.setText(s);

            /*send image to styling board*/
            editor.putString(Category_name+"_url",clothes.getFull_image());
            editor.putString(Category_name+"_id",cloth.getId());
            editor.apply();
        }
        else {
            SpannableString s = new SpannableString("Select "+Category_name);
            s.setSpan(new TypefaceSpan(this, "LobsterTwo-Bold.ttf"), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            selected.setText(s);

            /*remove image to styling board*/
            editor.putString(Category_name+"url","");
            editor.apply();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @OnClick(R.id.fab)
    public void close(View view){
        startActivity(new Intent(Cloth_selection_activity.this,Styling_activity.class));
        finish();
    }
}
