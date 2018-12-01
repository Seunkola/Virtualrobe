package com.virtualrobe.virtualrobe.virtualrobe_app.Styling_board.menu;


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.virtualrobe.virtualrobe.virtualrobe_app.Drag.DragLayer;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.Utility.color_picker_dialog;
import com.virtualrobe.virtualrobe.virtualrobe_app.sticker.view.stickerdemo.view.StickerView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Outfit_board_Fragment extends Fragment implements color_picker_dialog.OnCompleteListener {
    View rootView;

    @BindView(R.id.rview)
    RelativeLayout main_layout;

    @BindView(R.id.contentRoot)
    FrameLayout styling_layout;

    @BindView(R.id.drag_layer)
    DragLayer dragLayer;

    @BindView(R.id.imgView)
    ImageView Shirt_image;

    @BindView(R.id.imgView2)
    ImageView Bottom_image;

    ImageView Shopping_item;
    private StickerView mCurrentView;
    private String img_url;
    private AsyncTask<Void, Void, Void> image_loading;
    private String IMAGEVIEW_TAG;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.outfit_board_fragment, container, false);
        ButterKnife.bind(this,rootView);

        /*close image editing on screen touch*/
        rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCurrentView != null) {
                    mCurrentView.setInEdit(false);
                }
            }
        });

        /*retrieve clothe details*/
        try {
            SharedPreferences preferences = getActivity().getSharedPreferences("Cloth_details", Context.MODE_PRIVATE);
            /*get shirt information*/
            String shirt_url = preferences.getString("Shirts_url","");
            String shirt_id = preferences.getString("Shirts_id","");
            if (!shirt_url.equals("")){
                load_image(shirt_url,"shirts_id");
            }

            /*get Bottoms information*/
            String bottom_url = preferences.getString("Bottoms_url","");
            String bottom_id = preferences.getString("Bottoms_id","");
            if(!bottom_url.equals("")){
                load_image(bottom_url,"bottoms_id");
            }

            /*get specs information*/
            String specs_url = preferences.getString("specs_url","");
            String specs_id = preferences.getString("specs_id","");
            if (!specs_url.equals("")){
                load_image(specs_url,"specs_id");
            }

            /*get shoes information*/
            String shoes_url = preferences.getString("Shoes_url","");
            String shoes_id = preferences.getString("Shoes_id","");
            if (!shoes_url.equals("")){
                load_image(shoes_url,"shoes_id");
            }

            /*get Accessories*/
            String accessories_url = preferences.getString("Accessories_url","");
            String accessories_id = preferences.getString("Accessories_id","");
            if (!accessories_url.equals("")){
                load_image(accessories_url,"accessories_id");
            }

            /*get caps*/
            String caps_url = preferences.getString("caps_url","");
            String caps_id = preferences.getString("caps_id","");
            if (!caps_url.equals("")){
                load_image(caps_url,"caps_id");
            }

            /*get shopping item*/
            String item_url = preferences.getString("item_url","");
            String item_id = preferences.getString("item_id","");
            if (!item_url.equals("")){
                load_image(item_url,"item_id");
            }

        }
        catch (Exception e){
            Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
        }

        //set background color
        SharedPreferences friend_name = getActivity().getSharedPreferences("background_color",Context.MODE_PRIVATE);
        int color = friend_name.getInt("background",0);
        if (color!=0) {
            rootView.setBackgroundColor(color);
            dragLayer.setBackgroundColor(color);
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                rootView.setBackgroundColor(getResources().getColor(R.color.white,getActivity().getTheme()));
            }
            else {
                rootView.setBackgroundColor(getResources().getColor(R.color.white));
            }
        }


        return rootView;
    }

    @SuppressLint("StaticFieldLeak")
    public void load_image(final String img_urls, final String tag){
        image_loading = new AsyncTask<Void,Void,Void>(){
            Bitmap image;
            ProgressDialog progressDialog;
            @Override
            protected Void doInBackground(Void... params) {
                URL url = null;
                try {
                    url = new URL(img_urls);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                try {
                    if (url != null) {
                        image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPreExecute() {
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Retrieving"+tag);
                progressDialog.show();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressDialog.dismiss();
                final StickerView stickerView = new StickerView(getActivity());
                if (image != null) {
                    stickerView.setBitmap(image);
                    stickerView.setTag(tag);
                    stickerView.setOperationListener(new StickerView.OperationListener() {
                        @Override
                        public void onDeleteClick() {
                            img_url = "";
                            main_layout.removeView(stickerView);
                            SharedPreferences Shirts_url = getActivity().getSharedPreferences("Cloth_url", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = Shirts_url.edit();
                            editor.putString(tag, "");
                            editor.apply();
                        }

                        @Override
                        public void onEdit(StickerView stickerView) {
                            setCurrentEdit(stickerView);
                        }

                        @Override
                        public void onTop(StickerView stickerView) {

                        }
                    });
                    RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                    main_layout.addView(stickerView, lp);
                }
            }
        }.execute();
    }

    private void setCurrentEdit(StickerView stickerView) {
        if (mCurrentView != null) {
            mCurrentView.setInEdit(false);
        }
        mCurrentView = stickerView;
        stickerView.setInEdit(true);
    }

    @Override
    public void onComplete(int color) {
        Toast.makeText(getActivity(),"Background has been changed successfully",Toast.LENGTH_SHORT).show();
        if (color!=0){
            rootView.setBackgroundColor(color);
        }
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    rootView.setBackgroundColor(getResources().getColor(R.color.white, getActivity().getTheme()));
                }
                catch (Exception e){}
            }
            else {
                rootView.setBackgroundColor(getResources().getColor(R.color.white));
            }
        }
    }
}
