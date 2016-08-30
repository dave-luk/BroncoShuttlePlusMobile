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
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.RouteOnlineService;
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
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public class LiveMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final ArrayList<String> routes = new ArrayList<>();
    private GoogleMap mMap;
    private int polyReady, stopsReady, busReady;
    private ArrayList<ArrayList<LatLng>> masterPolyList = new ArrayList<>();
    private ArrayList<ArrayList<StopInfo>> masterStopList = new ArrayList<>();
    private ArrayList<ArrayList<BusInfo>> masterBusList = new ArrayList<>();

    private ArrayList<ArrayList<Marker>> BusMarkers = new ArrayList<>();
    private LatLngBounds.Builder boundsBuilder;
    private LatLngBounds.Builder stopBoundsBuilder = new LatLngBounds.Builder();
    private LatLngBounds bounds;
    private LatLngBounds stopBounds;
    private int stopBoundsCounter;

    private ArrayList<PolylineOptions> masterRouteOptions = new ArrayList<>();
    private String[] routeColors = {"#BBFF0000", "#BB0000FF", "#BBFFFF00", "#BB335933", "#BBFF00FF"};

    private int hasPoly = -1;

    private DrawerLayout drawerLayout;
    private ListView leftDrawer;
    private FrameLayout bottomDrawer;

    private LinearLayout linearLayout;
    private LinearLayout innerLayout;

    private Handler uiCallback = new Handler() {
        public void handleMessage(Message msg) {
            if (hasPoly != -1)
                updateBusLocation(Integer.parseInt(routes.get(hasPoly)));
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

        propagateRoutes();

        drawerLayout = (DrawerLayout) findViewById(R.id.liveMap_drawer_layout);
        leftDrawer = (ListView) findViewById(R.id.liveMap_left_drawer);
        bottomDrawer = (FrameLayout) findViewById(R.id.liveMap_bottom_drawer);

    }

    private void finalizeList() {
        leftDrawer.setAdapter(new ArrayAdapter<>(this, R.layout.item_live_map_drawer_item, R.id.item_liveMap_drawer_item, routes));
        leftDrawer.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                togglePolyline(Integer.parseInt(routes.get(position)), ((hasPoly == -1 || hasPoly != position) ? true : false));
                drawerLayout.closeDrawers();
            }
        });
    }

    private void propagateRoutes() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        RouteOnlineService routeOnlineService = retrofit.create(RouteOnlineService.class);
        Call<String[]> data = routeOnlineService.getInfo("number");
        data.enqueue(new Callback<String[]>() {
            @Override
            public void onResponse(Call<String[]> call, Response<String[]> response) {
                Log.d("<ROUTE_HEAD>", Arrays.asList(response.body()).toString());
                if (response.isSuccess()) {
                    routes.addAll(Arrays.asList(response.body()));
                    for (String s : routes) {
                        masterPolyList.add(new ArrayList<LatLng>());
                        masterStopList.add(new ArrayList<StopInfo>());
                        masterBusList.add(new ArrayList<BusInfo>());
                        masterRouteOptions.add(new PolylineOptions());
                        getPolyLine(s);
                        getRouteInfo(s);
                        getBusInfo(s);
                    }
                    finalizeList();
                }
            }

            @Override
            public void onFailure(Call<String[]> call, Throwable t) {
                Log.e("<FAIL-ROUTE>", t.getLocalizedMessage() + "");
            }
        });
    }

    private void getPolyLine(final String route) {
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

        Call<List<Location>> locationCall = polyLineService.polyList(Integer.parseInt(route));
        locationCall.enqueue(new Callback<List<Location>>() {
            @Override
            public void onResponse(Call<List<Location>> call, Response<List<Location>> response) {
                if (response.body() != null) {
                    int i = routes.indexOf(route);
                    for (Location l : response.body()) {
                        masterPolyList.get(i).add(l.parseLatLng());
                    }
                    polyReady++;
                }
            }

            @Override
            public void onFailure(Call<List<Location>> call, Throwable t) {
                Log.e("<Error>", t.getLocalizedMessage());
            }
        });
    }

    private void getRouteInfo(final String r) {
        // reach to server and pull route info
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        RouteStopService routeInfoService = retrofit.create(RouteStopService.class);
        Call<List<StopInfo>> call = routeInfoService.getrouteStops(Integer.parseInt(r));
        call.enqueue(new Callback<List<StopInfo>>() {

            @Override
            public void onResponse(Call<List<StopInfo>> call, Response<List<StopInfo>> response) {
                if (response.isSuccess()) {
                    Log.d("<Success>", "received data");
                    masterStopList.get(routes.indexOf(r)).addAll(response.body());
                    stopsReady++;
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

        final BusListService busListService = retrofit.create(BusListService.class);

        Call<List<BusInfo>> call = busListService.getInfo(str);
        call.enqueue(new Callback<List<BusInfo>>() {

            @Override
            public void onResponse(Call<List<BusInfo>> call, Response<List<BusInfo>> response) {
                if (response.isSuccess()) {
                    masterBusList.get(routes.indexOf(str)).addAll(response.body());
                    busReady++;
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
                try {
                    Marker curr = BusMarkers.get(routes.indexOf(route)).get(index);
                    if (curr.getTitle().equals(((BusInfo) o).getBusName()))
                        curr.setPosition(location);
                } catch (Exception e) {

                }
                BusMarkers.get(routes.indexOf(route)).add(mMap.addMarker(new MarkerOptions().position(location).title(((BusInfo) o).getBusName()).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus_icon))));
            }


        }
    }

    public void togglePolyline(int route, boolean state) {
        if (polyReady != busReady && busReady != stopsReady && stopsReady != routes.size()) return;

        mMap.clear();

        if (state) {
            boundsBuilder = new LatLngBounds.Builder();

            int index = routes.indexOf(Integer.toString(route));
            Log.d("<List>", routes.toString());
            Log.d("<PolyLine>", "Index is: " + index + " | " + route);

            if (!masterPolyList.get(index).isEmpty()) {
                Log.d("<PolyLine>", "has poly line!");

                for (LatLng point : masterPolyList.get(index)) {
                    boundsBuilder.include(point);
                    masterRouteOptions.get(index).add(point);
                    Log.d("<PolyLine>", "has point: " + point.toString());
                }
                bounds = boundsBuilder.build();

                mMap.addPolyline(masterRouteOptions.get(index).color(Color.parseColor(routeColors[index])));
                hasPoly = index;
            }

            for (int i = 0; i < masterStopList.get(index).size(); i++) {
                getLocation("stop", masterStopList.get(index).get(i), route, i);
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
        int index = routes.indexOf(route);
        if (index != -1)
            for (int i = 0; i < BusMarkers.get(index).size(); i++)
                getLocation("bus", masterBusList.get(index).get(i), route, i);
    }

    private void pan(LatLngBounds b) {
        if (b != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(b.getCenter(), 14.5f));
        }
    }

    private List<StopInfo> getStopList(String routeID) {
        try {
            int index = routes.indexOf(routeID);
            return masterStopList.get(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private List<BusInfo> getBusList(String routeID) {
        try {
            int index = routes.indexOf(routeID);
            return masterBusList.get(index);
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    private View createInfo(Marker m) {
        String[] data = m.getSnippet().split("[ ]+");
        Log.d("<data>", "" + data[0] + "|" + data[1] + "|" + data[2]);
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
                StopInfo stopInfo = getStopList(data[1]).get(Integer.parseInt(data[2]));

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
                BusInfo busInfo = getBusList(data[1]).get(Integer.parseInt(data[2]));

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
