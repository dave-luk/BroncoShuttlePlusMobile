package com.dave_cs.BroncoShuttlePlusMobile.LiveMaps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Location;
import com.dave_cs.BroncoShuttlePlusServerUtil.LocationService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Package.DynamicRoutePackage;
import com.dave_cs.BroncoShuttlePlusServerUtil.Package.LiveMapDynamicPackageService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Package.LiveMapStaticPackageService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Package.StaticRoutePackage;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfo;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class LiveMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "LiveMapsActivity";
    private final ArrayList<Integer> routeIDs = new ArrayList<>();
    @Bind(R.id.liveMap_drawer_layout)
    protected DrawerLayout drawerLayout;
    @Bind(R.id.liveMap_left_drawer)
    protected ListView leftDrawer;
    //TODO make this a dialog/bottom slide up view.
    @Bind(R.id.liveMap_bottom_drawer)
    protected FrameLayout rightDrawer;
    private List<StaticRoutePackage> staticRoutePackages = new ArrayList<>();
    private List<DynamicRoutePackage> dynamicRoutePackages = new ArrayList<>();
    private GoogleMap mMap;
    private ArrayList<ArrayList<LatLng>> masterPolyList = new ArrayList<>();
    private ArrayList<ArrayList<Marker>> busMarkers = new ArrayList<>();
    private ArrayList<ArrayList<Marker>> stopMarkers = new ArrayList<>();
    private LatLngBounds.Builder stopBoundsBuilder = new LatLngBounds.Builder();
    private int stopBoundsCounter;
    private ArrayList<PolylineOptions> masterRouteOptions = new ArrayList<>();
    //TODO: hardcoded colors can be converted to non limited ranges (needs some math algorithm)
    private String[] routeColors = {"#BBFF0000", "#BB0000FF", "#BBFFFF00", "#BB335933", "#BBFF00FF"};
    private int hasPoly = -1;
    private boolean dynamicDataReady = false;

    //TODO flickery update issue untackled.
    private Handler updateHandler = new Handler();
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (hasPoly != -1) {
                getDynamicPackages(hasPoly);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_maps);
        ButterKnife.bind(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        getStaticPackages();
        getDynamicPackages(-1);

        //setting the initial right drawer closed
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
    }

    private List<String> getRouteNames() {
        ArrayList<String> names = new ArrayList<>();
        for (StaticRoutePackage srp : staticRoutePackages) {
            names.add(srp.routeName);
        }
        return names;
    }

    private void finalizeList() {
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.item_live_map_drawer_item, R.id.item_liveMap_drawer_item, getRouteNames());
        leftDrawer.setAdapter(adapter);
        leftDrawer.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (dynamicDataReady) {
                    toggleRouteVisible(position, ((hasPoly == -1 || hasPoly != position)));
                    drawerLayout.closeDrawers();
                } else {
                    Toast.makeText(LiveMapsActivity.this, "Data not ready...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void parseStaticPackages() {
        for (StaticRoutePackage srp : staticRoutePackages) {
            //routeIDs
            routeIDs.add(srp.routeNumber);
            //polyLines
            ArrayList<LatLng> polyLine = new ArrayList<>();
            for (Location l : srp.polyLine) {
                polyLine.add(l.parseLatLng());
            }
            masterPolyList.add(polyLine);
            masterRouteOptions.add(new PolylineOptions());
            stopMarkers.add(new ArrayList<Marker>());
            busMarkers.add(new ArrayList<Marker>());
        }
    }

    private void getBusLocation(final BusInfo info, final int packageIndex, final int index) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .client(client)
                .build();

        LocationService locationService = retrofit.create(LocationService.class);

        Log.d(TAG, "the bus id passed is: " + info.getBusNumber());
        Call<Location> busCall = locationService.getLocation(null, info.getBusNumber());
        busCall.enqueue(new Callback<Location>() {

            @Override
            public void onResponse(Call<Location> call, Response<Location> response) {
                if (response.isSuccess()) {
                    Location l = response.body();
                    Log.d(TAG, "location of bus is:" + l.parseLatLng());
                    initMarker(info, l.parseLatLng(), "bus", packageIndex, index);
                } else {
                    Log.e(TAG, response.code() + ":" + response.message());
                }
            }

            @Override
            public void onFailure(Call<Location> call, Throwable t) {
                Log.e(TAG, t.getLocalizedMessage() + "" + call.toString());
            }
        });


    }

    private void initMarker(Object o, LatLng location, String type, int packageIndex, int index) {
        if (mMap != null) {
            Log.d(TAG, "Updated marker to: " + location);
            stopBoundsBuilder.include(location);
            stopBoundsCounter++;
            if (o instanceof StopInfo) {
                Log.i(TAG, "making stop markers");
                stopMarkers.get(packageIndex).add(mMap.addMarker(new MarkerOptions().position(location).title(((StopInfo) o).getName()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus_stop_icon)).snippet(type + " " + packageIndex + " " + index)));
            } else if (o instanceof BusInfo) {
                try {
                    Marker curr = busMarkers.get(packageIndex).get(index);
                    if (curr.getTitle().equals(((BusInfo) o).getBusName()))
                        curr.setPosition(location);
                    else throw new Exception();
                } catch (Exception e) {
                    busMarkers.get(packageIndex).add(mMap.addMarker(new MarkerOptions().position(location).title(((BusInfo) o).getBusName()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus_icon)).snippet(type + " " + packageIndex + " " + index)));
                }
            }
        }
    }

    public void toggleRouteVisible(final int index, boolean state) {
        //clear map for redraw
        mMap.clear();

        if (state) {
            if (index != hasPoly) {
                for (ArrayList<Marker> l : stopMarkers) {
                    l = new ArrayList<>();
                }
                for (ArrayList<Marker> l : busMarkers) {
                    l = new ArrayList<>();
                }
                stopBoundsBuilder = new LatLngBounds.Builder();
                stopBoundsCounter = 0;
            }

            if (!masterPolyList.get(index).isEmpty()) {
                Log.d(TAG, "has poly line!");

                for (LatLng point : masterPolyList.get(index)) {
                    masterRouteOptions.get(index).add(point);
                }

                mMap.addPolyline(masterRouteOptions.get(index).color(Color.parseColor(routeColors[index])));
                hasPoly = index;
            }

            for (int i = 0; i < dynamicRoutePackages.get(index).stops.size(); i++) {
                initMarker(dynamicRoutePackages.get(index).stops.get(i), staticRoutePackages.get(index).stops.get(i).loc.parseLatLng(), "stop", index, i);
            }


            updateBusLocation(index);

            updateRunnable.run();

            if (stopBoundsCounter > 0) {
                LatLngBounds stopBounds = stopBoundsBuilder.build();
                pan(stopBounds);
            } else if (mMap != null) {
                LatLng cpp = new LatLng(34.056781, -117.821071);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cpp, 14.5f));
            }
        } else {
            hasPoly = -1;
        }
    }

    private void updateBusLocation(int index) {
        Log.d(TAG, "bus update: " + dynamicRoutePackages.get(index).buses.size());
        if (index != -1)
            for (int i = 0; i < dynamicRoutePackages.get(index).buses.size(); i++)
                getBusLocation(dynamicRoutePackages.get(index).buses.get(i), index, i);
    }

    private void pan(LatLngBounds b) {
        if (b != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(b.getCenter(), 14.5f));
        }
    }

    private List<StopInfo> getStopList(int packageIndex) {
        try {
            return dynamicRoutePackages.get(packageIndex).stops;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private List<BusInfo> getBusList(int packageIndex) {
        try {
            return dynamicRoutePackages.get(packageIndex).buses;
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private View createInfo(Marker m) {
        return new InfoView(this, m);
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
        mMap.setMyLocationEnabled(true);

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //remove the selected object info
                rightDrawer.removeAllViews();
                //close all drawers
                drawerLayout.closeDrawers();
                //locking the right drawer
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
            }
        });
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //unlocking right drawer
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
                //adding new object info into drawer
                rightDrawer.addView(createInfo(marker));
                //show drawer
                drawerLayout.openDrawer(GravityCompat.END);
                return true;
            }
        });

    }

    private void getStaticPackages() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        LiveMapStaticPackageService liveMapStaticPackageService = retrofit.create(LiveMapStaticPackageService.class);
        Call<List<StaticRoutePackage>> data = liveMapStaticPackageService.getStaticInfo();
        data.enqueue(new Callback<List<StaticRoutePackage>>() {

            @Override
            public void onResponse(Call<List<StaticRoutePackage>> call, Response<List<StaticRoutePackage>> response) {
                if (response.isSuccess()) {
                    staticRoutePackages = response.body();

                    parseStaticPackages();
                    finalizeList();
                }
            }

            @Override
            public void onFailure(Call<List<StaticRoutePackage>> call, Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
            }
        });
    }

    private void getDynamicPackages(final int index) {
        //configure longer Timeout...
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        LiveMapDynamicPackageService liveMapDynamicPackageService = retrofit.create(LiveMapDynamicPackageService.class);
        Call<List<DynamicRoutePackage>> data = liveMapDynamicPackageService.getDynamicInfo((index != -1) ? routeIDs.get(index) : -1);
        data.enqueue(new Callback<List<DynamicRoutePackage>>() {

            @Override
            public void onResponse(Call<List<DynamicRoutePackage>> call, Response<List<DynamicRoutePackage>> response) {
                Log.i(TAG, "Successfully get dynamic data");
                if (response.isSuccess()) {
                    if (index == -1) {
                        dynamicRoutePackages = response.body();
                        dynamicDataReady = true;
                    } else {
                        dynamicRoutePackages.set(index, response.body().get(0));
                        if (LiveMapsActivity.this.hasPoly != -1) {
                            updateBusLocation(hasPoly);
                        }
                        //schedule another one
                        updateHandler.postDelayed(updateRunnable, 5000);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<DynamicRoutePackage>> call, Throwable t) {
                Log.e(TAG + ".DP", t.getLocalizedMessage());
                //schedule another one
                updateHandler.postDelayed(updateRunnable, 5000);
            }
        });
    }

    /**
     * On Destroy, we will stop the updates
     */
    @Override
    public void onPause() {
        updateHandler.removeCallbacks(updateRunnable);
        Log.i(TAG, "removed handler on exit");

        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("StaticPackages", (ArrayList<? extends Parcelable>) staticRoutePackages);
        outState.putParcelableArrayList("DynamicPackages", (ArrayList<? extends Parcelable>) dynamicRoutePackages);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        staticRoutePackages = savedInstanceState.getParcelableArrayList("StaticPackages");
        dynamicRoutePackages = savedInstanceState.getParcelableArrayList("DynamicPackages");
        parseStaticPackages();
        dynamicDataReady = true;
        finalizeList();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    //infoView
    private class InfoView extends LinearLayout {

        private LinearLayout innerLayout;

        public InfoView(Context c, Marker m) {
            super(c);
            String[] data = m.getSnippet().split("[ ]+");
            Log.d(TAG, "" + data[0] + "|" + data[1] + "|" + data[2]);
            //main box

            setBackgroundColor(Color.DKGRAY);
            setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            setWeightSum(100f);

            setLayoutParams(linearLayoutParams);

            //


            //horizontal box
            LinearLayout horiz = new LinearLayout(getContext());
            horiz.setBackgroundColor(Color.parseColor("#b4cfb5"));
            horiz.setOrientation(LinearLayout.HORIZONTAL);
            horiz.setWeightSum(100f);

            LinearLayout.LayoutParams horizParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
            horizParams.weight = 20f;

            horiz.setLayoutParams(horizParams);

            //

            //img view
            ImageView img = new ImageView(getContext());
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            imgParams.weight = 35f;
            img.setScaleType(ImageView.ScaleType.FIT_CENTER);
            img.setBackgroundColor(Color.parseColor("#00000000"));
            img.setImageResource(R.drawable.ic_bus_stop_icon);

            img.setLayoutParams(imgParams);

            //

            //text view
            TextView title = new TextView(getContext());
            title.setText(m.getTitle());
            title.setTextColor(Color.WHITE);
            title.setBackgroundColor(Color.TRANSPARENT);
            title.setSingleLine(false);
            title.setGravity(Gravity.CENTER_VERTICAL);
            title.setTextSize(24);

            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            titleParams.rightMargin = 5;
            titleParams.weight = 65f;

            title.setLayoutParams(titleParams);

            //


            horiz.addView(img);
            horiz.addView(title);

            innerLayout = new LinearLayout(getContext());
            innerLayout.setBackgroundColor(Color.WHITE);
            innerLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
            frameParams.weight = 80f;

            innerLayout.setLayoutParams(frameParams);

            switch (data[0]) {
                case "stop":
                    StopInfo stopInfo = getStopList(Integer.parseInt(data[1])).get(Integer.parseInt(data[2]));

                    TextView routes = new TextView(getContext());
                    routes.setText(String.format("Routes: %s", stopInfo.getOnRoute()));
                    routes.setTextColor(Color.BLACK);
                    routes.setTextSize(24);

                    int timeToNext = stopInfo.getTimeToNext();
                    String nextBusStr, nextTime;
                    if (timeToNext < 0) {
                        nextBusStr = "OUT OF SERVICE";
                        nextTime = "";
                    } else {
                        nextBusStr = stopInfo.getNextBusOfRoute() + " bus in";
                        nextTime = Integer.toString(timeToNext) + " s";
                    }


                    LinearLayout.LayoutParams routeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    routes.setLayoutParams(routeParams);

                    TextView nextBus = new TextView(getContext());
                    nextBus.setText(nextBusStr);
                    nextBus.setTextColor(Color.BLACK);
                    nextBus.setTextSize(24);

                    LinearLayout.LayoutParams nextBusParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    nextBus.setLayoutParams(nextBusParams);

                    TextView nextBusTime = new TextView(getContext());
                    nextBusTime.setText(nextTime);
                    nextBusTime.setTextColor(Color.BLACK);
                    nextBusTime.setTextSize(24);

                    LinearLayout.LayoutParams nextBusTimeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    nextBusTime.setLayoutParams(nextBusTimeParams);

                    innerLayout.addView(routes);
                    innerLayout.addView(nextBus);
                    innerLayout.addView(nextBusTime);
                    break;
                case "bus":
                    BusInfo busInfo = getBusList(Integer.parseInt(data[1])).get(Integer.parseInt(data[2]));

                    TextView busRoute = new TextView(getContext());
                    busRoute.setText(String.format("Route: %s", busInfo.getRoute()));
                    busRoute.setTextColor(Color.BLACK);
                    busRoute.setTextSize(24);

                    LinearLayout.LayoutParams busRouteParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    busRoute.setLayoutParams(busRouteParams);

                    String fullnessStr;
                    int x;

                    if ((x = busInfo.getFullness()) < 77) {
                        fullnessStr = Integer.toString(x) + "% full. approx. " + (30 - x * (30) / 100) + "/30 seats left";
                    } else {
                        fullnessStr = Integer.toString(x) + "% full. Full seated.";
                    }

                    TextView fullness = new TextView(getContext());
                    fullness.setText(fullnessStr);
                    fullness.setTextColor(Color.BLACK);
                    fullness.setTextSize(24);

                    LinearLayout.LayoutParams fullnessParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    fullness.setLayoutParams(fullnessParams);

                    TextView nextStop = new TextView(getContext());
                    nextStop.setText(String.format("Next Stop: %s", busInfo.getNextStop()));
                    nextStop.setTextColor(Color.BLACK);
                    nextStop.setTextSize(24);

                    LinearLayout.LayoutParams nextStopParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    nextStop.setLayoutParams(nextStopParams);

                    innerLayout.addView(busRoute);
                    innerLayout.addView(fullness);
                    innerLayout.addView(nextStop);
                    break;
            }

            addView(horiz);
            addView(innerLayout);

        }

    }
}
