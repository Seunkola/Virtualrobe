package com.virtualrobe.virtualrobe.virtualrobe_app.Search;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.rahimlis.badgedtablayout.BadgedTabLayout;
import com.virtualrobe.virtualrobe.virtualrobe_app.R;
import com.virtualrobe.virtualrobe.virtualrobe_app.Search.Fragments.Shopping_SearchResults;
import com.virtualrobe.virtualrobe.virtualrobe_app.Search.Fragments.Outfits_SearchResults;
import com.virtualrobe.virtualrobe.virtualrobe_app.Search.Fragments.Users_SearchResults;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Search_Activity extends FragmentActivity {
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.tabs)
    BadgedTabLayout tabLayout;

    private int[] tabIcons = {
            R.drawable.ic_profile,
            R.drawable.ic_outfit,
            R.drawable.ic_shopping
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);
        ButterKnife.bind(this);

        final Intent queryIntent = getIntent();
        String queryString = queryIntent.getStringExtra(SearchManager.QUERY).replace(" ","");
        Bundle bundle = new Bundle();
        bundle.putString("search",queryString);

        setupViewPager(viewPager,bundle);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();
    }
    private void setupTabIcons() {
        try {
            tabLayout.setIcon(0,tabIcons[0]);
            tabLayout.setIcon(1,tabIcons[1]);
            tabLayout.setIcon(2,tabIcons[2]);
        }
        catch (Exception e){
        }
    }

    private void setupViewPager(ViewPager viewPager,Bundle bundle) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(),bundle);
        adapter.addFragment(new Users_SearchResults(), "Users");
        adapter.addFragment(new Outfits_SearchResults(), "Outfits");
        adapter.addFragment(new Shopping_SearchResults(), "Shopping Mall");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        private final Bundle fragmentBundle;

        public ViewPagerAdapter(FragmentManager manager, Bundle bundle) {
            super(manager);
            fragmentBundle = bundle;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragment.setArguments(fragmentBundle);
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
