package com.virtualrobe.virtualrobe.virtualrobe_app.freehand_image_cropper;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.virtualrobe.virtualrobe.virtualrobe_app.Activity_upload.UploadActivity;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class CropView extends View implements View.OnTouchListener {
    private Paint paint;
    public static List<Point> points;
    int DIST = 2;
    boolean flgPathDraw = true;

    Point mfirstpoint = null;
    boolean bfirstpoint = false;

    Point mlastpoint = null;
    Bitmap bitmap;
    Context mContext;
    String bitmap_filename;
    int bool = 0;
    public static float variable_X,variable_Y = 0;
    String userId,categoryId,category_name;

    public CropView(Context c, Bitmap bitmap, String filename, String userId, String categoryId, String category_name) {
        super(c);
        this.bitmap = bitmap;
        bitmap_filename = filename;
        this.userId = userId;
        this.categoryId = categoryId;
        this.category_name = category_name;
        mContext = c;
        setFocusable(true);
        setFocusableInTouchMode(true);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setPathEffect(new DashPathEffect(new float[] { 10, 20 }, 0));
        paint.setStrokeWidth(5);
        paint.setColor(Color.WHITE);

        this.setOnTouchListener(this);
        points = new ArrayList<Point>();

        bfirstpoint = false;

    }

    public CropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setFocusable(true);
        setFocusableInTouchMode(true);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);

        this.setOnTouchListener(this);
        points = new ArrayList<Point>();
        bfirstpoint = false;

    }

    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap,variable_X,variable_Y, null);

        Path path = new Path();
        boolean first = true;

        for (int i = 0; i < points.size(); i += 2) {
            Point point = points.get(i);
            if (first) {
                first = false;
                path.moveTo(point.x, point.y);
            } else if (i < points.size() - 1) {
                Point next = points.get(i + 1);
                path.quadTo(point.x, point.y, next.x, next.y);
            } else {
                mlastpoint = points.get(i);
                path.lineTo(point.x, point.y);
            }
        }
        canvas.drawPath(path, paint);
    }

    public boolean onTouch(View view, MotionEvent event) {
        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();

        if (bool == 1) {
        if (flgPathDraw) {

            if (bfirstpoint) {

                if (comparepoint(mfirstpoint, point)) {
                    points.add(mfirstpoint);
                    flgPathDraw = false;
                    showcropdialog();
                } else {
                    points.add(point);
                }
            } else {
                points.add(point);
            }

            if (!(bfirstpoint)) {

                mfirstpoint = point;
                bfirstpoint = true;
            }
        }

        invalidate();

        if (event.getAction() == MotionEvent.ACTION_UP) {

                mlastpoint = point;
                if (flgPathDraw) {
                    if (points.size() > 12) {
                        if (!comparepoint(mfirstpoint, mlastpoint)) {
                            flgPathDraw = false;
                            points.add(mfirstpoint);
                            showcropdialog();
                        }
                    }
                }
            }

        }
        else {
            Snackbar.make(this, "Move image to preferred spot before cropping", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    variable_X = event.getX();
                    variable_Y = event.getY();
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    variable_X = event.getX();
                    variable_Y = event.getY();
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    bool =1;
                    break;
            }

        }
        return true;
    }

    private boolean comparepoint(Point first, Point current) {
        int left_range_x = (int) (current.x - 3);
        int left_range_y = (int) (current.y - 3);

        int right_range_x = (int) (current.x + 3);
        int right_range_y = (int) (current.y + 3);

        if ((left_range_x < first.x && first.x < right_range_x)
                && (left_range_y < first.y && first.y < right_range_y)) {
            if (points.size() < 10) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    private void showcropdialog() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        intent = new Intent(mContext, ImageCropActivity.class);
                        intent.putExtra("crop", true);
                        intent.putExtra("Image", bitmap_filename);
                        intent.putExtra(UploadActivity.KEY_USER_ID,userId);
                        intent.putExtra(UploadActivity.KEY_CATEGORY_ID,categoryId);
                        intent.putExtra(UploadActivity.KEY_CATEGORY_NAME,category_name);
                        mContext.startActivity(intent);
                        ((Activity) mContext).finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        intent = new Intent(mContext, ImageCropActivity.class);
                        intent.putExtra("crop", false);
                        intent.putExtra("Image", bitmap_filename);
                        intent.putExtra(UploadActivity.KEY_USER_ID,userId);
                        intent.putExtra(UploadActivity.KEY_CATEGORY_ID,categoryId);
                        intent.putExtra(UploadActivity.KEY_CATEGORY_NAME,category_name);
                        mContext.startActivity(intent);
                        bfirstpoint = false;
                        ((Activity) mContext).finish();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Do you Want to save Crop or Non-crop image?")
                .setPositiveButton("Crop", dialogClickListener)
                .setNegativeButton("Non-crop", dialogClickListener).show()
                .setCancelable(false);
    }



}

