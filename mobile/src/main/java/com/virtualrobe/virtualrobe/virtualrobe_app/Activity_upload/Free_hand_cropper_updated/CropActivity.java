package com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload.Free_hand_cropper_updated;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.virtualrobe.virtualrobe.virtualrobe_app.R;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;

public class CropActivity extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener, BottomNavigation.OnMenuItemSelectionListener {
    ImageView im_crop_image_view;
    Path clipPath;
    Bitmap bmp;
    Bitmap alteredBitmap;
    Canvas canvas;
    Paint paint;
    float downx = 0;
    float downy = 0;
    float tdownx = 0;
    float tdowny = 0;
    float upx = 0;
    float upy = 0;
    long lastTouchDown = 0;
    int CLICK_ACTION_THRESHHOLD = 100;
    Display display;
    Point size;
    int screen_width,screen_height;
    Button btn_ok;
    ArrayList<CropModel> cropModelArrayList;
    float smallx,smally,largex,largey;
    Paint cpaint;
    Bitmap temporary_bitmap;
    private ProgressDialog pDialog;

    @BindView(R.id.BottomNavigation)
    BottomNavigation bottomNavigation;

    @BindView(R.id.resize_view)
    RelativeLayout resize_view;

    @BindView(R.id.seekBar1)
    SeekBar resize_seekbar;

    @BindView(R.id.progress)
    TextView progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        ButterKnife.bind(this);

        init();

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.list_background_pressed));
            }
        }

        int cx = (screen_width - bmp.getWidth()) >> 1;
        int cy = (screen_height - bmp.getHeight()) >> 1;
        canvas.drawBitmap(bmp, cx, cy, null);
        im_crop_image_view.setImageBitmap(alteredBitmap);
        im_crop_image_view.setOnTouchListener(this);

        /*bottom navigation*/
        bottomNavigation.setOnMenuItemClickListener(this);

        /*Increase and decrease image size*/
        // Initialize the textview with '0'.
        progress.setText("Covered: " + resize_seekbar.getProgress() + "/" + resize_seekbar.getMax());

        resize_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress_value = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress_value = progresValue;
                Toast.makeText(getApplicationContext(), "Resizing Image", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                progress.setText("Covered: " + progress_value + "/" + seekBar.getMax());
                resize(progress_value);
            }
        });

    }

    private void resize(int progress_value) {
        if (progress_value==1){
            alteredBitmap = Bitmap.createBitmap(screen_width-20, screen_height-20, bmp.getConfig());
            canvas = new Canvas(alteredBitmap);
        }
        else if (progress_value == 2){
            alteredBitmap = Bitmap.createBitmap(screen_width-40, screen_height-40, bmp.getConfig());
            canvas = new Canvas(alteredBitmap);
        }
        else if (progress_value == 3){
            alteredBitmap = Bitmap.createBitmap(screen_width-60, screen_height-60, bmp.getConfig());
            canvas = new Canvas(alteredBitmap);
        }
        else if (progress_value == 4){
            alteredBitmap = Bitmap.createBitmap(screen_width-80, screen_height-80, bmp.getConfig());
            canvas = new Canvas(alteredBitmap);
        }
        else if (progress_value == 5){
            alteredBitmap = Bitmap.createBitmap(screen_width-100, screen_height-100, bmp.getConfig());
            canvas = new Canvas(alteredBitmap);
        }
    }

    private void init() {
        pDialog = new ProgressDialog(CropActivity.this);
        im_crop_image_view = (ImageView) findViewById(R.id.im_crop_image_view);
        cropModelArrayList = new ArrayList<>();
        btn_ok = (Button) findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(this);
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        screen_width = size.x;
        screen_height = size.y;

        initcanvas();
    }

    public void initcanvas() {

        Drawable d = getResources().getDrawable(R.drawable.polo);
        bmp = ((BitmapDrawable)d).getBitmap();

        alteredBitmap = Bitmap.createBitmap(screen_width, screen_height, bmp.getConfig());
        canvas = new Canvas(alteredBitmap);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        paint.setPathEffect(new DashPathEffect(new float[]{15.0f, 15.0f}, 0));

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int action = motionEvent.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downx = motionEvent.getX();
                downy = motionEvent.getY();
                clipPath = new Path();
                clipPath.moveTo(downx, downy);
                tdownx = downx;
                tdowny = downy;
                smallx = downx;
                smally = downy;
                largex = downx;
                largey = downy;
                lastTouchDown = System.currentTimeMillis();
                break;

            case MotionEvent.ACTION_MOVE:
                upx = motionEvent.getX();
                upy = motionEvent.getY();
                cropModelArrayList.add(new CropModel(upx, upy));
                clipPath = new Path();
                clipPath.moveTo(tdownx,tdowny);
                for(int i = 0; i<cropModelArrayList.size();i++){
                    clipPath.lineTo(cropModelArrayList.get(i).getY(),cropModelArrayList.get(i).getX());
                }
                canvas.drawPath(clipPath, paint);
                im_crop_image_view.invalidate();
                downx = upx;
                downy = upy;
                break;
            case MotionEvent.ACTION_UP:
                if (System.currentTimeMillis() - lastTouchDown < CLICK_ACTION_THRESHHOLD) {

                    cropModelArrayList.clear();
                    initcanvas();

                    int cx = (screen_width - bmp.getWidth()) >> 1;
                    int cy = (screen_height - bmp.getHeight()) >> 1;
                    canvas.drawBitmap(bmp, cx, cy, null);
                    im_crop_image_view.setImageBitmap(alteredBitmap);

                } else {
                    if (upx != upy) {
                        upx = motionEvent.getX();
                        upy = motionEvent.getY();


                        canvas.drawLine(downx, downy, upx, upy, paint);
                        clipPath.lineTo(upx, upy);
                        im_crop_image_view.invalidate();

                        crop();
                    }

                }
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }

    public void crop() {
        clipPath.close();
        clipPath.setFillType(Path.FillType.INVERSE_WINDING);

        for(int i = 0; i<cropModelArrayList.size();i++){
            if(cropModelArrayList.get(i).getY()<smallx){

                smallx=cropModelArrayList.get(i).getY();
            }
            if(cropModelArrayList.get(i).getX()<smally){

                smally=cropModelArrayList.get(i).getX();
            }
            if(cropModelArrayList.get(i).getY()>largex){

                largex=cropModelArrayList.get(i).getY();
            }
            if(cropModelArrayList.get(i).getX()>largey){

                largey=cropModelArrayList.get(i).getX();
            }
        }

        temporary_bitmap = alteredBitmap;
        cpaint = new Paint();
        cpaint.setAntiAlias(true);
        cpaint.setColor(getResources().getColor(R.color.colorAccent));
        cpaint.setAlpha(100);
        canvas.drawPath(clipPath, cpaint);

        canvas.drawBitmap(temporary_bitmap, 0, 0, cpaint);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_ok:
                save();

            default:
                break;
        }
    }

    private void save() {
        if(clipPath != null) {
            final int color = 0xff424242;
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawPath(clipPath, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            canvas.drawBitmap(alteredBitmap, 0, 0, paint);

            float w = largex - smallx;
            float h = largey - smally;
            alteredBitmap = Bitmap.createBitmap(alteredBitmap, (int) smallx, (int) smally, (int) w, (int) h);

        }else{
            alteredBitmap = bmp;
        }
        pDialog.show();

        Thread mThread = new Thread() {
            @Override
            public void run() {

                Bitmap bitmap = alteredBitmap;
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 70, stream);
                byte[] byteArray = stream.toByteArray();
                pDialog.dismiss();

                Intent intent = new Intent(CropActivity.this, DisplayCropActivity.class);
                intent.putExtra("image",byteArray);
                startActivity(intent);
            }
        };
        mThread.start();
    }

    @Override
    public void onMenuItemSelect(@IdRes int itemid, int position) {
        int tab = bottomNavigation.getMenuItemId(position);
        if (tab == R.id.tab_undo) {
            canvas.restore();
        }
        else if(tab == R.id.tab_resize) {
            if (resize_view.getVisibility() == View.GONE){
                resize_view.setVisibility(View.VISIBLE);
            }
            else {
                resize_view.setVisibility(View.GONE);
            }
        }
        else if (tab == R.id.tab_free_hand_crop){

        }
    }

    @Override
    public void onMenuItemReselect(@IdRes int itemid, int position) {
        int tab = bottomNavigation.getMenuItemId(position);

        if (tab == R.id.tab_undo) {

        }
        else if(tab == R.id.tab_resize) {
            if (resize_view.getVisibility() == View.GONE){
                resize_view.setVisibility(View.VISIBLE);
            }
            else {
                resize_view.setVisibility(View.GONE);
            }
        }
        else if (tab == R.id.tab_free_hand_crop){

        }

    }
}
