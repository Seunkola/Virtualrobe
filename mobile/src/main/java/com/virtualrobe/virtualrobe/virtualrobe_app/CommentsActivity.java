package com.virtualrobe.virtualrobe.virtualrobe_app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;
import com.virtualrobe.virtualrobe.virtualrobe_app.SocialFeatures.RatingDialogFragment;
import com.virtualrobe.virtualrobe.virtualrobe_app.adapter.RatingAdapter;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Rating;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.outfit_model;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;


public class CommentsActivity extends AppCompatActivity
        implements EventListener<DocumentSnapshot>, RatingDialogFragment.RatingListener{
    public static final String ARG_DRAWING_START_LOCATION = "arg_drawing_start_location";

    private int drawingStartLocation;
    private static final String TAG = "OutfitDetail";
    public static final String KEY_OUTFIT_ID = "key_outfit_id";

    @BindView(R.id.outfit_image)
    ImageView mImageView;

    @BindView(R.id.outfit_rating)
    MaterialRatingBar mRatingIndicator;

    @BindView(R.id.outfit_num_ratings)
    TextView mNumRatingsView;

    @BindView(R.id.outfit_user)
    TextView mUser;

    @BindView(R.id.description)
    TextView mDescription;

    @BindView(R.id.view_empty_ratings)
    ViewGroup mEmptyView;

    @BindView(R.id.recycler_ratings)
    RecyclerView mRatingsRecycler;

    @BindView(R.id.contentRoot)
    RelativeLayout contentroot;

    private RatingDialogFragment mRatingDialog;

    private FirebaseFirestore mFirestore;
    private DocumentReference mOutfitRef;
    private ListenerRegistration mOutfitRegistration;

    private RatingAdapter mRatingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.view_comments);
        ButterKnife.bind(this);

        // Get outfit ID from extras
        String outfitId = getIntent().getExtras().getString(KEY_OUTFIT_ID);
        if (outfitId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_OUTFIT_ID);
        }

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the outfit
        mOutfitRef = mFirestore.collection("Outfits").document(outfitId);

        // Get ratings
        Query ratingsQuery = mOutfitRef
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

        // RecyclerView
        mRatingAdapter = new RatingAdapter(ratingsQuery) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    mRatingsRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mRatingsRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }
    };

        mRatingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRatingsRecycler.setAdapter(mRatingAdapter);

        mRatingDialog = new RatingDialogFragment();

        drawingStartLocation = getIntent().getIntExtra(ARG_DRAWING_START_LOCATION, 0);
        if (savedInstanceState == null) {
        contentroot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                contentroot.getViewTreeObserver().removeOnPreDrawListener(this);
                startIntroAnimation();
                return true;
            }
        });
    }

}

    private void startIntroAnimation() {
        contentroot.setScaleY(0.1f);
        contentroot.setPivotY(drawingStartLocation);

        contentroot.animate()
                .scaleY(1)
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                    }
                })
                .start();
    }

    @Override
    public void onBackPressed() {
        contentroot.animate()
                .translationY(Utils.getScreenHeight(this))
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        CommentsActivity.super.onBackPressed();
                        overridePendingTransition(0, 0);
                    }
                })
                .start();
    }

    private static class Utils {
        private static int screenWidth = 0;
        private static int screenHeight = 0;

        public static int dpToPx(int dp) {
            return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
        }

        public static int getScreenHeight(Context c) {
            if (screenHeight == 0) {
                WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screenHeight = size.y;
            }

            return screenHeight;
        }

        public static int getScreenWidth(Context c) {
            if (screenWidth == 0) {
                WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screenWidth = size.x;
            }

            return screenWidth;
        }

        public static boolean isAndroid5() {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mRatingAdapter.startListening();
        mOutfitRegistration = mOutfitRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        mRatingAdapter.stopListening();

        if (mOutfitRegistration != null) {
            mOutfitRegistration.remove();
            mOutfitRegistration = null;
        }
    }

    private Task<Void> addRating(final DocumentReference OutfitRef, final Rating rating) {
        // TODO(developer): Implement
        // Create reference for new rating, for use inside the transaction
        final DocumentReference ratingRef = OutfitRef.collection("ratings")
                .document();

        // In a transaction, add the new rating and update the aggregate totals
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction)
                    throws FirebaseFirestoreException {

                outfit_model outfit = transaction.get(OutfitRef)
                        .toObject(outfit_model.class);

                // Compute new number of ratings
                int newNumRatings = outfit.getNumRatings() + 1;

                // Compute new average rating
                double oldRatingTotal = outfit.getAvgRating() *
                        outfit.getNumRatings();
                double newAvgRating = (oldRatingTotal + rating.getRating()) /
                        newNumRatings;

                // Set new restaurant info
                outfit.setNumRatings(newNumRatings);
                outfit.setAvgRating(newAvgRating);

                // Commit to Firestore
                transaction.set(OutfitRef, outfit);
                transaction.set(ratingRef, rating);

                return null;
            }
        });
    }

    @Override
    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
        if (e != null) {
            return;
        }

        onOutfitLoaded(documentSnapshot.toObject(outfit_model.class));
    }

    private void onOutfitLoaded(outfit_model outfit) {
        mUser.setText(outfit.getuser());
        mRatingIndicator.setRating((float) outfit.getAvgRating());
        mNumRatingsView.setText(getString(R.string.fmt_num_ratings, outfit.getNumRatings()));
        mDescription.setText(outfit.getDescribe());

        // Background image
        Glide.with(mImageView.getContext())
                .load(outfit.getImage_thumbnailbg())
                .into(mImageView);
    }

    @OnClick(R.id.outfit_button_back)
    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    @OnClick(R.id.fab_show_rating_dialog)
    public void onAddRatingClicked(View view) {
        mRatingDialog.show(getSupportFragmentManager(), RatingDialogFragment.TAG);
    }

    @Override
    public void onRating(Rating rating) {
        // In a transaction, add the new rating and update the aggregate totals
        addRating(mOutfitRef, rating)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Hide keyboard and scroll to top
                        hideKeyboard();
                        mRatingsRecycler.smoothScrollToPosition(0);
                        Snackbar.make(findViewById(android.R.id.content), "Rating Added",
                                Snackbar.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Show failure message and hide keyboard
                        hideKeyboard();
                        Snackbar.make(findViewById(android.R.id.content), "Failed to add rating",
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

}
