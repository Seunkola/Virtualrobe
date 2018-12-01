package com.virtualrobe.virtualrobe.virtualrobe_app.newsfeed_layout;


import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.virtualrobe.virtualrobe.virtualrobe_app.CommentsActivity;
import com.virtualrobe.virtualrobe.virtualrobe_app.MainActivity;
import com.virtualrobe.virtualrobe.virtualrobe_app.Styling_board.Styling_activity;
import com.virtualrobe.virtualrobe.virtualrobe_app.adapter.feedAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.virtualrobe.virtualrobe.virtualrobe_app.R;

public class feed extends Fragment implements feedAdapter.OnFeedSelectedListener,OnShowcaseEventListener {
    private ShowcaseView sv;

    public feed(){
    }

    private static final String TAG = "Feed";

    private static final int RC_SIGN_IN = 9001;

    private static final int LIMIT = 50;

    @BindView(R.id.recycler_feeds)
    RecyclerView recyclerView;

    @BindView(R.id.view_empty)
    ViewGroup mEmptyView;

    @BindView(R.id.fabProgressCircle)
    FABProgressCircle fabProgressCircle;

    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private feedAdapter mAdapter;

    private static final float ALPHA_DIM_VALUE = 0.1f;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feeds, container, false);
        ButterKnife.bind(this,view);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Initialize Firestore and the main RecyclerView
        initFirestore();
        initRecyclerView(view);

        /*set tutorial shocase*/
        RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lps.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        int margin = ((Number) (getResources().getDisplayMetrics().density * 12)).intValue();
        lps.setMargins(margin, margin, margin, margin);

        /*sv = new ShowcaseView.Builder(getActivity())
                .withMaterialShowcase()
                .setTarget(new ViewTarget(fabProgressCircle))
                .setContentTitle("Style Outfits & Post to Newsfeed")
                .setContentText("Press the red circled button to view Styling board")
                .hideOnTouchOutside()
                .setContentText("")
                .setStyle(R.style.CustomShowcaseTheme2)
                .setShowcaseEventListener(this)
                .replaceEndButton(R.layout.view_custom_button)
                .build();*/
        //sv.setButtonPosition(lps);
        //sv.show();
        return view;
    }

    private void initFirestore() {
        // TODO(developer): Implement
        mFirestore = FirebaseFirestore.getInstance();
        mQuery = mFirestore.collection("Outfits")
                .orderBy("newsfeed_time", Query.Direction.DESCENDING)
                .limit(LIMIT);
    }

    private void initRecyclerView(final View view) {
        if (mQuery == null) {
        }

        mAdapter = new feedAdapter(mQuery, this) {

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
                Snackbar.make(view.findViewById(android.R.id.content),
                        "Error: occured.", Snackbar.LENGTH_LONG).show();
            }
        };

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(mAdapter);
    }

    @OnClick(R.id.fab)
    public void Create_outfit(View view){
        startActivity(new Intent(getActivity(), Styling_activity.class));
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
    public void onfeedSelected(DocumentSnapshot outfit) {
        // Go to the details page for the selected restaurant
        Intent intent = new Intent(getActivity(), CommentsActivity.class);
        intent.putExtra(CommentsActivity.KEY_OUTFIT_ID, outfit.getId());

        startActivity(intent);
    }

    private void dimView(View view) {
       view.setAlpha(ALPHA_DIM_VALUE);
   }

    @Override
    public void onShowcaseViewHide(ShowcaseView showcaseView) {
        recyclerView.setAlpha(1f);
    }

    @Override
    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

    }

    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {
        dimView(recyclerView);
    }

    @Override
    public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

    }
}
