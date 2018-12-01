package com.virtualrobe.virtualrobe.virtualrobe_app.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by seunk on 9/20/2017.
 */

public class SlideAdapter extends PagerAdapter {
    private ArrayList<Integer> images;
    private LayoutInflater inflater;
    private Context context;

    public SlideAdapter(Context context, ArrayList<Integer> images) {
        this.context = context;
        this.images=images;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {
        View myImageLayout = inflater.inflate(R.layout.slide, view, false);
        ImageView myImage = (ImageView) myImageLayout
                .findViewById(R.id.image);
        //myImage.setImageResource(images.get(position));
        Picasso.with(context)
                .load(images.get(position))
                .into(myImage);

        view.addView(myImageLayout, 0);
        return myImageLayout;
    }

}
