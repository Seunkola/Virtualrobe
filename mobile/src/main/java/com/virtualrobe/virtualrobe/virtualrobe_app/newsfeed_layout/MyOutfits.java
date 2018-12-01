package com.virtualrobe.virtualrobe.virtualrobe_app.newsfeed_layout;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.Styling_board.Styling_activity;
import com.virtualrobe.virtualrobe.virtualrobe_app.TypefaceSpan;
import com.virtualrobe.virtualrobe.virtualrobe_app.adapter.OutfitAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MyOutfits extends AppCompatActivity implements OutfitAdapter.OnFeedSelectedListener {

    public MyOutfits(){

    }

    @BindView(R.id.page_text)
    TextView page_Header;

    @BindView(R.id.text_current_search)
    TextView mCurrentSearchView;

    @BindView(R.id.text_current_sort_by)
    TextView mCurrentSortByView;

    @BindView(R.id.recycler_outfits)
    RecyclerView recyclerView;

    @BindView(R.id.view_empty)
    LinearLayout mEmptyView;
    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private OutfitAdapter mAdapter;
    private GridLayoutManager layoutManager;
    private boolean isButtonClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_outfit);
        ButterKnife.bind(this);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        /*Set Category name and font type*/
        SpannableString s = new SpannableString("My Outfits");
        s.setSpan(new TypefaceSpan(this, "LobsterTwo-Bold.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        page_Header.setText(s);

        // Initialize Firestore and the main RecyclerView
        initFirestore();
        initRecyclerView();
    }

    private void initFirestore() {
        //String user_name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String user_name = "seunk";
        mFirestore = FirebaseFirestore.getInstance();
        if (user_name!=null) {
            mQuery = mFirestore.collection("Outfits")
                    .orderBy("newsfeed_time", Query.Direction.DESCENDING)
                    .whereEqualTo("user", user_name);
        }
    }

    private void initRecyclerView() {
        mAdapter = new OutfitAdapter(mQuery, this) {

            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: occured.", Snackbar.LENGTH_LONG).show();
            }
        };

        layoutManager = new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onfeedSelected(DocumentSnapshot outfit) {
    }

    @OnClick(R.id.fab)
    public void onfabClicked(View view) {
        startActivity(new Intent(MyOutfits.this, Styling_activity.class));
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
        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
