package com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload.Free_hand_cropper_updated;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.virtualrobe.virtualrobe.virtualrobe_app.R;

public class DisplayCropActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.displaycrop);

        ImageView im_crop = (ImageView) findViewById(R.id.im_crop);
        byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        im_crop.setImageBitmap(bmp);
    }
}
