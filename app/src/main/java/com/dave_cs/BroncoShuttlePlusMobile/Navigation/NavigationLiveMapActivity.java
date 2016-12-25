package com.dave_cs.BroncoShuttlePlusMobile.Navigation;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dave_cs.BroncoShuttlePlusMobile.Application.Data.LiveMapData;
import com.dave_cs.BroncoShuttlePlusMobile.Application.DataUpdateApplication;
import com.dave_cs.BroncoShuttlePlusMobile.LiveMaps.LiveMapsActivity;
import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.LocationService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.NearestStopAdapter;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Observable;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by David on 12/23/2016.
 */

public class NavigationLiveMapActivity extends LiveMapsActivity {

    private static final String TAG = "NavLiveMapActivity";

    //This is in meters.
    private static final int IN_RANGE_LIMIT = 2000;

    private Location currLocation;
    private LocationManager locationManager;
    private boolean processing = false;
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "location received!");
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
    protected void onCreateLayout() {
        setContentView(R.layout.activity_navigation_live_maps);

        ButterKnife.bind(this);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (((DataUpdateApplication) getApplication()).liveMapData.liveMapStaticRoutePackages.isEmpty()) {
            Log.i(TAG, "empty!");
            error = true;
        } else {
            staticRoutePackages = ((DataUpdateApplication) getApplication()).liveMapData.liveMapStaticRoutePackages;
            parseStaticPackages();
            error = false;
        }

        if (!((DataUpdateApplication) getApplication()).liveMapData.liveMapDynamicRoutePackages.isEmpty()) {
            dynamicRoutePackages = ((DataUpdateApplication) getApplication()).liveMapData.liveMapDynamicRoutePackages;
            dynamicDataReady = true;
        } else {
            ((DataUpdateApplication) getApplication()).liveMapData.requestUpdate(-1, 0);
        }

        //setup bottom sheet
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(true);
        bottomSheet.post(new Runnable() {
            @Override
            public void run() {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setSupportActionBar((Toolbar) findViewById(R.id.nav_action_bar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowCustomEnabled(true);
        }
    }

    @Override
    public void toggleRouteVisible(int index, boolean state) {
        //do nothing: cannot turn off routes.
    }

    private void showRoutes() {

        for (int index = 0; index < routeIDs.size(); index++) {
            for (LatLng point : masterPolyList.get(index)) {
                masterRouteOptions.get(index).add(point);
            }

            mMap.addPolyline(masterRouteOptions.get(index).color(Color.parseColor(routeColors[index])));

            for (int i = 0; i < dynamicRoutePackages.get(index).stops.size(); i++) {
                initMarker(dynamicRoutePackages.get(index).stops.get(i), staticRoutePackages.get(index).stops.get(i).loc.parseLatLng(), "stop", index, i);
            }

            updateBusLocation(index);

            updateHandler.post(updateRunnable);

            if (stopBoundsCounter > 0) {
                LatLngBounds stopBounds = stopBoundsBuilder.build();
                pan(stopBounds);
            } else if (mMap != null) {
                LatLng cpp = new LatLng(34.056781, -117.821071);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cpp, 14.5f));
            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        staticRoutePackages = savedInstanceState.getParcelableArrayList("StaticPackages");
        dynamicRoutePackages = savedInstanceState.getParcelableArrayList("DynamicPackages");
        parseStaticPackages();
        dynamicDataReady = true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(34.056781, -117.821071), 14.5f));
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        mMap.setMyLocationEnabled(true);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (!error) {
                    bottomSheet.post(new Runnable() {
                        @Override
                        public void run() {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            bottomSheetBehavior.setPeekHeight(0);
                        }
                    });
                }
            }
        });
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //adding new object info into drawer
                setInfoView(marker);
                //show drawer
                bottomSheet.post(new Runnable() {
                    @Override
                    public void run() {
                        bottomSheetBehavior.setPeekHeight(200);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                });
                return true;
            }
        });

        if (dynamicDataReady) {
            showRoutes();
        } else if (error) {
            displayError();
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.i(TAG, "Updating");
        if (observable instanceof LiveMapData) {
            if (((LiveMapData) observable).liveMapDynamicRoutePackages.size() == staticRoutePackages.size()) {
                dynamicRoutePackages = ((LiveMapData) observable).liveMapDynamicRoutePackages;
                dynamicDataReady = true;
                showRoutes();
            }
            if (staticRoutePackages.isEmpty()) {
                staticRoutePackages = ((LiveMapData) observable).liveMapStaticRoutePackages;
                parseStaticPackages();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.navigation_options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /*TODO: revamp this
      TODO: add some sort of visual hint for loading.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.nearest:
                Log.i(TAG, "Nearest selected!");

                if (processing) {
                    return false;
                } else {
                    processing = true;
                }
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
                                if (ActivityCompat.checkSelfPermission(NavigationLiveMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NavigationLiveMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

                if (!DataUpdateApplication.getInstance().detailsViewData.detailsViewStopData.stopInfoList.isEmpty()) {
                    final List<StopInfo> stopInfoList = DataUpdateApplication.getInstance().detailsViewData.detailsViewStopData.stopInfoList;
                    final ArrayList<StopLocation> stopList = getStopLocations(stopInfoList);

                    Thread listUpdate = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            //TODO: bad thing to do...
                            while (stopList.size() != stopInfoList.size() || currLocation == null) {
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (ActivityCompat.checkSelfPermission(NavigationLiveMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NavigationLiveMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

                                            if (currLocation.distanceTo(location) <= IN_RANGE_LIMIT) {
                                                s.setDist(currLocation.distanceTo(location));
                                                s.setBearing(currLocation.bearingTo(location));
                                                inRangeList.add(s);
                                            }

                                        }
                                    }

                                    NearestStopAdapter adapter = new NearestStopAdapter(NavigationLiveMapActivity.this, inRangeList, currLocation);

                                    AlertDialog.Builder builder = new AlertDialog.Builder(NavigationLiveMapActivity.this);
                                    builder.setView(R.layout.dialog_nearest);
                                    final AlertDialog dialog = builder.create();
                                    dialog.show();
                                    processing = false;
                                    final ListView listView = (ListView) dialog.findViewById(R.id.nearest_stops_list);
                                    if (inRangeList.isEmpty()) {
                                        TextView nothing = new TextView(NavigationLiveMapActivity.this);
                                        nothing.setText(R.string.too_far_text);
                                        nothing.setTextColor(Color.parseColor("#FFFFFF"));
                                        nothing.setTextSize(16);
                                        nothing.setPadding(15, 15, 15, 15);
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
                                            //show that marker's dialog..
                                            dialog.dismiss();
                                            Log.i(TAG, "would show marker...");
                                            //TODO: show marker here...
                                            StopLocation location = ((StopLocation) parent.getAdapter().getItem(position));
                                            for (ArrayList<Marker> markerList : stopMarkers) {
                                                for (Marker m : markerList) {
                                                    if (m.getTag().equals(location.getStopNumber())) {
                                                        setInfoView(m);
                                                        bottomSheet.post(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                bottomSheetBehavior.setPeekHeight(200);
                                                                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                                            }
                                                        });
                                                    }
                                                }
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    });
                    listUpdate.start();

                    return true;
                } else {
                    Toast.makeText(this, "Not ready!", Toast.LENGTH_SHORT).show();
                    processing = false;
                    return false;
                }
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public ArrayList<StopLocation> getStopLocations(List<StopInfo> stopInfoList) {
        final ArrayList<StopLocation> stopLocations = new ArrayList<>();

        for (final StopInfo s : stopInfoList) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://dave-cs.com")
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();

            LocationService locationService = retrofit.create(LocationService.class);

            Call<com.dave_cs.BroncoShuttlePlusServerUtil.Location> stopCall = locationService.getLocation(s.getStopNumber(), null);
            stopCall.enqueue(new Callback<com.dave_cs.BroncoShuttlePlusServerUtil.Location>() {

                @Override
                public void onResponse(Call<com.dave_cs.BroncoShuttlePlusServerUtil.Location> call, Response<com.dave_cs.BroncoShuttlePlusServerUtil.Location> response) {
                    if (response.isSuccess()) {
                        com.dave_cs.BroncoShuttlePlusServerUtil.Location l = response.body();
                        stopLocations.add(new StopLocation(s.getName(), s.getStopNumber(), l));
                    } else {
                        Log.e(TAG, response.code() + ":" + response.message());
                    }
                }

                @Override
                public void onFailure(Call<com.dave_cs.BroncoShuttlePlusServerUtil.Location> call, Throwable t) {
                    Log.e(TAG, t.getLocalizedMessage() + "::" + call.toString());
                }
            });
        }

        return stopLocations;
    }

    private void displayError() {
        ImageView img = (ImageView) findViewById(R.id.info_view_image);
        img.setImageResource(R.drawable.ic_error_icon);

        TextView titleText = (TextView) findViewById(R.id.info_view_title_text);
        titleText.setText(R.string.info_view_error);

        findViewById(R.id.info_view_content).setVisibility(View.GONE);

        bottomSheet.post(new Runnable() {
            @Override
            public void run() {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                bottomSheetBehavior.setPeekHeight(200);
                bottomSheetBehavior.setHideable(false);
            }
        });
    }

    /**
     * This override will show all the previous information with an extra nav button.
     *
     * @param m Marker with information to construct info view from
     */
    @Override
    protected void setInfoView(final Marker m) {
        super.setInfoView(m);
        Button navButton = (Button) findViewById(R.id.navigation_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dest = m.getPosition().latitude + ", " + m.getPosition().longitude;
                Uri gmmIntentUri = Uri.parse(String.format(Locale.getDefault(), "google.navigation:q=%s&mode=w", dest));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });
    }
}
