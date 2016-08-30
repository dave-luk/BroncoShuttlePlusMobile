package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.dave_cs.BroncoShuttlePlusMobile.R;

/**
 * Created by David on 3/5/2016.
 */
public class ViewPagerDetailsViewActivity extends AppCompatActivity {

    private static final int NUM_PAGES = 3;

    private ViewPager mViewPager;
    private DetailsPageAdapter mPagerAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_view_view_pager);

        mViewPager = (ViewPager) findViewById(R.id.details_pager);
        mPagerAdapter = new DetailsPageAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mPagerAdapter);

        Toolbar toolBar = (Toolbar) findViewById(R.id.bar);

        setSupportActionBar(toolBar);

    }

    public class DetailsPageAdapter extends FragmentPagerAdapter {
        public DetailsPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new DetailsRouteFragmentTab();
                case 1:
                    return new DetailsStopFragmentTab();
                case 2:
                    return new DetailsBusFragmentTab();
                default:
                    return null;
            }
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            android.support.v7.app.ActionBar bar = getSupportActionBar();
            switch (position) {
                case 0:
                    bar.setTitle("Routes");
                    break;
                case 1:
                    bar.setTitle("Stops");
                    break;
                case 2:
                    bar.setTitle("Shuttles");
                    break;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }


    }

}
