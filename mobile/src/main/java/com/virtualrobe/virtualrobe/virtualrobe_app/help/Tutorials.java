package com.virtualrobe.virtualrobe.virtualrobe_app.help;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.adapter.SlideAdapter;

import java.util.ArrayList;
import java.util.Collections;

import me.relex.circleindicator.CircleIndicator;


public class Tutorials extends Fragment {
    private static ViewPager mPager;
    private static int currentPage = 0;
    private static final Integer[] images= {R.drawable.add_to_wardrobe,R.drawable.profile,R.drawable.wardrobe,
            R.drawable.categories,R.drawable.laundry,R.drawable.help,R.drawable.logout};
    private ArrayList<Integer> TutorialArray = new ArrayList<Integer>();
    Button skip;

    public Tutorials(){}
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.tutorial, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        Collections.addAll(TutorialArray, images);

        mPager = (ViewPager) view.findViewById(R.id.pager);
        mPager.setAdapter(new SlideAdapter(getActivity(),TutorialArray));
        CircleIndicator indicator = (CircleIndicator) view.findViewById(R.id.indicator);
        indicator.setViewPager(mPager);

        // Auto start of viewpager
       /* final Handler handler = new Handler();
        final Runnable Update = new Runnable() {
            public void run() {
                if (currentPage == images.length) {
                    currentPage = 0;
                }
                mPager.setCurrentItem(currentPage++, true);
            }
        };
        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(Update);
            }
        }, 2500, 2500);*/

       //skip
        skip = (Button)view.findViewById(R.id.button2);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               getActivity().onBackPressed();
            }
        });

    }
}
