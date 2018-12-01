package com.virtualrobe.virtualrobe.virtualrobe_app.ShoppingMall.Adapters.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.ShoppingMall.Adapters.SectionAdapter;
import com.virtualrobe.virtualrobe.virtualrobe_app.ShoppingMall.Adapters.View_itemDetails;
import com.virtualrobe.virtualrobe.virtualrobe_app.TypefaceSpan;

import org.w3c.dom.Document;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Sections extends Fragment implements SectionAdapter.ItemListener {
    public static String Categoryname="category";
    public static String PageName;

    @BindView(R.id.page_text)
    TextView header_name;

    @BindView(R.id.recycler_listing)
    RecyclerView recyclerView;

    @BindView(R.id.view_empty)
    LinearLayout mEmptyView;

    @BindView(R.id.fab2)
    FloatingActionButton filter_fab;

    private FirebaseFirestore mFirestore;
    private DocumentReference store;
    private DocumentReference category;
    private Query items;
    SectionAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mall_sections, container, false);
        ButterKnife.bind(this,view);
        Bundle args = getArguments();
        if (args != null)
        {
            String page_name = args.getString(Categoryname);
             /*Set Category name and font type*/
             if (page_name!=null) {
                 PageName = page_name;
                 SpannableString s = new SpannableString(page_name);
                 s.setSpan(new TypefaceSpan(getActivity(), "LobsterTwo-Bold.ttf"), 0, s.length(),
                         Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                 header_name.setText(s);

                 // Initialize Firestore
                 mFirestore = FirebaseFirestore.getInstance();

                 // Get reference to Store
                 if (page_name.equalsIgnoreCase("Men's Wear")||
                         page_name.equalsIgnoreCase("Men's Shoe") || page_name.equalsIgnoreCase("Men's Accessory")){
                     store = mFirestore.collection("Store").document("Men");

                     //get reference to category
                     if (page_name.equalsIgnoreCase("Men's Wear")) {
                         category = store.collection("Men_Category").document("wear");
                     }
                     else if (page_name.equalsIgnoreCase("Men's Shoe")){
                         category = store.collection("Men_Category").document("shoes");
                     }
                     else {
                         category = store.collection("Men_Category").document("Accessories");
                     }
                 }
                 else if (page_name.equalsIgnoreCase("Women's Wear") || page_name.equalsIgnoreCase("Women's Shoe") ||
                         page_name.equalsIgnoreCase("Women's Accessory")){
                     store = mFirestore.collection("Store").document("Women");

                     //get reference to category
                     if (page_name.equalsIgnoreCase("Women's Wear")) {
                         category = store.collection("Women_Category").document("wear");
                     }
                     else if (page_name.equalsIgnoreCase("Women's Shoe")){
                         category = store.collection("Women_Category").document("shoes");
                     }
                     else {
                         category = store.collection("Women_Category").document("Accessories");
                     }
                 }
                 else {
                     store = mFirestore.collection("Store").document("watches");

                     //get reference to category
                     if (page_name.equalsIgnoreCase("Men")) {
                         category = store.collection("Watches_category").document("men");
                     }
                     else if (page_name.equalsIgnoreCase("Women")){
                         category = store.collection("Watches_category").document("women");
                     }
                     else {
                         category = store.collection("Watches_category").document("unisex");
                     }
                 }

                 //get reference to the items
                 items = category.collection("items");
                 adapter = new SectionAdapter(items,this){
                     @Override
                     protected void onDataChanged() {
                         if (getItemCount() == 0) {
                             recyclerView.setVisibility(View.GONE);
                             mEmptyView.setVisibility(View.VISIBLE);
                         } else {
                             recyclerView.setVisibility(View.VISIBLE);
                             mEmptyView.setVisibility(View.GONE);
                         }
                     }
                 };

                 recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),2));
                 recyclerView.setAdapter(adapter);
             }
        }

        return view;
    }

    @OnClick(R.id.fab2)
    public void View_Filter(View view){
        Filter dialog = Filter.newInstance();
        dialog.setParentFab(filter_fab);
        dialog.show(getActivity().getSupportFragmentManager(), dialog.getTag());
    }

    @Override
    public void itemSelected(DocumentSnapshot snapshot) {
        startActivity(new Intent(getActivity(), View_itemDetails.class));
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
