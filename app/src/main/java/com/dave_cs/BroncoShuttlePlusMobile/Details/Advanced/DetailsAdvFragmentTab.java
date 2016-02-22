package com.dave_cs.BroncoShuttlePlusMobile.Details.Advanced;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dave_cs.BroncoShuttlePlusMobile.Details.DetailsViewActivity;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.JacksonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by David on 2/7/2016.
 */
public class DetailsAdvFragmentTab extends Fragment implements OnMapReadyCallback {

    private SupportMapFragment mapFragment;
    private GoogleMap googleMap;
    private LatLng location = new LatLng(34.056781, -117.821071);
    private Marker currMarker;
    private ImageButton refocus;

    private FrameLayout detailsInfo;
    private LinearLayout linearLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout innerLayout;

    private Object info;

    private String name;
    private String type;
    private int id;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = getChildFragmentManager();
        mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map_frag);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.map_frag, mapFragment).commit();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DetailsViewActivity) getActivity()).setSuspend(true);

        // get the type from bundles, write to lat-lng
        try {
            int stopID = getArguments().getInt("stopNumber");
            String stopName = getArguments().getString("stopName");
            int busID = getArguments().getInt("busNumber");
            String busName = getArguments().getString("busName");
            if (stopID != 0) {
                name = stopName;
                type = "stop";
                id = stopID;
            } else if (busID != 0) {
                name = busName;
                type = "bus";
                id = busID;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        getLocation();
        getInfo();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.details_adv_fragment_layout, container, false);
        refocus = (ImageButton) v.findViewById(R.id.refocus_icon);
        detailsInfo = (FrameLayout) v.findViewById(R.id.details_frag_holder);

        swipeRefreshLayout = new SwipeRefreshLayout(getContext());
        swipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.gold);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getInfo();
            }
        });

        if (detailsInfo != null) {
            linearLayout = new LinearLayout(getContext());
            linearLayout.setBackgroundColor(Color.WHITE);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            linearLayout.setWeightSum(100f);
            linearLayout.setLayoutParams(linearLayoutParams);

            initInfo();
        }

        refocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (googleMap != null) {
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 16.5f));
                }
            }
        });
        return v;
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
        ((DetailsViewActivity) getActivity()).setSuspend(false);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("<Maps>", "map ready");
        this.googleMap = googleMap;

        Log.d("<Maps>", "got a location. displaying" + location.toString());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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

        getChildFragmentManager().beginTransaction().replace(R.id.map_frag, mapFragment).commit();

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                currMarker.showInfoWindow();
            }
        });
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (!marker.isInfoWindowShown()) {
                    marker.showInfoWindow();
                }
                return true;
            }
        });

        if (mapFragment == null) {
            Log.d("<Map>", "replacing frag");
            mapFragment = SupportMapFragment.newInstance();
        }

        getChildFragmentManager().beginTransaction().replace(R.id.map_frag, mapFragment).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (googleMap == null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initInfo() {

        TextView title = new TextView(getContext());
        title.setText(name);
        title.setTextColor(Color.WHITE);
        title.setBackgroundColor(Color.BLACK);
        title.setSingleLine(true);
        title.setEllipsize(TextUtils.TruncateAt.END);
        title.setTextSize(24);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        titleParams.weight = 30f;
        title.setLayoutParams(titleParams);

        innerLayout = new LinearLayout(getContext());
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams frameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        frameParams.weight = 70f;
        innerLayout.setLayoutParams(frameParams);

        if (info != null) {
            switch (type) {
                case "stop":
                    StopInfo stopInfo = (StopInfo) info;

                    TextView routes = new TextView(getContext());
                    routes.setText("Routes: " + stopInfo.getOnRoute());
                    routes.setTextColor(Color.BLACK);
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

                    TextView nextBus = new TextView(getContext());
                    nextBus.setText(nextBusStr);
                    nextBus.setTextColor(Color.BLACK);
                    nextBus.setTextSize(20);

                    LinearLayout.LayoutParams nextBusParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    nextBus.setLayoutParams(nextBusParams);

                    TextView nextBusTime = new TextView(getContext());
                    nextBusTime.setText(nextTime);
                    nextBusTime.setTextColor(Color.BLACK);
                    nextBusTime.setTextSize(20);

                    LinearLayout.LayoutParams nextBusTimeParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    nextBusTime.setLayoutParams(nextBusTimeParams);

                    clearView();
                    innerLayout.addView(routes);
                    innerLayout.addView(nextBus);
                    innerLayout.addView(nextBusTime);
                    break;
                case "bus":
                    BusInfo busInfo = (BusInfo) info;

                    TextView busRoute = new TextView(getContext());
                    busRoute.setText("Route: " + busInfo.getRoute());
                    busRoute.setTextColor(Color.BLACK);
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

                    TextView fullness = new TextView(getContext());
                    fullness.setText(fullnessStr);
                    fullness.setTextColor(Color.BLACK);
                    fullness.setTextSize(20);

                    LinearLayout.LayoutParams fullnessParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    fullness.setLayoutParams(fullnessParams);

                    TextView nextStop = new TextView(getContext());
                    nextStop.setText("Next Stop: " + busInfo.getNextStop());
                    nextStop.setTextColor(Color.BLACK);
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
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.addView(linearLayout);
        detailsInfo.addView(swipeRefreshLayout);
    }

    private void clearView() {
        innerLayout.removeAllViews();
        linearLayout.removeAllViews();
        swipeRefreshLayout.removeView(linearLayout);
        detailsInfo.removeAllViews();
    }

    private void getInfo() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        switch (type) {
            case "stop":
                StopInfoService stopInfoService = retrofit.create(StopInfoService.class);

                Call<StopInfo> stopCall = stopInfoService.getInfo(Integer.toString(id));
                stopCall.enqueue(new Callback<StopInfo>() {
                    @Override
                    public void onResponse(Response<StopInfo> response) {
                        if (response.isSuccess()) {
                            info = response.body();
                            initInfo();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("<Error>", t.getLocalizedMessage() + "");
                    }
                });
                break;
            case "bus":
                BusInfoService busInfoService = retrofit.create(BusInfoService.class);

                Call<BusInfo> busCall = busInfoService.getInfo(Integer.toString(id));
                busCall.enqueue(new Callback<BusInfo>() {
                    @Override
                    public void onResponse(Response<BusInfo> response) {
                        if (response.isSuccess()) {
                            info = response.body();
                            initInfo();
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("<Error>", t.getLocalizedMessage() + "");
                    }
                });
                break;
        }
    }

    private void getLocation() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        LocationService locationService = retrofit.create(LocationService.class);

        switch (type) {
            case "stop":
                Log.d("<Query>", "the stop id passed is: " + id);
                Call<Location> stopCall = locationService.getLocation(id, null);
                stopCall.enqueue(new Callback<Location>() {

                    @Override
                    public void onResponse(Response<Location> response) {
                        if (response.isSuccess()) {
                            Location l = response.body();
                            location = new LatLng(l.getLat(), l.getLng());
                            Log.d("<Location>", "location is:" + location);
                            initMarker();
                        } else {
                            Log.e("<Error>", response.code() + ":" + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("<Error>", t.getLocalizedMessage() + "");
                    }
                });
                break;
            case "bus":
                Log.d("<Query>", "the bus id passed is: " + id);
                Call<Location> busCall = locationService.getLocation(null, id);
                busCall.enqueue(new Callback<Location>() {

                    @Override
                    public void onResponse(Response<Location> response) {
                        if (response.isSuccess()) {
                            Location l = response.body();
                            location = new LatLng(l.getLat(), l.getLng());
                            initMarker();
                        } else {
                            Log.e("<Error>", response.code() + ":" + response.message());
                        }
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("<Error>", t.getLocalizedMessage() + "");
                    }
                });
                break;
        }
    }

    private void initMarker() {
        if (googleMap != null) {
            Log.d("<Marker>", "Updated marker to: " + location);
            googleMap.clear();
            currMarker = googleMap.addMarker(new MarkerOptions().position(location).title(name).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_bus_icon)));
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
}


