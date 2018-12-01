package com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload.Fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload.UploadActivity;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.upload.Constants;
import com.vlk.multimager.utils.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import id.zelory.compressor.Compressor;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class fragment_rotate_image extends Fragment {
    private UploadActivity activity;
    private Bitmap compressed;

    public fragment_rotate_image(){}

    Bitmap bitmap;
    int count_left,count_right = 0;

    public interface BitmapListener {
        void Onbitmap(Bitmap bitmap);
    }

    private BitmapListener listener;

    @BindView(R.id.previewImage)
    ImageView PreviewImage;

    String Category;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rotate_image, container, false);
        ButterKnife.bind(this,view);
        Bundle extras = getArguments();
        if (extras != null) {
            String image  = extras.getString("Image");
            if (image != null && !image.isEmpty()) {
                Uri image_uri = Uri.parse(image);
                Glide.with(this)
                        .load(image_uri)
                        .placeholder(R.drawable.grey)
                        .into(PreviewImage);

                /*if (listener != null) {
                    listener.Onbitmap(bitmap);
                }*/
            }

            String category_name = extras.getString("Category_name");
            if (category_name!=null && !category_name.isEmpty()){
                Category = category_name;
            }
        }

        return view;
    }

    @OnClick(R.id.nextButton)
    public void next_step(View view){
        Bundle bundle = new Bundle();
        bundle.putString("Category_name",Category);
        Fragment fragment = new fragment_select_cloth_color();
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.flContent, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    private Bitmap convertUriToBitmap(Uri uri){
        Bitmap bm = null;
        try {
            bm = BitmapFactory.decodeStream(
                    getActivity().getContentResolver().openInputStream(uri));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(getActivity().getContentResolver().openInputStream(uri));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = 0;
        if (ei != null) {
            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
        }
        switch (orientation) {

            case ExifInterface.ORIENTATION_ROTATE_90:
                bm = rotateImage(bm, 90);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                bm = rotateImage(bm, 180);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                bm = rotateImage(bm, 270);
                break;

            case ExifInterface.ORIENTATION_NORMAL:
            default:
                bm = bm;
        }

        //convert pixels to dp
        //final float scale = getResources().getDisplayMetrics().density;
        //int pixels = (int) (400 * scale + 0.5f);
        //return Constants.getScaledDownBitmap(bm,pixels,false);
        return bm;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BitmapListener) {
            listener = (BitmapListener) context;
        }
        if (context instanceof UploadActivity) {
            this.activity = (UploadActivity) context;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (getActivity() instanceof UploadActivity)
            this.activity=(UploadActivity) getActivity();
    }
}
