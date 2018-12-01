package com.virtualrobe.virtualrobe.virtualrobe_app.Search.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.Search.Adapter.MyUsersRecyclerViewAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Users_SearchResults extends Fragment implements MyUsersRecyclerViewAdapter.OnFeedSelectedListener {
    String user="";
    private View view;
    private FirebaseFirestore mFirestore;
    Query mQuery;
    MyUsersRecyclerViewAdapter adapter;

    @BindView(R.id.list)
    RecyclerView recyclerView;

    @BindView(R.id.view_empty)
    LinearLayout empty_layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        user = args.getString("search");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_users_list, container, false);
        ButterKnife.bind(this,view);

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get Resuls of users
        mQuery = mFirestore.collection("Users")
                .orderBy("username", Query.Direction.ASCENDING)
                .whereEqualTo("username",user);

        // Recycleview
        adapter = new MyUsersRecyclerViewAdapter(mQuery,this){
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

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        return view;
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
