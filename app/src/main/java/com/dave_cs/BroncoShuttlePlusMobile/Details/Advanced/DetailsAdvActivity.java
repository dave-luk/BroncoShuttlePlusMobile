package com.dave_cs.BroncoShuttlePlusMobile.Details.Advanced;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfoService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Location;
import com.dave_cs.BroncoShuttlePlusServerUtil.LocationService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfoService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.Bind;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by David on 2/7/2016.
 */
public class DetailsAdvActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "DetailsAdvActivity";
    @Bind(R.id.refocus_icon)
    protected ImageButton recenter;
    @Bind(R.id.details_frag_holder)
    protected FrameLayout detailsInfo;
    private FragmentManager fm = getSupportFragmentManager();
    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private LatLng location = new LatLng(34.056781, -117.821071);
    private Marker currMarker;
    private LinearLayout linearLayout;
    private LinearLayout innerLayout;
    private Object info;
    private String name;
    private InfoType type;
    private int id;
    private Handler updateHandler = new Handler();
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            getInfo();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_details_adv);
        ButterKnife.bind(this);

        //Set up for info layouts.
        linearLayout = new LinearLayout(this);
        linearLayout.setBackgroundColor(Color.WHITE);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linearLayout.setWeightSum(100f);
        linearLayout.setLayoutParams(linearLayoutParams);

        //this let user return to marker.
        recenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16.5f));
                }
            }
        });

        //set up the map fragment
        mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_frag);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_frag, mapFragment).commit();
        }

        // get the type from bundles, write to lat-lng
        try {
            int stopID = getIntent().getExtras().getInt("stopNumber");
            String stopName = getIntent().getExtras().getString("stopName");
            int busID = getIntent().getExtras().getInt("busNumber");
            String busName = getIntent().getExtras().getString("busName");
            if (stopID != 0) {
                name = stopName;
                type = InfoType.STOP;
                id = stopID;
            } else if (busID != 0) {
                name = busName;
                type = InfoType.BUS;
                id = busID;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getLocation();
        getInfo();

        //TODO: make this nicer
        Toolbar toolbar = (Toolbar) findViewById(R.id.advBar);
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (googleMap != null) {
            if (getFragmentManager().findFragmentById(R.id.map_frag) != null) {
                getFragmentManager()
                        .beginTransaction()
                        .remove(getFragmentManager().findFragmentById(R.id.map_frag))
                        .commit();
            }
            googleMap = null;
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.i(TAG, "map ready");
        this.googleMap = googleMap;

        Log.i(TAG, "got a location. displaying" + location.toString());
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
        googleMap.setMyLocationEnabled(true);
        initMarker();

        getSupportFragmentManager().beginTransaction().replace(R.id.map_frag, mapFragment).commit();


        //This keeps the name on all times.
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                currMarker.showInfoWindow();
            }
        });
        // This disables navigation, we don't want this behavior.
//        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                if (!marker.isInfoWindowShown()) {
//                    marker.showInfoWindow();
//                }
//                return true;
//            }
//        });

        if (mapFragment == null) {
            Log.i(TAG, "replacing frag");
            mapFragment = SupportMapFragment.newInstance();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.map_frag, mapFragment).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (googleMap == null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void applyStyle(TextView view, int resID) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            view.setTextAppearance(resID);
        } else {
            view.setTextAppearance(this, resID);
        }
    }

    private void initInfo() {

        TextView title = new TextView(this);
        title.setText(name);
        title.setTextColor(Color.WHITE);
        title.setBackgroundColor(Color.parseColor("#b4cfb5"));
        title.setSingleLine(true);
        applyStyle(title, R.style.AppTheme_InfoWindowTitle);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setTextSize(24);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        titleParams.weight = 30f;
        title.setLayoutParams(titleParams);

        innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        frameParams.weight = 70f;
        innerLayout.setLayoutParams(frameParams);

        if (info != null) {
            switch (type) {
                case STOP:
                    StopInfo stopInfo = (StopInfo) info;

                    TextView routes = new TextView(this);
                    routes.setText(String.format("Routes: %s", stopInfo.getOnRoute()));
                    routes.setTextColor(Color.BLACK);
                    applyStyle(routes, R.style.AppTheme_InfoWindowContent);
                    routes.setTextSize(20);

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

                    TextView nextBus = new TextView(this);
                    nextBus.setText(nextBusStr);
                    nextBus.setTextColor(Color.BLACK);
                    applyStyle(nextBus, R.style.AppTheme_InfoWindowContent);
                    nextBus.setTextSize(20);

                    LinearLayout.LayoutParams nextBusParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    nextBus.setLayoutParams(nextBusParams);

                    TextView nextBusTime = new TextView(this);
                    nextBusTime.setText(nextTime);
                    nextBusTime.setTextColor(Color.BLACK);
                    applyStyle(nextBusTime, R.style.AppTheme_InfoWindowContent);
                    nextBusTime.setTextSize(20);

                    LinearLayout.LayoutParams nextBusTimeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    nextBusTime.setLayoutParams(nextBusTimeParams);

                    clearView();
                    innerLayout.addView(routes);
                    innerLayout.addView(nextBus);
                    innerLayout.addView(nextBusTime);
                    break;
                case BUS:
                    BusInfo busInfo = (BusInfo) info;

                    TextView busRoute = new TextView(this);
                    busRoute.setText(String.format("Route: %s", busInfo.getRoute()));
                    busRoute.setTextColor(Color.BLACK);
                    applyStyle(busRoute, R.style.AppTheme_InfoWindowContent);
                    busRoute.setTextSize(20);

                    LinearLayout.LayoutParams busRouteParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    busRoute.setLayoutParams(busRouteParams);

                    String fullnessStr;
                    int x;

                    if ((x = busInfo.getFullness()) < 77) {
                        fullnessStr = Integer.toString(x) + "% full. approx. " + (30 - x * (30) / 100) + "/30 seats left";
                    } else {
                        fullnessStr = Integer.toString(x) + "% full. Full seated.";
                    }

                    TextView fullness = new TextView(this);
                    fullness.setText(fullnessStr);
                    fullness.setTextColor(Color.BLACK);
                    applyStyle(fullness, R.style.AppTheme_InfoWindowContent);
                    fullness.setTextSize(20);

                    LinearLayout.LayoutParams fullnessParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    fullness.setLayoutParams(fullnessParams);

                    TextView nextStop = new TextView(this);
                    nextStop.setText(String.format("Next Stop: %s", busInfo.getNextStop()));
                    nextStop.setTextColor(Color.BLACK);
                    applyStyle(nextStop, R.style.AppTheme_InfoWindowContent);
                    nextStop.setTextSize(20);

                    LinearLayout.LayoutParams nextStopParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    nextStop.setLayoutParams(nextStopParams);

                    clearView();
                    innerLayout.addView(busRoute);
                    innerLayout.addView(fullness);
                    innerLayout.addView(nextStop);
                    break;
            }

            linearLayout.addView(title);

            linearLayout.addView(innerLayout);

        }

        detailsInfo.addView(linearLayout);
    }

    private void clearView() {
        innerLayout.removeAllViews();
        linearLayout.removeAllViews();
        detailsInfo.removeAllViews();
    }

    private void getInfo() {

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

        switch (type) {
            case STOP:
                StopInfoService stopInfoService = retrofit.create(StopInfoService.class);

                Call<StopInfo> stopCall = stopInfoService.getInfo(Integer.toString(id));
                stopCall.enqueue(new Callback<StopInfo>() {
                    @Override
                    public void onResponse(Call<StopInfo> call, Response<StopInfo> response) {
                        if (response.isSuccess()) {
                            Log.i(TAG, "get info");
                            info = response.body();
                            initInfo();
                            updateHandler.postDelayed(updateRunnable, 5000);
                        }
                    }

                    @Override
                    public void onFailure(Call<StopInfo> call, Throwable t) {
                        Log.e(TAG, "error: " + t.getLocalizedMessage() + "" + call.toString());
                    }

                });
                break;
            case BUS:
                BusInfoService busInfoService = retrofit.create(BusInfoService.class);

                Call<BusInfo> busCall = busInfoService.getInfo(Integer.toString(id));
                busCall.enqueue(new Callback<BusInfo>() {
                    @Override
                    public void onResponse(Call<BusInfo> call, Response<BusInfo> response) {
                        if (response.isSuccess()) {
                            Log.i(TAG, "get info");
                            info = response.body();
                            initInfo();
                            updateHandler.postDelayed(updateRunnable, 5000);
                        }
                    }

                    @Override
                    public void onFailure(Call<BusInfo> call, Throwable t) {
                        Log.e(TAG, "error: " + t.getLocalizedMessage() + "" + call.toString());
                    }
                });
                break;
        }
    }

    private void getLocation() {

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

        switch (type) {
            case STOP:
                Log.i(TAG, "the stop id passed is: " + id);
                Call<Location> stopCall = locationService.getLocation(id, null);
                stopCall.enqueue(new Callback<Location>() {

                    @Override
                    public void onResponse(Call<Location> call, Response<Location> response) {
                        if (response.isSuccess()) {
                            Location l = response.body();
                            location = new LatLng(l.getLat(), l.getLng());
                            Log.i(TAG, "location is:" + location);
                            initMarker();
                        } else {
                            Log.e(TAG, response.code() + ":" + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Location> call, Throwable t) {
                        Log.e(TAG, "error: " + t.getLocalizedMessage() + "" + call.toString());
                    }
                });
                break;
            case BUS:
                Log.i(TAG, "the bus id passed is: " + id);
                Call<Location> busCall = locationService.getLocation(null, id);
                busCall.enqueue(new Callback<Location>() {

                    @Override
                    public void onResponse(Call<Location> call, Response<Location> response) {
                        if (response.isSuccess()) {
                            Location l = response.body();
                            location = new LatLng(l.getLat(), l.getLng());
                            Log.i(TAG, "location of bus is:" + location);
                            initMarker();
                        } else {
                            Log.e(TAG, response.code() + ":" + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Location> call, Throwable t) {
                        Log.e(TAG, "error: " + t.getLocalizedMessage() + "" + call.toString());
                    }
                });
                break;
        }
    }

    private void initMarker() {
        if (googleMap != null) {
            Log.i(TAG, "Updated marker to: " + location);
            googleMap.clear();
            currMarker = googleMap.addMarker(new MarkerOptions().position(location).title(name).icon((type.equals(InfoType.BUS)) ? BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus_icon) : BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus_stop_icon)));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16.5f), new GoogleMap.CancelableCallback() {
                @Override
                public void onFinish() {
                    currMarker.showInfoWindow();
                }

                @Override
                public void onCancel() {
                }
            });

        }
    }

    @Override
    protected void onPause() {
        updateHandler.removeCallbacks(updateRunnable);
        super.onPause();
    }

    public enum InfoType {
        BUS, STOP
    }
}


