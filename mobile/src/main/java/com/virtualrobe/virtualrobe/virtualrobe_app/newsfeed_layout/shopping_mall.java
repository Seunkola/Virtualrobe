package com.virtualrobe.virtualrobe.virtualrobe_app.newsfeed_layout;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.ShoppingMall.Adapters.Fragments.Sections;
import com.virtualrobe.virtualrobe.virtualrobe_app.ShoppingMall.Adapters.MallSections;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class shopping_mall extends Fragment implements MallSections.SectionListener {
    public shopping_mall(){
    }

    @BindView(R.id.fashion_recycleview)
    RecyclerView Fashion_recycleview;

    private SectionedRecyclerViewAdapter sectionAdapter,sectionAdapter2 ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mall, container, false);
        ButterKnife.bind(this,view);

        sectionAdapter = new SectionedRecyclerViewAdapter();
        sectionAdapter2 = new SectionedRecyclerViewAdapter();

        /*set image background for men fashion*/
        ArrayList<String> men_fashion_images = new ArrayList<>();
        men_fashion_images.add("http://www.virtualrobe.com/wardrobe/shirts.jpg");
        men_fashion_images.add("http://www.virtualrobe.com/wardrobe/shoes.jpg");
        men_fashion_images.add("http://www.virtualrobe.com/wardrobe/men_accessories.jpg");

        ArrayList<String> men_fashion_categories = new ArrayList<>();
        men_fashion_categories.add("Men's Wear");
        men_fashion_categories.add("Men's Shoe");
        men_fashion_categories.add("Men's Accessory");

        /*set image background for women fashion */
        ArrayList<String> women_fashion_images = new ArrayList<>();
        women_fashion_images.add("http://www.virtualrobe.com/wardrobe/women_wear.jpg");
        women_fashion_images.add("http://www.virtualrobe.com/wardrobe/shoes.jpg");
        women_fashion_images.add("http://www.virtualrobe.com/wardrobe/women_accessories.jpg");

        ArrayList<String> women_fashion_categories = new ArrayList<>();
        women_fashion_categories.add("Women's Wear");
        women_fashion_categories.add("Women's Shoe");
        women_fashion_categories.add("Women's Accessory");

        /*set image background for watches */
        ArrayList<String> watches_images = new ArrayList<>();
        watches_images.add("http://www.virtualrobe.com/wardrobe/watches.jpg");
        watches_images.add("http://www.virtualrobe.com/wardrobe/women_watches.jpg");
        watches_images.add("http://www.virtualrobe.com/wardrobe/watches.jpg");

        ArrayList<String> watches_categories = new ArrayList<>();
        watches_categories.add("Men");
        watches_categories.add("Women");
        watches_categories.add("Unisex");

        sectionAdapter.addSection(new MallSections("Men's Fashion",men_fashion_images,men_fashion_categories,getActivity(),this));
        sectionAdapter.addSection(new MallSections("Women's Fashion",women_fashion_images,women_fashion_categories,getActivity(),this));
        sectionAdapter.addSection(new MallSections("Watches",watches_images,watches_categories,getActivity(),this));

        GridLayoutManager glm = new GridLayoutManager(getContext(), 3);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch(sectionAdapter.getSectionItemViewType(position)) {
                    case SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER:
                        return 3;
                    default:
                        return 1;
                }
            }
        });
        Fashion_recycleview.setLayoutManager(glm);
        Fashion_recycleview.setAdapter(sectionAdapter);

        return view;
    }

    @Override
    public void onSectionselected(String category) {
        Fragment fragment = new Sections();
        Bundle bundle = new Bundle();
        bundle.putString(Sections.Categoryname,category);
        try {
            fragment.setArguments(bundle);
            FragmentManager fragmentManager = getFragmentManager();
            if (fragmentManager != null) {
                fragmentManager.beginTransaction().replace(R.id.flContent,fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).addToBackStack("").commit();
            }
        }
        catch (Exception e){

        }

    }
}
