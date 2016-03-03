package com.dave_cs.BroncoShuttlePlusMobile.LiveMaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusListService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Location;
import com.dave_cs.BroncoShuttlePlusServerUtil.LocationService;
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

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class LiveMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final String[] routeTitle = {"Route A", "Route B1", "Route B2", "Route C"};
    private final int[] routeList = {3164, 3166, 3167, 3162};
    private GoogleMap mMap;
    private List<LatLng> A = new ArrayList<>(),
            B1 = new ArrayList<>(),
            B2 = new ArrayList<>(),
            C = new ArrayList<>();
    private List<StopInfo> AStop = new ArrayList<>(),
            B1Stop = new ArrayList<>(),
            B2Stop = new ArrayList<>(),
            CStop = new ArrayList<>();
    private List<BusInfo> ABus = new ArrayList<>(),
            B1Bus = new ArrayList<>(),
            B2Bus = new ArrayList<>(),
            CBus = new ArrayList<>();
    private List<Marker> BusMarkers = new ArrayList<>();
    private LatLngBounds.Builder boundsBuilder;
    private LatLngBounds.Builder stopBoundsBuilder = new LatLngBounds.Builder();
    private LatLngBounds bounds;
    private LatLngBounds stopBounds;
    private int stopBoundsCounter;
    private PolylineOptions routeA = new PolylineOptions(),
            routeB1 = new PolylineOptions(),
            routeB2 = new PolylineOptions(),
            routeC = new PolylineOptions();
    private int hasPoly;

    private DrawerLayout drawerLayout;
    private ListView leftDrawer;
    private FrameLayout bottomDrawer;

    private LinearLayout linearLayout;
    private LinearLayout innerLayout;

    private Handler uiCallback = new Handler() {
        public void handleMessage(Message msg) {
            if (hasPoly != -1)
                updateBusLocation(routeList[hasPoly]);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        for (int i = 0; i < routeList.length; i++) {
            getPolyLine(routeList[i]);
            getRouteInfo(routeList[i]);
            getBusInfo(routeTitle[i].replace("Route ", ""));
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.liveMap_drawer_layout);
        leftDrawer = (ListView) findViewById(R.id.liveMap_left_drawer);
        bottomDrawer = (FrameLayout) findViewById(R.id.liveMap_bottom_drawer);

        leftDrawer.setAdapter(new ArrayAdapter<>(this,
                R.layout.item_live_map_drawer_item, R.id.item_liveMap_drawer_item, routeTitle));
        leftDrawer.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                togglePolyline(routeList[position], ((hasPoly == -1 || hasPoly != position) ? true : false));
                drawerLayout.closeDrawers();
            }
        });
    }

    private void getPolyLine(final int route) {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .client(client)
                .build();

        PolyLineService polyLineService = retrofit.create(PolyLineService.class);

        Call<List<Location>> locationCall = polyLineService.polyList(route);
        locationCall.enqueue(new Callback<List<Location>>() {
            @Override
            public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
                if (response.body() != null) {
                    switch (route) {
                        case 3164:
                            for (Location l : response.body()) {
                                A.add(l.parseLatLng());
                            }
                            routeA.addAll(A);
                            break;
                        case 3166:
                            for (Location l : response.body()) {
                                B1.add(l.parseLatLng());
                            }
                            routeB1.addAll(B1);
                            break;
                        case 3167:
                            for (Location l : response.body()) {
                                B2.add(l.parseLatLng());
                            }
                            routeB2.addAll(B2);
                            break;
                        case 3162:
                            for (Location l : response.body()) {
                                C.add(l.parseLatLng());
                            }
                            routeC.addAll(C);
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Location>> call, Throwable t) {
                Log.e("<Error>", t.getLocalizedMessage());
            }
        });
    }

    private void getRouteInfo(final int r) {
        // reach to server and pull route info
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        RouteStopService routeInfoService = retrofit.create(RouteStopService.class);
        Call<List<StopInfo>> call = routeInfoService.getrouteStops(r);
        call.enqueue(new Callback<List<StopInfo>>() {

            @Override
            public void onResponse(Call<List<StopInfo>> call, Response<List<StopInfo>> response) {
                if (response.isSuccess()) {
                    Log.d("<Success>", "received data");

                    switch (r) {
                        case 3164:
                            AStop.clear();
                            AStop = response.body();
                            break;
                        case 3166:
                            B1Stop.clear();
                            B1Stop = response.body();
                            break;
                        case 3167:
                            B2Stop.clear();
                            B2Stop = response.body();
                            break;
                        case 3162:
                            CStop.clear();
                            CStop = response.body();
                            break;
                    }
                } else {
                    Log.d("<Error>", "" + response.code());
                }

            }

            @Override
            public void onFailure(Call<List<StopInfo>> call, Throwable t) {
                Log.e("<Error>", t.getLocalizedMessage());
            }
        });
    }

    private void getBusInfo(final String str) {
        // reach to server and pull route info


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        BusListService busListService = retrofit.create(BusListService.class);

        Call<List<BusInfo>> call = busListService.getInfo(str);
        call.enqueue(new Callback<List<BusInfo>>() {

            @Override
            public void onResponse(Call<List<BusInfo>> call, Response<List<BusInfo>> response) {
                if (response.isSuccess()) {
                    switch (str) {
                        case "A":
                            ABus.clear();
                            ABus.addAll(response.body());
                            break;
                        case "B1":
                            B1Bus.clear();
                            B1Bus.addAll(response.body());
                            break;
                        case "B2":
                            B2Bus.clear();
                            B2Bus.addAll(response.body());
                            break;
                        case "C":
                            CBus.clear();
                            CBus.addAll(response.body());
                            break;
                    }
                } else {
                    Log.e("<Error>", response.code() + ":" + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<BusInfo>> call, Throwable t) {
                Log.e("<Error>", t.getLocalizedMessage() + "");
            }
        });
    }

    private void getLocation(final String type, final Object info, final int route, final int index) {

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
            case "stop":
                Log.d("<Query>", "the stop id passed is: " + ((StopInfo) info).getStopNumber());
                Call<Location> stopCall = locationService.getLocation(((StopInfo) info).getStopNumber(), null);
                stopCall.enqueue(new Callback<Location>() {

                    @Override
                    public void onResponse(Call<Location> call, Response<Location> response) {
                        if (response.isSuccess()) {
                            Location l = response.body();
                            LatLng location = new LatLng(l.getLat(), l.getLng());
                            Log.d("<Location>", "location is:" + location);
                            initMarker(info, location, type, route, index);
                        } else {
                            Log.e("<Error>", response.code() + ":" + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Location> call, Throwable t) {
                        Log.e("<Error>", t.getLocalizedMessage() + "" + call.toString());
                    }
                });
                break;
            case "bus":
                Log.d("<Query>", "the bus id passed is: " + ((BusInfo) info).getBusNumber());
                Call<Location> busCall = locationService.getLocation(null, ((BusInfo) info).getBusNumber());
                busCall.enqueue(new Callback<Location>() {

                    @Override
                    public void onResponse(Call<Location> call, Response<Location> response) {
                        if (response.isSuccess()) {
                            Location l = response.body();
                            LatLng location = new LatLng(l.getLat(), l.getLng());
                            Log.d("<Location>", "location of bus is:" + l.getLat() + " | " + location);
                            initMarker(info, location, type, route, index);
                        } else {
                            Log.e("<Error>", response.code() + ":" + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Call<Location> call, Throwable t) {
                        Log.e("<Error>", t.getLocalizedMessage() + "" + call.toString());
                    }
                });
                break;
        }
    }

    private void initMarker(Object o, LatLng location, String type, int route, int index) {
        if (mMap != null) {
            Log.d("<Marker>", "Updated marker to: " + location);
            stopBoundsBuilder.include(location);
            stopBoundsCounter++;
            if (o instanceof StopInfo) {
                mMap.addMarker(new MarkerOptions().position(location).title(((StopInfo) o).getName()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus_stop_icon)).snippet(type + " " + route + " " + index));
            } else if (o instanceof BusInfo) {
                BusMarkers.add(mMap.addMarker(new MarkerOptions().position(location).title(((BusInfo) o).getBusName()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus_icon))));
            }


        }
    }

    public void togglePolyline(int route, boolean state) {
        mMap.clear();

        if (state) {
            boundsBuilder = new LatLngBounds.Builder();
            switch (route) {
                case 3164:
                    if (!A.isEmpty()) {
                        for (LatLng point : A) {
                            boundsBuilder.include(point);
                        }
                        bounds = boundsBuilder.build();

                        mMap.addPolyline(routeA.color(Color.parseColor("#BBFF0000")));
                        hasPoly = 0;
                    }

                    for (int i = 0; i < AStop.size(); i++) {
                        getLocation("stop", AStop.get(i), route, i);
                    }
                    break;
                case 3166:
                    if (!B1.isEmpty()) {
                        for (LatLng point : B1) {
                            boundsBuilder.include(point);
                        }
                        bounds = boundsBuilder.build();

                        mMap.addPolyline(routeB1.color(Color.parseColor("#BB0000FF")));
                        hasPoly = 1;
                    }

                    for (int i = 0; i < B1Stop.size(); i++) {
                        getLocation("stop", B1Stop.get(i), route, i);
                    }
                    break;
                case 3167:
                    if (!B2.isEmpty()) {
                        for (LatLng point : B2) {
                            boundsBuilder.include(point);
                        }
                        bounds = boundsBuilder.build();

                        mMap.addPolyline(routeB2.color(Color.parseColor("#BBFFFF00")));
                        hasPoly = 2;
                    }

                    for (int i = 0; i < B2Stop.size(); i++) {
                        getLocation("stop", B2Stop.get(i), route, i);
                    }
                    break;
                case 3162:
                    if (!C.isEmpty()) {
                        for (LatLng point : C) {
                            boundsBuilder.include(point);
                        }
                        bounds = boundsBuilder.build();

                        mMap.addPolyline(routeC.color(Color.parseColor("#BB335933")));
                        hasPoly = 3;
                    }

                    for (int i = 0; i < CStop.size(); i++) {
                        getLocation("stop", CStop.get(i), route, i);
                    }
                    break;
            }
            updateBusLocation(route);

            Thread timer = new Thread() {
                public void run() {
                    for (; ; ) {
                        // do stuff in a separate thread
                        uiCallback.sendEmptyMessage(0);
                        try {
                            Thread.sleep(3000);    // sleep for 3 seconds
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            timer.start();

            Log.d("<PolyLine>", "elements: " + A.size() + " | " + B1.size() + " | " + B2.size() + " | " + C.size());
            if (stopBoundsCounter > 0) {
                stopBounds = stopBoundsBuilder.build();
                pan(stopBounds);
            } else if (mMap != null) {
                LatLng cpp = new LatLng(34.056781, -117.821071);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cpp, 14.5f));
            }
        } else {
            hasPoly = -1;

        }
    }

    private void updateBusLocation(int route) {
        for (Marker m : BusMarkers)
            m.remove();
        BusMarkers.clear();
        switch (route) {
            case 3164:
                for (int i = 0; i < ABus.size(); i++) {
                    getLocation("bus", ABus.get(i), route, i);
                }
                break;
            case 3166:

                for (int i = 0; i < B1Bus.size(); i++) {
                    getLocation("bus", B1Bus.get(i), route, i);
                }
                break;
            case 3167:

                for (int i = 0; i < B2Bus.size(); i++) {
                    getLocation("bus", B2Bus.get(i), route, i);
                }
                break;
            case 3162:
                for (int i = 0; i < CBus.size(); i++) {
                    getLocation("bus", CBus.get(i), route, i);
                }
                break;
        }
    }

    private void pan(LatLngBounds b) {
        if (b != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(b.getCenter(), 14.5f));
        }
    }

    private List<StopInfo> getStopList(int routeID) {
        switch (routeID) {
            case 3164:
                return AStop;
            case 3166:
                return B1Stop;
            case 3167:
                return B2Stop;
            case 3162:
                return CStop;
        }
        return null;
    }

    private List<BusInfo> getBusList(int routeID) {
        switch (routeID) {
            case 3164:
                return ABus;
            case 3166:
                return B1Bus;
            case 3167:
                return B2Bus;
            case 3162:
                return CBus;
        }
        return null;
    }

    private View createInfo(Marker m) {
        String[] data = m.getSnippet().split("[ ]+");

        //main box
        linearLayout = new LinearLayout(this);
        linearLayout.setBackgroundColor(Color.WHITE);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        linearLayout.setWeightSum(100f);

        linearLayout.setLayoutParams(linearLayoutParams);

        //


        //horizontal box
        LinearLayout horiz = new LinearLayout(this);
        horiz.setBackgroundColor(Color.BLACK);
        horiz.setOrientation(LinearLayout.HORIZONTAL);
        horiz.setWeightSum(100f);

        LinearLayout.LayoutParams horizParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        horizParams.weight = 20f;

        horiz.setLayoutParams(horizParams);

        //

        //img view
        ImageView img = new ImageView(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        imgParams.weight = 35f;
        img.setScaleType(ImageView.ScaleType.FIT_CENTER);
        img.setBackgroundColor(Color.parseColor("#00000000"));
        img.setImageResource(R.drawable.ic_bus_stop_icon);

        img.setLayoutParams(imgParams);

        //

        //text view
        TextView title = new TextView(this);
        title.setText(m.getTitle());
        title.setTextColor(Color.WHITE);
        title.setBackgroundColor(Color.BLACK);
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

        innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        frameParams.weight = 80f;

        innerLayout.setLayoutParams(frameParams);

        switch (data[0]) {
            case "stop":
                StopInfo stopInfo = getStopList(Integer.parseInt(data[1])).get(Integer.parseInt(data[2]));

                TextView routes = new TextView(this);
                routes.setText("Routes: " + stopInfo.getOnRoute());
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

                TextView nextBus = new TextView(this);
                nextBus.setText(nextBusStr);
                nextBus.setTextColor(Color.BLACK);
                nextBus.setTextSize(24);

                LinearLayout.LayoutParams nextBusParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                nextBus.setLayoutParams(nextBusParams);

                TextView nextBusTime = new TextView(this);
                nextBusTime.setText(nextTime);
                nextBusTime.setTextColor(Color.BLACK);
                nextBusTime.setTextSize(24);

                LinearLayout.LayoutParams nextBusTimeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                nextBusTime.setLayoutParams(nextBusTimeParams);

                clearView();
                innerLayout.addView(routes);
                innerLayout.addView(nextBus);
                innerLayout.addView(nextBusTime);
                break;
            case "bus":
                BusInfo busInfo = getBusList(Integer.parseInt(data[1])).get(Integer.parseInt(data[2]));

                TextView busRoute = new TextView(this);
                busRoute.setText("Route: " + busInfo.getRoute());
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

                TextView fullness = new TextView(this);
                fullness.setText(fullnessStr);
                fullness.setTextColor(Color.BLACK);
                fullness.setTextSize(24);

                LinearLayout.LayoutParams fullnessParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                fullness.setLayoutParams(fullnessParams);

                TextView nextStop = new TextView(this);
                nextStop.setText("Next Stop: " + busInfo.getNextStop());
                nextStop.setTextColor(Color.BLACK);
                nextStop.setTextSize(24);

                LinearLayout.LayoutParams nextStopParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                nextStop.setLayoutParams(nextStopParams);

                clearView();
                innerLayout.addView(busRoute);
                innerLayout.addView(fullness);
                innerLayout.addView(nextStop);
                break;
        }

        linearLayout.addView(horiz);
        linearLayout.addView(innerLayout);

        return linearLayout;
    }

    private void clearView() {
        innerLayout.removeAllViews();
        linearLayout.removeAllViews();
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
                bottomDrawer.removeAllViews();
                drawerLayout.closeDrawers();
            }
        });
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                bottomDrawer.addView(createInfo(marker));
                drawerLayout.openDrawer(GravityCompat.END);
                return true;
            }
        });

    }
}
