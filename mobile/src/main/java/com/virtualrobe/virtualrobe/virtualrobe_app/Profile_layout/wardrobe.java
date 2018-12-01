package com.virtualrobe.virtualrobe.virtualrobe_app.Profile_layout;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;
import com.virtualrobe.virtualrobe.virtualrobe_app.Profile_Activity;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.SocialFeatures.CategoryDialogFragment;
import com.virtualrobe.virtualrobe.virtualrobe_app.SocialFeatures.RatingDialogFragment;
import com.virtualrobe.virtualrobe.virtualrobe_app.adapter.wardrobeAdapter;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.user_model;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.wardrobe_model;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class wardrobe extends Fragment implements CategoryDialogFragment.CategoryListener{
    private Profile_Activity activity;
    private wardrobeAdapter mAdapter;

    public wardrobe() {
    }

    @BindView(R.id.recycler_wardrobe)
    RecyclerView recyclerView;

    private FirebaseFirestore mFirestore;
    private DocumentReference mUserRef;
    private ListenerRegistration mUserRegistration;
    ArrayList<String> images = new ArrayList<String>();
    ArrayList<String> images_name = new ArrayList<String>();
    private CategoryDialogFragment categoryDialogFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_wardrobe, container, false);
        ButterKnife.bind(this,rootView);

        /*get user ID*/
        final String user_id = String.valueOf(activity.user_id());

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        //add_default_categories
        add_categories();

        // Initialize Firestore and the main RecyclerView "8jce8KxvYtFl3iyuhYDL"
        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the User
        mUserRef = mFirestore.collection("Users").document(user_id);

        // Get category
        Query query = mUserRef
                .collection("Wardrobe")
                .limit(50);

        // RecyclerView
        mAdapter = new wardrobeAdapter(query,user_id) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                     /* add default categories*/
                    if (images!=null && images.size()!=0) {
                        for (int i=0; i<images.size(); i++) {
                            wardrobe_model category = new wardrobe_model();
                            category.setImage(images.get(i));
                            category.setImage_name(images_name.get(i));
                            addCategory(mUserRef, category)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Snackbar.make(rootView, "Category Added",
                                                    Snackbar.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Snackbar.make(rootView, "Failed to add Category",
                                                    Snackbar.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }
                else {
                    Snackbar.make(rootView, "Category available",
                            Snackbar.LENGTH_SHORT).show();
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        };

        Context context = getActivity();
        //recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(context,2));
        recyclerView.setAdapter(mAdapter);

        return rootView;
    }

    private void add_categories(){
        images.add("http://www.virtualrobe.com/wardrobe/shirts.jpg");
        images.add("http://www.virtualrobe.com/wardrobe/trousers.jpg");
        images.add("http://www.virtualrobe.com/wardrobe/shoes.jpg");
        images.add("http://www.virtualrobe.com/wardrobe/watches.jpg");

        images_name.add("Shirts");
        images_name.add("Bottoms");
        images_name.add("Shoes");
        images_name.add("Accessories");
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Profile_Activity)
            this.activity=(Profile_Activity) context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() instanceof Profile_Activity)
            this.activity=(Profile_Activity) getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }

    public Task<Void> addCategory(final DocumentReference UserRef, final wardrobe_model wardrobe) {
        // Create reference for new Category, for use inside the transaction
        final DocumentReference wardrobeRef = UserRef.collection("Wardrobe")
                .document();

        // In a transaction, add the new Category
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction)
                    throws FirebaseFirestoreException {

                transaction.set(wardrobeRef, wardrobe);

                return null;
            }
        });
    }

    @OnClick(R.id.fab)
    public void onAddCategoryClicked(View view) {
        categoryDialogFragment = new CategoryDialogFragment();
        categoryDialogFragment.show(getFragmentManager(), CategoryDialogFragment.TAG);
    }

    @Override
    public void onAdd(wardrobe_model wardrobe) {

    }

}





