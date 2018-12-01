package com.virtualrobe.virtualrobe.virtualrobe_app.ShoppingMall.Adapters.Fragments;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.allattentionhere.fabulousfilter.AAH_FabulousFragment;
import com.google.android.flexbox.FlexboxLayout;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.ShoppingMall.Adapters.Util;

import java.util.ArrayList;
import java.util.List;


public class Filter extends AAH_FabulousFragment {
    ArrayMap<String, List<String>> applied_filters = new ArrayMap<>();
    List<TextView> textviews = new ArrayList<>();
    TabLayout tabs_types;
    private DisplayMetrics metrics;
    SectionsPagerAdapter mAdapter;

    public static Filter newInstance() {
        return new Filter();
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.filter_view, null);
        RelativeLayout rl_content = (RelativeLayout) contentView.findViewById(R.id.rl_content);
        LinearLayout ll_buttons = (LinearLayout) contentView.findViewById(R.id.ll_buttons);
        ViewPager vp_types = (ViewPager) contentView.findViewById(R.id.vp_types);
        tabs_types = (TabLayout) contentView.findViewById(R.id.tabs_types);

        /*close filter*/
        contentView.findViewById(R.id.imgbtn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeFilter("closed");
            }
        });

        mAdapter = new SectionsPagerAdapter();
        vp_types.setOffscreenPageLimit(4);
        vp_types.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        tabs_types.setupWithViewPager(vp_types);

        /*params to set*/
        setAnimationDuration(600); //optional; default 500ms
        setPeekHeight(300); // optional; default 400dp
        //setCallbacks((Callbacks) getActivity()); //optional; to get back result
        //setAnimationListener((AnimationListener) getActivity()); //optional; to get animation callbacks
        setViewgroupStatic(ll_buttons); // optional; layout to stick at bottom on slide
        setViewPager(vp_types); //optional; if you use viewpager that has scrollview
        setViewMain(rl_content); //necessary; main bottomsheet view
        setMainContentView(contentView); // necessary; call at end before super
        super.setupDialog(dialog, style); //call super at last

    }

    public class SectionsPagerAdapter extends PagerAdapter {
        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup collection, int position) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.view_filters_sorters, collection, false);
            FlexboxLayout fbl = (FlexboxLayout) layout.findViewById(R.id.fbl);

            /*inflate filter layouts*/
            switch (position) {
                case 0:
                    inflateLayoutWithFilters("Category", fbl);
                    break;
                case 1:
                    inflateLayoutWithFilters("Brand", fbl);
                    break;
                case 2:
                    inflateLayoutWithFilters("Price", fbl);
                    break;
                case 3:
                    inflateLayoutWithFilters("Color", fbl);
                    break;
            }
            collection.addView(layout);

            return layout;
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return false;
        }
    }

    private void inflateLayoutWithFilters(final String filter_category, FlexboxLayout fbl) {
        List<String> keys = new ArrayList<>();
        switch (filter_category) {
            case "Category":
                String page_name = Sections.PageName;
                if (page_name.equalsIgnoreCase("Men's Wear")) {
                    keys = Util.getMenWearcategory();
                }
                else if(page_name.equalsIgnoreCase("Men's Shoe")){
                    keys = Util.getMenShoescategory();
                }
                else if (page_name.equalsIgnoreCase("Men's Accessory")){
                    keys = Util.getMenAccesoriescategory();
                }
                else  if (page_name.equalsIgnoreCase("Women's Wear")) {
                    keys = Util.getWomenWearcategory();
                }
                else if (page_name.equalsIgnoreCase("Women's Shoe")){
                    keys = Util.getWomenShoescategrory();
                }
                else if (page_name.equalsIgnoreCase("Women's Accessory")){
                    keys = Util.getWomenAccessories();
                }
                else {
                    keys = Util.getWatchescategory();
                }
                break;
            case "Brand":
                keys = Util.getBrand();
                break;
            case "Price":
                keys = Util.getPrice();
                break;
            case "Color":
                keys = Util.getColor();
                break;
        }

        for (int i = 0; i < keys.size(); i++) {
            try {
                /*assign views*/
                View subchild = getActivity().getLayoutInflater().inflate(R.layout.single_chip, null);
                final TextView filter_title = ((TextView) subchild.findViewById(R.id.txt_title));
                filter_title.setText(keys.get(i));

                /*set up click event*/
                final int finalI = i;
                final List<String> finalKeys = keys;
                filter_title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (filter_title.getTag() != null && filter_title.getTag().equals("selected")) {
                            filter_title.setTag("unselected");
                            filter_title.setBackgroundResource(R.drawable.chip_unselected);
                            filter_title.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                            removeFromSelectedMap(filter_category, finalKeys.get(finalI));
                        } else {
                            filter_title.setTag("selected");
                            filter_title.setBackgroundResource(R.drawable.chip_selected);
                            filter_title.setTextColor(ContextCompat.getColor(getContext(), R.color.disabled_gray));
                            addToSelectedMap(filter_category, finalKeys.get(finalI));
                        }
                    }
                });

                if (applied_filters != null && applied_filters.get(filter_category) != null && applied_filters.get(filter_category).contains(keys.get(finalI))) {
                    filter_title.setTag("selected");
                    filter_title.setBackgroundResource(R.drawable.chip_selected);
                    filter_title.setTextColor(ContextCompat.getColor(getContext(), R.color.disabled_gray));
                } else {
                    filter_title.setBackgroundResource(R.drawable.chip_unselected);
                    filter_title.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
                }
                textviews.add(filter_title);

                fbl.addView(subchild);
            }
            catch (Exception e){
                Toast.makeText(getContext(),"Error"+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void addToSelectedMap(String key, String value) {
        if (applied_filters.get(key) != null && !applied_filters.get(key).contains(value)) {
            applied_filters.get(key).add(value);
        } else {
            List<String> temp = new ArrayList<>();
            temp.add(value);
            applied_filters.put(key, temp);
        }
    }

    private void removeFromSelectedMap(String key, String value) {
        if (applied_filters.get(key).size() == 1) {
            applied_filters.remove(key);
        } else {
            applied_filters.get(key).remove(value);
        }
    }
}
