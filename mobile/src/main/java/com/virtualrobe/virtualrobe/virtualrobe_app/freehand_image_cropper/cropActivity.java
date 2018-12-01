package com.virtualrobe.virtualrobe.virtualrobe_app.freehand_image_cropper;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload.UploadActivity;

import java.io.FileNotFoundException;

public class cropActivity extends Activity {

    private Bitmap bitmap;
    String filename,UserId,CategoryId,Category_name;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filename = getIntent().getStringExtra("bitmap");
        /*get User id*/
        UserId = getIntent().getExtras().getString(UploadActivity.KEY_USER_ID);
        if (UserId == null) {
            throw new IllegalArgumentException("Must pass extra " + UploadActivity.KEY_USER_ID);
        }
        /*get Category id*/
        CategoryId = getIntent().getExtras().getString(UploadActivity.KEY_CATEGORY_ID);
        if (CategoryId == null) {
            throw new IllegalArgumentException("Must pass extra " + UploadActivity.KEY_CATEGORY_ID);
        }

        /*get Category name*/
        Category_name = getIntent().getExtras().getString(UploadActivity.KEY_CATEGORY_NAME);
        if (Category_name == null) {
            throw new IllegalArgumentException("Must pass extra " + UploadActivity.KEY_CATEGORY_NAME);
        }
        try {
            bitmap = BitmapFactory.decodeStream(openFileInput(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        View view = new CropView(cropActivity.this,bitmap,filename,UserId,CategoryId,Category_name);
        setContentView(view);
    }

    @Override    protected void onResume() {
        super.onResume();
    }

}
