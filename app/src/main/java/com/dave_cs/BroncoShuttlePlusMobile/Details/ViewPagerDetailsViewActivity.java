package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dave_cs.BroncoShuttlePlusMobile.Details.Advanced.DetailsAdvActivity;
import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.NearestStopAdapter;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopLocation;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by David on 3/5/2016.
 */
public class ViewPagerDetailsViewActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private static final int NUM_PAGES = 3;

    private ViewPager mViewPager;
    private DetailsPageAdapter mPagerAdapter;

    private Location currLocation;
    private LocationManager locationManager;
    private boolean processing = false;
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("<Location>: ", "received!");
            currLocation = location;
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_view_view_pager);

        handleIntent(getIntent());
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mViewPager = (ViewPager) findViewById(R.id.details_pager);
        mPagerAdapter = new DetailsPageAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mPagerAdapter);

        Toolbar toolBar = (Toolbar) findViewById(R.id.bar);

        setSupportActionBar(toolBar);

        getSupportActionBar().setDisplayShowCustomEnabled(true);
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nearest:
                //do thing!
                Log.d("<Selection>: ", "Nearest selected!");
                if (processing) return false;
                else processing = true;
                currLocation = null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                final Thread locationReq = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (currLocation == null) {
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (ActivityCompat.checkSelfPermission(ViewPagerDetailsViewActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ViewPagerDetailsViewActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                locationManager.removeUpdates(locationListener);
                            }
                        });
                    }
                });
                locationReq.start();

                if (((DetailsStopFragmentTab) mPagerAdapter.getItem(1)).ready) {
                    final ArrayList<StopLocation> stopList = ((DetailsStopFragmentTab) mPagerAdapter.getItem(1)).getStopLocations();

                    Thread listUpdate = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (stopList.size() != ((DetailsStopFragmentTab) mPagerAdapter.getItem(1)).getStopCount() || currLocation == null) {
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if (ActivityCompat.checkSelfPermission(ViewPagerDetailsViewActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(ViewPagerDetailsViewActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        // TODO: Consider calling
                                        //    ActivityCompat#requestPermissions
                                        // here to request the missing permissions, and then overriding
                                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                        //                                          int[] grantResults)
                                        // to handle the case where the user grants the permission. See the documentation
                                        // for ActivityCompat#requestPermissions for more details.
                                        return;
                                    }

                                    ArrayList<StopLocation> inRangeList = new ArrayList<>();
                                    if (currLocation != null) {
                                        for (StopLocation s : stopList) {
                                            Location location = new Location(LocationManager.GPS_PROVIDER);
                                            location.setLatitude(s.getLat());
                                            location.setLongitude(s.getLng());

                                            if (currLocation.distanceTo(location) <= 2000) {
                                                s.setDist(currLocation.distanceTo(location));
                                                s.setBearing(currLocation.bearingTo(location));
                                                inRangeList.add(s);
                                            }

                                        }
                                    }

                                    NearestStopAdapter adapter = new NearestStopAdapter(ViewPagerDetailsViewActivity.this, inRangeList, currLocation);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewPagerDetailsViewActivity.this);
                                    builder.setView(R.layout.dialog_nearest);
                                    final AlertDialog dialog = builder.create();
                                    dialog.show();
                                    processing = false;
                                    final ListView listView = (ListView) dialog.findViewById(R.id.nearest_stops_list);
                                    if (inRangeList.isEmpty()) {
                                        TextView nothing = new TextView(ViewPagerDetailsViewActivity.this);
                                        nothing.setText(R.string.too_far_text);
                                        nothing.setAllCaps(true);
                                        nothing.setTextColor(Color.parseColor("#555555"));
                                        nothing.setTextSize(20);
                                        nothing.setPadding(10, 10, 10, 10);
                                        listView.addFooterView(nothing);
                                    }
                                    listView.setAdapter(adapter);
                                    adapter.sort(new Comparator<StopLocation>() {
                                        @Override
                                        public int compare(StopLocation lhs, StopLocation rhs) {
                                            float diff = lhs.getDist() - rhs.getDist();
                                            if (Math.abs(diff) < 1)
                                                return (diff < 0) ? -1 : 1;
                                            else return Math.round(diff);
                                        }
                                    });
                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            Intent intent = new Intent(ViewPagerDetailsViewActivity.this, DetailsAdvActivity.class);
                                            intent.putExtra("stopName", ((StopLocation) listView.getItemAtPosition(position)).getName());
                                            intent.putExtra("stopNumber", ((StopLocation) listView.getItemAtPosition(position)).getStopNumber());
                                            dialog.dismiss();
                                            startActivity(intent);
                                        }
                                    });
                                }
                            });
                        }
                    });
                    listUpdate.start();

                    return true;
                } else {
                    Toast.makeText(ViewPagerDetailsViewActivity.this, "Not ready!", Toast.LENGTH_SHORT).show();
                    processing = false;
                    return false;
                }
            default:
                return super.onOptionsItemSelected(menuItem);
        }
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
                    bar.setIcon(R.drawable.ic_details_view_icon);
                    break;
                case 1:
                    bar.setTitle("Stops");
                    bar.setIcon(R.drawable.ic_bus_stop_icon);
                    break;
                case 2:
                    bar.setTitle("Shuttles");
                    bar.setIcon(R.drawable.ic_live_map_draw_icon);
                    break;
            }
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }


    }
}
