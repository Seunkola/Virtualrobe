package com.virtualrobe.virtualrobe.virtualrobe_app;

import android.app.Activity;
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
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload.UploadActivity;
import com.virtualrobe.virtualrobe.virtualrobe_app.View.View_Cloth;
import com.virtualrobe.virtualrobe.virtualrobe_app.adapter.ClotheAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class Clothes_Activity extends AppCompatActivity implements ClotheAdapter.OnFeedSelectedListener{
    public static final String KEY_CATEGORY_ID = "Category_ID";
    public static final String KEY_CATEGORY_NAME = "Category";
    public static final String KEY_USER_ID = "User_ID";

    @BindView(R.id.page_text)
    TextView page_Header;

    @BindView(R.id.text_current_search)
    TextView mCurrentSearchView;

    @BindView(R.id.text_current_sort_by)
    TextView mCurrentSortByView;

    @BindView(R.id.recycler_clothes)
    RecyclerView recyclerView;

    @BindView(R.id.view_empty)
    LinearLayout mEmptyView;

    private FirebaseFirestore mFirestore;
    private DocumentReference mCategoryRef;
    private ListenerRegistration mCategoryRegistration;
    private ClotheAdapter adapter;
    String Category_ID;
    String User_ID;
    private String Category_name;
    boolean isButtonClicked;
    GridLayoutManager layoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_clothes);
        ButterKnife.bind(this);

        /*get Category name*/
       Category_name = getIntent().getExtras().getString(KEY_CATEGORY_NAME);
        if (Category_name == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_CATEGORY_NAME);
        }
        else {
            /*Set Category name and font type*/
            SpannableString s = new SpannableString(Category_name);
            s.setSpan(new TypefaceSpan(this, "LobsterTwo-Bold.ttf"), 0, s.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            page_Header.setText(s);
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
        Query iteems_Query = mCategoryRef
                .collection("Category_item");

        // RecyclerView
        adapter = new ClotheAdapter(iteems_Query,this){
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

        layoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    @OnClick(R.id.fab)
    public void onfabClicked(View view) {
        Intent intent = new Intent(this, UploadActivity.class);
        intent.putExtra(UploadActivity.KEY_CATEGORY_NAME,Category_name);
        intent.putExtra(UploadActivity.KEY_CATEGORY_ID,Category_ID);
        intent.putExtra(UploadActivity.KEY_USER_ID,User_ID);
        startActivity(intent);
    }

    @OnClick(R.id.list_toggle)
    public void onListclicked(View view){
        isButtonClicked = !isButtonClicked;
        view.setBackgroundResource(isButtonClicked ? R.drawable.ic_list_black_24dp : R.drawable.ic_grid_on_black_24dp);
        if (layoutManager.getSpanCount()==3){
            layoutManager.setSpanCount(1);
        }
        else {
            layoutManager.setSpanCount(3);
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
    public void onfeedSelected(DocumentSnapshot outfit) {
        Intent intent = new Intent(this, View_Cloth.class);
        intent.putExtra(View_Cloth.KEY_CLOTH_ID, outfit.getId());
        intent.putExtra(View_Cloth.KEY_CATEGORY_ID,Category_ID);
        intent.putExtra(View_Cloth.KEY_USER_ID,User_ID);
        startActivity(intent);
    }
}
