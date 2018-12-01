package com.virtualrobe.virtualrobe.virtualrobe_app.help;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.virtualrobe.virtualrobe.virtualrobe_app.CommentsActivity;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;


public class helpActivity extends FragmentActivity {
    RelativeLayout contentroot;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;
    public static final String ARG_DRAWING_START_LOCATION = "arg_drawing_start_location";
    private int drawingStartLocation;
    Button back;
    Bundle bundles;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.help);

        Intent getbundle = getIntent();
        String bundle = getbundle.getStringExtra("welcome");

        contentroot = (RelativeLayout) findViewById(R.id.contentRoot);
        // tab slider
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(sectionsPagerAdapter);

        back = (Button)findViewById(R.id.back);

        drawingStartLocation = getIntent().getIntExtra(ARG_DRAWING_START_LOCATION, 0);
        if (savedInstanceState == null) {
            contentroot.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    contentroot.getViewTreeObserver().removeOnPreDrawListener(this);
                    startIntroAnimation();
                    return true;
                }
            });

        }
        if (bundle!=null && bundle.equals("welcome")){
            bundles = new Bundle();
            bundles.putString("welcome", bundle);
            viewPager.setAdapter(sectionsPagerAdapter);
            viewPager.setCurrentItem(1, false);
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void startIntroAnimation() {
        contentroot.setScaleY(0.1f);
        contentroot.setPivotY(drawingStartLocation);

        contentroot.animate()
                .scaleY(1)
                .setDuration(200)
                .setInterpolator(new AccelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animateContent();
                    }
                })
                .start();
    }

    private void animateContent() {

    }

    @Override
    public void onBackPressed() {
        contentroot.animate()
                .translationY(helpActivity.Utils.getScreenHeight(this))
                .setDuration(200)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        helpActivity.super.onBackPressed();
                        overridePendingTransition(0, 0);
                    }
                })
                .start();
    }

    private static class Utils {
        private static int screenWidth = 0;
        private static int screenHeight = 0;

        public static int dpToPx(int dp) {
            return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
        }

        public static int getScreenHeight(Context c) {
            if (screenHeight == 0) {
                WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screenHeight = size.y;
            }

            return screenHeight;
        }

        public static int getScreenWidth(Context c) {
            if (screenWidth == 0) {
                WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
                Display display = wm.getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                screenWidth = size.x;
            }

            return screenWidth;
        }

        public static boolean isAndroid5() {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager childFragmentManager) {
            super(childFragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
            {
                // find first fragment...
               Tutorials ft1 = new Tutorials();
                return ft1;
            }
            else if (position == 1)
            {
                TermsAndPolicy ft3 = new TermsAndPolicy();
                ft3.setArguments(bundles);
                return ft3;
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Tutorial";
                case 1:
                    return "Terms & Policy";
            }
            return null;
        }
    }
}
