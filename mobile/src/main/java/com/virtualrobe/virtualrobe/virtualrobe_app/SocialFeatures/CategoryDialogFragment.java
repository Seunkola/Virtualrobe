package com.virtualrobe.virtualrobe.virtualrobe_app.SocialFeatures;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.adapter.CategoryAdapter;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.wardrobe_model;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CategoryDialogFragment extends DialogFragment implements CategoryAdapter.OnImageSelectedListener {
    public static final String TAG = "CategoryDialog";

    @BindView(R.id.form_text)
    EditText mText;

    @BindView(R.id.imageSelectionButton)
    RecyclerView recyclerView;

    private static final int LIMIT = 50;
    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private CategoryAdapter adapter;

    public interface CategoryListener {

        void onAdd(wardrobe_model wardrobe);

    }

    private CategoryListener mCategoryListener;

    private String background_image;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_category, container, false);
        ButterKnife.bind(this, v);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Initialize Firestore and the main RecyclerView
        initFirestore();
        initRecyclerView(v);

        return v;
    }

    private void initFirestore() {
        mFirestore = FirebaseFirestore.getInstance();
        mQuery = mFirestore.collection("Wardrobe_background_images")
                .limit(LIMIT);
    }

    private void initRecyclerView(final View v) {
        if (mQuery == null) {
        }
        adapter = new CategoryAdapter(mQuery, this) {

            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    recyclerView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(v.findViewById(android.R.id.content),
                        "Error: occured.", Snackbar.LENGTH_LONG).show();
            }
        };

        Context context = getActivity();
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(context,2));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof CategoryListener) {
            mCategoryListener = (CategoryListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

    }

    @OnClick(R.id.form_button)
    public void onSubmitClicked(View view) {
        if (background_image!=null && !background_image.equals("")) {
            wardrobe_model wardrobe = new wardrobe_model(background_image,mText.getText().toString());

            if (mCategoryListener != null){
                mCategoryListener.onAdd(wardrobe);
            }
            else {
                Toast.makeText(getActivity(), "not working", Toast.LENGTH_SHORT).show();
            }

            dismiss();

        }
        else {
            Snackbar.make(view,
                    "Select Image Background", Snackbar.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.form_cancel)
    public void onCancelClicked(View view) {
        dismiss();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Start listening for Firestore updates
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }

    @Override
    public void onfeedSelected(DocumentSnapshot category) {
        wardrobe_model model = category.toObject(wardrobe_model.class);
        background_image = model.getImage();
        Toast.makeText(getActivity(), "selected", Toast.LENGTH_SHORT).show();
    }
}
