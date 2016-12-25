package com.dave_cs.BroncoShuttlePlusMobile.LiveMaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dave_cs.BroncoShuttlePlusMobile.Application.Data.LiveMapData;
import com.dave_cs.BroncoShuttlePlusMobile.Application.DataUpdateApplication;
import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Location;
import com.dave_cs.BroncoShuttlePlusServerUtil.LocationService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Package.DynamicRoutePackage;
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
import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class LiveMapsActivity extends AppCompatActivity implements OnMapReadyCallback, Observer {

    private static final String TAG = "LiveMapsActivity";

    private static final int UPDATE_INTERVAL = 10000;

    protected final ArrayList<Integer> routeIDs = new ArrayList<>();
    @Nullable
    @Bind(R.id.liveMap_drawer_layout)
    protected DrawerLayout drawerLayout;
    @Nullable
    @Bind(R.id.liveMap_left_drawer)
    protected ListView leftDrawerList;
    @Nullable
    @Bind(R.id.listView_error_box)
    protected LinearLayout errorBoxLinearLayout;
    @Bind(R.id.liveMap_bottom_sheet)
    protected NestedScrollView bottomSheet;
    protected GoogleMap mMap;
    protected List<StaticRoutePackage> staticRoutePackages = new ArrayList<>();
    protected List<DynamicRoutePackage> dynamicRoutePackages = new ArrayList<>();
    protected boolean dynamicDataReady = false;

    protected ArrayList<ArrayList<LatLng>> masterPolyList = new ArrayList<>();
    protected ArrayList<ArrayList<Marker>> busMarkers = new ArrayList<>();
    protected ArrayList<ArrayList<Marker>> stopMarkers = new ArrayList<>();
    protected ArrayList<PolylineOptions> masterRouteOptions = new ArrayList<>();
    protected LatLngBounds.Builder stopBoundsBuilder = new LatLngBounds.Builder();
    protected int stopBoundsCounter;
    //TODO: hardcoded colors can be converted to non limited ranges (needs some math algorithm)
    protected String[] routeColors = {"#BBFF0000", "#BB0000FF", "#BBFFFF00", "#BB335933", "#BBFF00FF"};
    protected BottomSheetBehavior bottomSheetBehavior;
    protected Handler updateHandler = new Handler();
    protected boolean error = false;
    private int hasPoly = -1;
    protected Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (hasPoly != -1) {
                ((DataUpdateApplication) getApplication()).liveMapData.requestUpdate(hasPoly, routeIDs.get(hasPoly));
            }
        }
    };
    //TODO flickery update issue untackled.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((DataUpdateApplication) getApplication()).liveMapData.addObserver(this);

        onCreateLayout();
    }

    protected void onCreateLayout() {
        setContentView(R.layout.activity_live_maps);
        ButterKnife.bind(this);

        if (((DataUpdateApplication) getApplication()).liveMapData.liveMapStaticRoutePackages.isEmpty()) {
            Log.i(TAG, "empty!");
            errorBoxLinearLayout.setVisibility(View.VISIBLE);
            error = true;
        } else {
            staticRoutePackages = ((DataUpdateApplication) getApplication()).liveMapData.liveMapStaticRoutePackages;
            parseStaticPackages();
            finalizeList();
            error = false;
            errorBoxLinearLayout.setVisibility(View.GONE);
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
    }

    private List<String> getRouteNames() {
        ArrayList<String> names = new ArrayList<>();
        for (StaticRoutePackage srp : staticRoutePackages) {
            names.add(srp.routeName);
        }
        return names;
    }

    protected void finalizeList() {
        ArrayAdapter adapter = new ArrayAdapter<>(this, R.layout.item_live_map_drawer_item, R.id.item_liveMap_drawer_item, getRouteNames());
        leftDrawerList.setAdapter(adapter);
        leftDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
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

    protected void parseStaticPackages() {
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

        Log.i(TAG, "the bus id passed is: " + info.getBusNumber());
        Call<Location> busCall = locationService.getLocation(null, info.getBusNumber());
        busCall.enqueue(new Callback<Location>() {

            @Override
            public void onResponse(Call<Location> call, Response<Location> response) {
                if (response.isSuccess()) {
                    Location l = response.body();
                    Log.i(TAG, "location of bus is:" + l.parseLatLng());
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

    protected void initMarker(Object o, LatLng location, String type, int packageIndex, int index) {
        if (mMap != null) {
            Log.i(TAG, "Updated marker to: " + location);
            if (type.equals("stop")) {
                stopBoundsBuilder.include(location);
                stopBoundsCounter++;
            }
            if (o instanceof StopInfo) {
                Log.i(TAG, "making stop markers");
                Marker m = mMap.addMarker(new MarkerOptions().position(location).title(((StopInfo) o).getName()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus_stop_icon)).snippet(type + " " + packageIndex + " " + index));
                m.setTag(((StopInfo) o).getStopNumber());
                stopMarkers.get(packageIndex).add(m);
            } else if (o instanceof BusInfo) {
                try {
                    Marker curr = busMarkers.get(packageIndex).get(index);
                    if (curr.getTag().equals(((BusInfo) o).getBusNumber())) {
                        curr.setPosition(location);
                    } else {
                        throw new Exception();
                    }
                } catch (Exception e) {
                    Marker m = mMap.addMarker(new MarkerOptions().position(location).title(((BusInfo) o).getBusName()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus_icon)).snippet(type + " " + packageIndex + " " + index));
                    m.setTag(((BusInfo) o).getBusNumber());
                    busMarkers.get(packageIndex).add(m);
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
                Log.i(TAG, "has poly line!");

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

            updateHandler.post(updateRunnable);

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

    protected void updateBusLocation(int index) {
        Log.i(TAG, "bus update: " + dynamicRoutePackages.get(index).buses.size());
        if (index != -1)
            for (int i = 0; i < dynamicRoutePackages.get(index).buses.size(); i++)
                getBusLocation(dynamicRoutePackages.get(index).buses.get(i), index, i);
    }

    protected void pan(LatLngBounds b) {
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
                //close all drawers
                drawerLayout.closeDrawers();
                //locking the right drawer
//                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
                bottomSheet.post(new Runnable() {
                    @Override
                    public void run() {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                        bottomSheetBehavior.setPeekHeight(0);
                    }
                });
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
    protected void onResume() {
        Log.i(TAG, "resumed: " + error);
        if (errorBoxLinearLayout != null) {
            if (error) {
                errorBoxLinearLayout.setVisibility(View.VISIBLE);
            } else {
                errorBoxLinearLayout.setVisibility(View.GONE);
            }
        }
        if (hasPoly != -1) {
            updateHandler.postDelayed(updateRunnable, UPDATE_INTERVAL);
        }
        super.onResume();
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
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED && !error) {
            bottomSheet.post(new Runnable() {
                @Override
                public void run() {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            });
        } else if (!error) {
            moveTaskToBack(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {

                Rect outRect = new Rect();
                bottomSheet.getGlobalVisibleRect(outRect);

                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    bottomSheet.post(new Runnable() {
                        @Override
                        public void run() {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            bottomSheetBehavior.setPeekHeight(0);
                        }
                    });

                }
            }
        }

        return super.dispatchTouchEvent(event);
    }

    protected void setInfoView(Marker m) {
        String[] data = m.getSnippet().split("[ ]+");
        Log.i(TAG, "" + data[0] + "|" + data[1] + "|" + data[2]);

        ImageView img = (ImageView) findViewById(R.id.info_view_image);
        img.setImageResource((data[0].equals("stop") ? R.drawable.ic_bus_stop_icon : R.drawable.ic_bus_icon_tint));

        TextView titleText = (TextView) findViewById(R.id.info_view_title_text);
        titleText.setText(m.getTitle());

        TextView line1 = (TextView) findViewById(R.id.info_line_1);
        TextView line2 = (TextView) findViewById(R.id.info_line_2);
        TextView line3 = (TextView) findViewById(R.id.info_line_3);


        switch (data[0]) {
            case "stop":
                StopInfo stopInfo = getStopList(Integer.parseInt(data[1])).get(Integer.parseInt(data[2]));

                int timeToNext = stopInfo.getTimeToNext();
                String nextBusStr, nextTime;
                if (timeToNext < 0) {
                    nextBusStr = "OUT OF SERVICE";
                    nextTime = "";
                } else {
                    nextBusStr = stopInfo.getNextBusOfRoute() + " bus in";
                    nextTime = Integer.toString(timeToNext) + " s";
                }

                line1.setText(String.format("Routes: %s", stopInfo.getOnRoute()));
                line2.setText(nextBusStr);
                line3.setText(nextTime);
                break;
            case "bus":
                BusInfo busInfo = getBusList(Integer.parseInt(data[1])).get(Integer.parseInt(data[2]));

                String fullnessStr;
                int x;

                if ((x = busInfo.getFullness()) < 77) {
                    fullnessStr = Integer.toString(x) + "% full. approx. " + (30 - x * (30) / 100) + "/30 seats left";
                } else {
                    fullnessStr = Integer.toString(x) + "% full. Full seated.";
                }

                line1.setText(String.format("Route: %s", busInfo.getRoute()));
                line2.setText(fullnessStr);
                line3.setText(String.format("Next Stop: %s", busInfo.getNextStop()));
                break;
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.i(TAG, "Updating...");
        if (observable instanceof LiveMapData) {
            if (!((LiveMapData) observable).liveMapDynamicRoutePackages.isEmpty()) {
                dynamicRoutePackages = ((LiveMapData) observable).liveMapDynamicRoutePackages;
                dynamicDataReady = true;
                if (hasPoly != -1) {
                    updateBusLocation(hasPoly);
                }
            }
            if (staticRoutePackages.isEmpty() && !((LiveMapData) observable).liveMapStaticRoutePackages.isEmpty()) {
                staticRoutePackages = ((LiveMapData) observable).liveMapStaticRoutePackages;
                error = false;
                errorBoxLinearLayout.setVisibility(View.GONE);
                parseStaticPackages();
                finalizeList();
            }
        }
    }
}