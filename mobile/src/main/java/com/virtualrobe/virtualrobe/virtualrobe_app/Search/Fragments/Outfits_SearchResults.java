package com.virtualrobe.virtualrobe.virtualrobe_app.Search.Fragments;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.Search.Adapter.MyOutfitsRecyclerViewAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class Outfits_SearchResults extends Fragment implements MyOutfitsRecyclerViewAdapter.OnFeedSelectedListener {

    private String outfit="";
    private View view;
    private FirebaseFirestore mFirestore;
    Query mQuery;

    @BindView(R.id.list)
    RecyclerView recyclerView;

    @BindView(R.id.view_empty)
    LinearLayout empty_layout;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    private GridLayoutManager layoutManager;
    MyOutfitsRecyclerViewAdapter adapter;
    private boolean isButtonClicked;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        outfit = args.getString("search");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_outfits_results, container, false);
        ButterKnife.bind(this,view);

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get Resuls of users
        mQuery = mFirestore.collection("Outfits")
                .orderBy("newsfeed_time", Query.Direction.ASCENDING)
                .whereEqualTo("user",outfit);

        // Recyleview
        adapter = new MyOutfitsRecyclerViewAdapter(mQuery,this){
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    recyclerView.setVisibility(View.GONE);
                    empty_layout.setVisibility(View.VISIBLE);
                }
                else {
                    recyclerView.setVisibility(View.VISIBLE);
                    empty_layout.setVisibility(View.GONE);
                }
            }
        };
        layoutManager = new GridLayoutManager(getContext(),3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @OnClick(R.id.fab)
    public void onListclicked(View view){
        isButtonClicked = !isButtonClicked;
        fab.setImageResource(isButtonClicked ? R.drawable.ic_list_black_24dp : R.drawable.ic_grid_on_black_24dp);
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
    public void onfeedSelected(DocumentSnapshot user) {

    }
}
