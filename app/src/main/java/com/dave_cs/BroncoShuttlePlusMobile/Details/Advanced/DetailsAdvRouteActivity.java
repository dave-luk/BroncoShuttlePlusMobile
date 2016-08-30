package com.dave_cs.BroncoShuttlePlusMobile.Details.Advanced;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.AdvRouteViewExpandableListViewAdapter;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.RouteInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.RouteInfoService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfo;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by David on 1/25/2016.
 */
public class DetailsAdvRouteActivity extends AppCompatActivity {

    private RouteInfo routeInfo;

    private List<BusInfo> busInfoList = new ArrayList<>();

    private List<StopInfo> stopInfoList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    private AdvRouteViewExpandableListViewAdapter listAdapter;
    private TextView textView;
    private ExpandableListView expandableListView;
    private List<String> headers;


    private String routeName;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null && networkInfo.isConnected()) {
            routeName = getIntent().getExtras().getString("routeName");
            propagate();
        }
        else {
            busInfoList.add(new BusInfo());
            stopInfoList.add(new StopInfo());
        }

        setContentView(R.layout.activity_details_adv_route);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.adv_routeList_refresh_widget);
        swipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.gold);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listAdapter.removeAll();
                propagate();
            }
        });

        expandableListView = (ExpandableListView) findViewById(R.id.expLV);
        expandableListView.setItemsCanFocus(true);
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                //android.support.v4.app.Fragment newFrag = new DetailsAdvFragmentTab();
                Intent intent = new Intent(DetailsAdvRouteActivity.this, DetailsAdvActivity.class);

                switch (groupPosition) {
                    case 0:
                        BusInfo busInfo = (BusInfo) listAdapter.getChild(groupPosition, childPosition);
                        intent.putExtra("busName", busInfo.getBusName());
                        intent.putExtra("busNumber", busInfo.getBusNumber());
                        startActivity(intent);
                        break;
                    case 1:
                        StopInfo stopInfo = (StopInfo) listAdapter.getChild(groupPosition, childPosition);
                        intent.putExtra("stopName", stopInfo.getName());
                        intent.putExtra("stopNumber", stopInfo.getStopNumber());
                        startActivity(intent);
                }
                return true;
            }
        });
        setUpList();
        listAdapter = new AdvRouteViewExpandableListViewAdapter(this, headers, busInfoList, stopInfoList);
        expandableListView.setAdapter(listAdapter);
        for(int i =0; i < listAdapter.getGroupCount(); i++)
        {
            expandableListView.expandGroup(i);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.advBar);
        toolbar.setTitle(routeName);
        setSupportActionBar(toolbar);
    }

    private void setUpList() {
        headers = new ArrayList<>();
        headers.add("Shuttle");
        headers.add("Stops");
    }

    private void propagate() {
        // reach to server and pull route info
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        RouteInfoService routeInfoService = retrofit.create(RouteInfoService.class);
            Call<RouteInfo> call = routeInfoService.getInfo(routeName.replace("ROUTE ",""));
            call.enqueue(new Callback<RouteInfo>() {

                @Override
                public void onResponse(Call<RouteInfo> call, Response<RouteInfo> response) {
                    if (response.isSuccess()) {
                        Log.d("<Success>","received data");
                        routeInfo = response.body();
                        listAdapter.removeAll();
                        listAdapter.add(routeInfo.getBusOnRoute());
                        listAdapter.add(routeInfo.getStopsOnRoute());
                        listAdapter.notifyDataSetChanged();
                        Log.d("<data>", "data got and notified");
                    } else {
                        Log.d("<Error>", "" + response.code());
                    }
                    if (swipeRefreshLayout != null)
                        swipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onFailure(Call<RouteInfo> call, Throwable t) {
                    Log.e("<Error>", t.getLocalizedMessage());
                    Log.i("TEST", listAdapter.getChildrenCount(1) + " " + ((StopInfo)listAdapter.getChild(1, 0)).getName());
                }
            });
    }
}

