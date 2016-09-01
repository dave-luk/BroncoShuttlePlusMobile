package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.dave_cs.BroncoShuttlePlusMobile.R;

import java.util.ArrayList;

/**
 * Created by David on 3/5/2016.
 */
public class ViewPagerDetailsViewActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final int NUM_PAGES = 3;

    private ViewPager mViewPager;
    private DetailsPageAdapter mPagerAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_view_view_pager);

        handleIntent(getIntent());

        mViewPager = (ViewPager) findViewById(R.id.details_pager);
        mPagerAdapter = new DetailsPageAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mPagerAdapter);

        Toolbar toolBar = (Toolbar) findViewById(R.id.bar);

        setSupportActionBar(toolBar);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            ((Filterable) mPagerAdapter.getItem(mViewPager.getCurrentItem())).filter(query);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.search);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (searchView != null) {
            Log.d("<Search info>: ", "have info: " + searchView.getQuery());
            searchView.setOnQueryTextListener(this);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        ((Filterable) mPagerAdapter.getItem(mViewPager.getCurrentItem())).filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.isEmpty())
            ((Filterable) mPagerAdapter.getItem(mViewPager.getCurrentItem())).clear();
        else
            ((Filterable) mPagerAdapter.getItem(mViewPager.getCurrentItem())).filter(newText);
        return true;
    }

    public class DetailsPageAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragmentList = new ArrayList<>();

        public DetailsPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            if (fragmentList.isEmpty()) {
                fragmentList.add(new DetailsRouteFragmentTab());
                fragmentList.add(new DetailsStopFragmentTab());
                fragmentList.add(new DetailsBusFragmentTab());
            }

            return fragmentList.get(position);
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
