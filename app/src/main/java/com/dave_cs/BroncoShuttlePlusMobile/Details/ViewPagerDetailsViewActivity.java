package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import com.dave_cs.BroncoShuttlePlusMobile.R;

/**
 * Created by David on 3/5/2016.
 */
public class ViewPagerDetailsViewActivity extends FragmentActivity {

    private static final int NUM_PAGES = 3;

    private ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_view_view_pager);

        final ActionBar bar = getActionBar();


        mViewPager = (ViewPager) findViewById(R.id.details_pager);
        mPagerAdapter = new DetailsPageAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mPagerAdapter);
    }
}
