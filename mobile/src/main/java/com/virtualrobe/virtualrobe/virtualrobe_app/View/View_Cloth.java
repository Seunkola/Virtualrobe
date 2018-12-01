package com.virtualrobe.virtualrobe.virtualrobe_app.View;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.nightonke.boommenu.BoomButtons.ButtonPlaceEnum;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.nightonke.boommenu.ButtonEnum;
import com.nightonke.boommenu.Piece.PiecePlaceEnum;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.TypefaceSpan;
import com.virtualrobe.virtualrobe.virtualrobe_app.Utility.PicassoBigCache;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Category_item_model;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.supercharge.shimmerlayout.ShimmerLayout;


public class View_Cloth extends AppCompatActivity implements EventListener<DocumentSnapshot> {
    public static String KEY_CLOTH_ID = "ID";
    public static String KEY_CATEGORY_ID = "CID";
    public static String KEY_USER_ID="UID";

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @BindView(R.id.page_text)
    TextView header;

    @BindView(R.id.image)
    ImageView imageView;

    @BindView(R.id.description)
    TextView description;

    @BindView(R.id.cloth_size)
    TextView cloth_size;

    @BindView(R.id.color)
    ImageView color;

    @BindView(R.id.color_name)
    TextView color_name;

    @BindView(R.id.brand)
    TextView brand;

    @BindView(R.id.bmb)
    BoomMenuButton boomMenuButton;

    @BindView(R.id.shimmer_text)
    ShimmerLayout shimmerLayout;

    private FirebaseFirestore mFirestore;
    private DocumentReference mUserRef;
    private DocumentReference mCategoryRef;
    private DocumentReference mClothRef;
    private ListenerRegistration mClothRegistration;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_view_cloth);
        ButterKnife.bind(this);
        set_bmb();

        /*start shimmering imageview*/
        shimmerLayout.startShimmerAnimation();

        /*Set header name and font type*/
        SpannableString s = new SpannableString("View Item");
        s.setSpan(new TypefaceSpan(this, "LobsterTwo-Bold.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        header.setText(s);

        /*get User id*/
        String UserId = getIntent().getExtras().getString(KEY_USER_ID);
        if (UserId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_USER_ID);
        }
        /*get Category id*/
        String CategoryId = getIntent().getExtras().getString(KEY_CATEGORY_ID);
        if (CategoryId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_CATEGORY_ID);
        }
        /*get Cloth id*/
        String ClothId = getIntent().getExtras().getString(KEY_CLOTH_ID);
        if (ClothId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_CLOTH_ID);
        }
        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        //get refrence to the user
        mUserRef = mFirestore.collection("Users").document(UserId);

        // Get reference to the category
        mCategoryRef = mUserRef.collection("Wardrobe").document(CategoryId);

        // Get reference to the cloth
        mClothRef = mCategoryRef.collection("Category_item").document(ClothId);

    }

    public void set_bmb(){
        assert boomMenuButton != null;
        boomMenuButton.setButtonEnum(ButtonEnum.Ham);
        boomMenuButton.setPiecePlaceEnum(PiecePlaceEnum.HAM_4);
        boomMenuButton.setButtonPlaceEnum(ButtonPlaceEnum.HAM_4);

        for (int i = 0; i < boomMenuButton.getPiecePlaceEnum().pieceNumber(); i++) {
            HamButton.Builder builder = new HamButton.Builder();
            if(i == 0){

                builder.index(i).normalImageRes(R.drawable.ic_create_white_24dp);
                builder.index(i).normalTextRes(R.string.edit);
                builder.index(i).subNormalTextRes(R.string.edit_descr2);
                builder.index(i).listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        // When the boom-button corresponding this builder is clicked.
                    }
                });
            }
            else if (i == 1){
                builder.index(i).normalImageRes(R.drawable.ic_delete_white_24dp);
                builder.index(i).normalTextRes(R.string.assign);
                builder.index(i).subNormalTextRes(R.string.assign_descr1);
                builder.index(i).listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        // When the boom-button corresponding this builder is clicked.
                    }
                });
            }

            else if (i == 2){
                builder.index(i).normalImageRes(R.drawable.ic_crop_white_24dp);
                builder.index(i).normalTextRes(R.string.crop);
                builder.index(i).subNormalTextRes(R.string.crop_descr1);
                builder.index(i).listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                    }
                });
            }

            else {
                builder.index(i).normalImageRes(R.drawable.ic_delete_forever_white_24dp);
                builder.index(i).normalTextRes(R.string.delete);
                builder.index(i).subNormalTextRes(R.string.delete_descr1);
                builder.index(i).listener(new OnBMClickListener() {
                    @Override
                    public void onBoomButtonClick(int index) {
                        // When the boom-button corresponding this builder is clicked.
                    }
                });
            }
            boomMenuButton.addBuilder(builder);

        }

    }

    @Override
    public void onStart() {
        super.onStart();
        mClothRegistration = mClothRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mClothRegistration != null) {
            mClothRegistration.remove();
            mClothRegistration = null;
        }
    }

    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e!=null){
            return;
        }

        onClothLoaded(snapshot.toObject(Category_item_model.class));
        progressBar.setVisibility(View.GONE);
    }

    private void onClothLoaded(final Category_item_model cloth) {
        // cloth image

        /*get image from memory*/
        PicassoBigCache.INSTANCE.getPicassoBigCache(View_Cloth.this)
                .load(cloth.getFull_image())
                .placeholder(R.drawable.grey)
                .error(R.drawable.grey)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        shimmerLayout.stopShimmerAnimation();
                    }

                    @Override
                    public void onError() {
                        /*Load thumbnail*/
                        PicassoBigCache.INSTANCE.getPicassoBigCache(View_Cloth.this)
                                .load(cloth.getThumbnail())
                                .placeholder(R.drawable.grey)
                                .error(R.drawable.grey)
                                .into(imageView, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        shimmerLayout.stopShimmerAnimation();
                                        /*load full image from network with thubnail as placeholder*/
                                        PicassoBigCache.INSTANCE.getPicassoBigCache(View_Cloth.this)
                                                .load(cloth.getThumbnail())
                                                .placeholder(imageView.getDrawable())
                                                .error(R.drawable.error_placeholder)
                                                .into(imageView);
                                    }

                                    @Override
                                    public void onError() {
                                        /*load full image from network without thubnail as placeholder*/
                                        PicassoBigCache.INSTANCE.getPicassoBigCache(View_Cloth.this)
                                                .load(cloth.getThumbnail())
                                                .placeholder(R.drawable.grey)
                                                .error(R.drawable.error_placeholder)
                                                .into(imageView);
                                        shimmerLayout.stopShimmerAnimation();
                                    }
                                });
                    }
                });

        /*get description*/
        if (cloth.getDescription()!=null && !cloth.getDescription().equals("")){
            description.setText(cloth.getDescription());
        }

        /*get cloth size*/
        if (cloth.getSize()!=null && !cloth.getSize().equals("")){
            cloth_size.setText(cloth.getSize());
        }

        /*get color*/
        if (cloth.getColor()!=null && !cloth.getColor().equals("")){
            color_name.setText(cloth.getColor());
        }

        /*get brand*/
        if (cloth.getBrand()!=null && !cloth.getBrand().equals("")){
            brand.setText(cloth.getBrand());
        }

    }
}
