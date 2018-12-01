package com.virtualrobe.virtualrobe.virtualrobe_app.help;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.baoyz.widget.PullRefreshLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.virtualrobe.virtualrobe.virtualrobe_app.MainActivity;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Category_item_model;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.Clothes;
import com.virtualrobe.virtualrobe.virtualrobe_app.model.SquareImageView;
import com.virtualrobe.virtualrobe.virtualrobe_app.upload.Constants;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ClothesFragment extends Fragment {


    public ClothesFragment(){}

    MediaScannerConnection msConn;
    Bitmap bitmap;

    GridView gv;

    String page_name;
    String temp="";
    String username;
    PullRefreshLayout pullRefreshLayout;
    ProgressBar spinner;
    TextView add;
    private Context mContext;

    ArrayList<String> categories;
    ArrayList<String> img;
    ArrayList<String> full_img;
    ArrayList<Integer> laundry;
    ArrayList<String> description;
    URL url;
    String image;
    AsyncTask<String,Void,Void> displayclothes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootview = inflater.inflate(R.layout.fragment_clothes, container, false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();


       /*for (int i=0; i<clothesArrayLists.size(); i++){
            final DocumentReference alovelaceDocumentRef = db.collection("Users").document("tyQW422jBANqG9V9kw1A");
            final DocumentReference wardrobde = alovelaceDocumentRef.collection("Wardrobe").document("vHBki4QbCK0DkOG4z5Ux");
            final DocumentReference category_item = wardrobde.collection("Category_item").document();
            final Category_item_model model = new Category_item_model();
            model.setFull_image("https://s3-us-west-2.amazonaws.com/virtualrobewardrobe/baileywestyo/Shirts/"+clothesArrayLists.get(i).getFull_image());
            model.setThumbnail("https://s3-us-west-2.amazonaws.com/virtualrobethumbnail/baileywestyo/Shirts/"+clothesArrayLists.get(i).getImage_url());
            model.setDescription("");
            model.setBrand("");
            model.setColor("");
            //model.setLaundry_status(clothesArrayLists.get(i).getLaundry_status());
            db.runTransaction(new Transaction.Function<Void>() {
                @Nullable
                @Override
                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                    transaction.set(category_item,model);
                    return null;
                }
            });
    //}*/

        return rootview;
    }
}
