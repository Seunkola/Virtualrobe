package com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.otaliastudios.autocomplete.Autocomplete;
import com.otaliastudios.autocomplete.AutocompleteCallback;
import com.otaliastudios.autocomplete.AutocompletePresenter;
import com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload.AutoComplete.BrandPresenter;
import com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload.AutoComplete.ColorPresenter;
import com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload.Fragments.fragment_rotate_image;
import com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload.Free_hand_cropper_updated.CropActivity;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.TypefaceSpan;
import com.virtualrobe.virtualrobe.virtualrobe_app.freehand_image_cropper.cropActivity;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Brand;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Category_item_model;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Clothes;
import com.vlk.multimager.activities.GalleryActivity;
import com.vlk.multimager.activities.MultiCameraActivity;
import com.vlk.multimager.utils.Constants;
import com.vlk.multimager.utils.Image;
import com.vlk.multimager.utils.Params;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.model.AspectRatio;
import com.yalantis.ucrop.view.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

public class UploadActivity extends AppCompatActivity implements
        fragment_rotate_image.BitmapListener{

    @BindView(R.id.page_text)
    TextView header;

    @BindView(R.id.flContent)
    FrameLayout frameLayout;

    private Fragment fragment;
    ArrayList<Image> imagesList;
    private Bitmap bitmap;
    private String Image_name,User_name,UserId,CategoryId,Category_name;

    public static final String KEY_CATEGORY_ID = "Category_ID";
    public static final String KEY_CATEGORY_NAME = "Category";
    public static final String KEY_USER_ID = "User_ID";
    private static final String SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage";
    private AsyncTask<Bitmap, Void, Void> StartUpload;
    private DocumentReference Categoryref;
    private Autocomplete userAutocomplete;
    private Autocomplete ColorAutocomplete;
    private TransferObserver observer,observer_sm;
    private boolean click;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        ButterKnife.bind(this);

        /*Get User name*/
        SharedPreferences sharedPreferences=getSharedPreferences("virtualrobe",MODE_PRIVATE);
        User_name = sharedPreferences.getString("username","");

        /*get User id*/
        UserId = getIntent().getExtras().getString(KEY_USER_ID);
        if (UserId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_USER_ID);
        }
        /*get Category id*/
        CategoryId = getIntent().getExtras().getString(KEY_CATEGORY_ID);
        if (CategoryId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_CATEGORY_ID);
        }

        /*get Category name*/
        Category_name = getIntent().getExtras().getString(KEY_CATEGORY_NAME);
        if (Category_name == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_CATEGORY_NAME);
        }

        /* set document references */
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference alovelaceDocumentRef = db.collection("Users").document(UserId);
        Categoryref = alovelaceDocumentRef.collection("Wardrobe").document(CategoryId);

        //set default fragment
        display_default(savedInstanceState);

        /*Set App name and font type*/
        SpannableString s = new SpannableString("Edit Image");
        s.setSpan(new TypefaceSpan(this, "LobsterTwo-Bold.ttf"), 0, s.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        header.setText(s);

        /*get free hand cropped image*/
        String filename = getIntent().getStringExtra("Image");
        if (filename !=null && !filename.equals("")) {
            try {
                com.virtualrobe.virtualrobe.virtualrobe_app.upload.Constants constant = new com.virtualrobe.virtualrobe.virtualrobe_app.upload.Constants();
                bitmap = constant.compressImage_toBitmap(filename,UploadActivity.this);
                displayView(1, "rotate");
            }
            catch (Exception e){

            }
        }
        else {
            /*call image picker*/
            selectImage();
        }
    }

    /*save image to category*/

    private String CapitalizeFirstChar(String s){
        char c[] = s.toCharArray();
        c[0] = Character.toUpperCase(c[0]);
        return new String(c);
    }

    private void setupBrandAutocomplete(EditText edit) {
        float elevation = 6f;
        Drawable backgroundDrawable = new ColorDrawable(Color.WHITE);
        AutocompletePresenter<Brand> presenter = new BrandPresenter(this);
        AutocompleteCallback<Brand> callback = new AutocompleteCallback<Brand>() {
            @Override
            public boolean onPopupItemClicked(Editable editable, Brand item) {
                editable.clear();
                editable.append(item.getName());
                return true;
            }

            public void onPopupVisibilityChanged(boolean shown) {}
        };

        userAutocomplete = Autocomplete.<Brand>on(edit)
                .with(elevation)
                .with(backgroundDrawable)
                .with(presenter)
                .with(callback)
                .build();
    }

    private void setupColorAutocomplete(EditText edit) {
        float elevation = 6f;
        Drawable backgroundDrawable = new ColorDrawable(Color.WHITE);
        AutocompletePresenter<com.virtualrobe.virtualrobe.virtualrobe_app.model.Color> presenter = new ColorPresenter(this);
        AutocompleteCallback<com.virtualrobe.virtualrobe.virtualrobe_app.model.Color> callback = new AutocompleteCallback<com.virtualrobe.virtualrobe.virtualrobe_app.model.Color>() {
            @Override
            public boolean onPopupItemClicked(Editable editable, com.virtualrobe.virtualrobe.virtualrobe_app.model.Color item) {
                editable.clear();
                editable.append(item.getName());
                return true;
            }

            public void onPopupVisibilityChanged(boolean shown) {}
        };

        ColorAutocomplete = Autocomplete.<com.virtualrobe.virtualrobe.virtualrobe_app.model.Color>on(edit)
                .with(elevation)
                .with(backgroundDrawable)
                .with(presenter)
                .with(callback)
                .build();
    }

    /*save clothe details to database*/
    private Task<Void> addDetails(final DocumentReference CategoryRef, final Category_item_model clothes) {
        final DocumentReference category_item = CategoryRef.collection("Category_item").document();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                transaction.set(category_item,clothes);
                return null;
            }
        });
    }

    /* Select image dialog*/
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(UploadActivity.this);
        builder.setTitle("Add New Item!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                int color = Color.parseColor("#efc04a");
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(UploadActivity.this, MultiCameraActivity.class);
                    Params params = new Params();
                    params.setCaptureLimit(1);
                    params.setToolbarColor(color);
                    params.setActionButtonColor(color);
                    params.setButtonTextColor(color);
                    intent.putExtra(Constants.KEY_PARAMS, params);
                    startActivityForResult(intent, Constants.TYPE_MULTI_CAPTURE);

                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(UploadActivity.this, GalleryActivity.class);
                    Params params = new Params();
                    params.setCaptureLimit(1);
                    params.setPickerLimit(1);
                    params.setToolbarColor(color);
                    params.setActionButtonColor(color);
                    params.setButtonTextColor(color);
                    intent.putExtra(Constants.KEY_PARAMS, params);
                    startActivityForResult(intent, Constants.TYPE_MULTI_PICKER);

                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                    onBackPressed();
                }
            }
        });
        builder.show();

    }

    /*Display default fragment*/
    private void display_default(Bundle savedInstanceState) {
        fragment = null;
        fragment = new fragment_rotate_image();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.flContent, fragment,"Rotate").commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode != RESULT_OK) {
            if (resultCode == UCrop.RESULT_ERROR) {
                final Throwable cropError = UCrop.getError(intent);
            }
            return;
        }
        switch (requestCode) {
            case Constants.TYPE_MULTI_CAPTURE:
                handleResponseIntent(intent);
                break;
            case Constants.TYPE_MULTI_PICKER:
                handleResponseIntent(intent);
                break;
        }
        if (requestCode == UCrop.REQUEST_CROP) {
            final Uri resultUri = UCrop.getOutput(intent);
            if (resultUri!=null) {
                displayView(0, resultUri.toString());
            }
        }
    }

    /*Handle image selection */
    private void handleResponseIntent(Intent intent) {
        imagesList = intent.getParcelableArrayListExtra(Constants.KEY_BUNDLE_LIST);
        displayView(1,"rotate");
    }

    public ArrayList<Image> images(){
        return imagesList;
    }

    public void displayView(int position,String tag) {
        // update the main content by replacing fragments
        Fragment fragment = null;
        switch (position) {
            case 0:
                Bundle bundle = new Bundle();
                bundle.putString("Image",tag);
                bundle.putString("Category_name",Category_name);
                fragment = new fragment_rotate_image();
                fragment.setArguments(bundle);
                break;
            case 1:
                if (imagesList != null && !imagesList.isEmpty()) {
                    Image image = imagesList.get(0);
                    int minSizePixels = 800;
                    int maxSizePixels = 2400;
                    UCrop.Options options = new UCrop.Options();
                    options.setToolbarColor(ContextCompat.getColor(this, R.color.list_background_pressed));
                    // Aspect ratio options
                    options.setAspectRatioOptions(1,
                            new AspectRatio("WOW", 1, 2),
                            new AspectRatio("MUCH", 3, 4),
                            new AspectRatio("RATIO", CropImageView.DEFAULT_ASPECT_RATIO, CropImageView.DEFAULT_ASPECT_RATIO),
                            new AspectRatio("SO", 16, 9),
                            new AspectRatio("ASPECT", 1, 1));
                    UCrop.of(image.uri, Uri.fromFile(new File(getCacheDir(), SAMPLE_CROPPED_IMAGE_NAME)))
                           .withOptions(options)
                           .withMaxResultSize(minSizePixels, maxSizePixels)
                           .start(UploadActivity.this);


                }
                break;

            default:
                fragment = null;
                break;
        }

        if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.flContent, fragment,tag).commit();
            }
    }

    @Override
    public void Onbitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


    public Bitmap getBitmap(){
        return bitmap;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (StartUpload!=null && StartUpload.getStatus()== AsyncTask.Status.RUNNING){
            StartUpload.cancel(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (StartUpload!=null && StartUpload.getStatus()== AsyncTask.Status.RUNNING){
            StartUpload.cancel(true);
        }
    }
}
