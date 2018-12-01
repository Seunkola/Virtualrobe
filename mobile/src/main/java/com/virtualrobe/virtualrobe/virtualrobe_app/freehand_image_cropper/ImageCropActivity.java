package com.virtualrobe.virtualrobe.virtualrobe_app.freehand_image_cropper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload.UploadActivity;
import com.virtualrobe.virtualrobe.virtualrobe_app.MainActivity;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ImageCropActivity extends Activity {
    ImageView compositeImageView;
    boolean crop;
    private MediaScannerConnection msConn;
    String UserId,CategoryId,Category_name;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_crop_activity);

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

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            crop = extras.getBoolean("crop");
        }
        int widthOfscreen = 0;
        int heightOfScreen = 0;

        DisplayMetrics dm = new DisplayMetrics();
        try {
            getWindowManager().getDefaultDisplay().getMetrics(dm);
        } catch (Exception ex) {
        }
        widthOfscreen = dm.widthPixels;
        heightOfScreen = dm.heightPixels;

        compositeImageView = (ImageView) findViewById(R.id.iv);

        final String folder = getIntent().getStringExtra("folder");
        String filename = getIntent().getStringExtra("Image");
        Bitmap bitmap2 = null;
        try {
            bitmap2 = BitmapFactory.decodeStream(openFileInput(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (bitmap2!=null) {
            final int width = bitmap2.getWidth();
            final int height = bitmap2.getHeight();
            final Bitmap resultingImage = Bitmap.createBitmap(widthOfscreen,
                    heightOfScreen, bitmap2.getConfig());

            Canvas canvas = new Canvas(resultingImage);
            Paint paint = new Paint();
            paint.setAntiAlias(true);

            Path path = new Path();
            for (int i = 0; i < CropView.points.size(); i++) {
                path.lineTo(CropView.points.get(i).x, CropView.points.get(i).y);
            }
            canvas.drawPath(path, paint);
            if (crop) {
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            } else {
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
            }
            canvas.drawBitmap(bitmap2, CropView.variable_X, CropView.variable_Y, paint);
            compositeImageView.setImageBitmap(resultingImage);

            //send to mainactivity
            final Button finish = (Button) findViewById(R.id.finish);
            finish.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String file = createBitmapFile(resultingImage);
                    Intent intent = new Intent(ImageCropActivity.this, UploadActivity.class);
                    intent.putExtra("Image", file);
                    intent.putExtra(UploadActivity.KEY_USER_ID,UserId);
                    intent.putExtra(UploadActivity.KEY_CATEGORY_ID,CategoryId);
                    intent.putExtra(UploadActivity.KEY_CATEGORY_NAME,Category_name);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    public String createBitmapFile(Bitmap bitmap) {
        String fileName = "image";
        File imageFileFolder = new File(Environment.getExternalStorageDirectory(), "Virtualrobe_" + fileName);
        imageFileFolder.mkdir();
        FileOutputStream out = null;
        final File imageFileName = new File(imageFileFolder, fileName + ".png");
        try {
            out = new FileOutputStream(imageFileName);
            bitmap.setHasAlpha(true);
            bitmap.compress(Bitmap.CompressFormat.PNG,100, out);
            out.flush();
            out.close();
            scanPhoto(imageFileName.toString());
            out = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageFileName.toString();
    }

    public void scanPhoto(final String imageFileName) {
        msConn = new MediaScannerConnection(ImageCropActivity.this, new MediaScannerConnection.MediaScannerConnectionClient() {
            public void onMediaScannerConnected() {
                msConn.scanFile(imageFileName, null);

            }

            public void onScanCompleted(String path, Uri uri) {
                msConn.disconnect();

            }
        });
        msConn.connect();
    }
}