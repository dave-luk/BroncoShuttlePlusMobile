package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfoAdapter;
import com.dave_cs.BroncoShuttlePlusServerUtil.OnSwipeTouchListener;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.AdvRouteViewExpandableListViewAdapter;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.RouteInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.RouteInfoService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfoAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.JacksonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by David on 1/25/2016.
 */
public class DetailsAdvRouteFragmentTab extends android.support.v4.app.Fragment {

    private RouteInfo routeInfo;

    private List<BusInfo> busInfoList = new ArrayList<>();

    private List<StopInfo> stopInfoList = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    private AdvRouteViewExpandableListViewAdapter listAdapter;
    private ExpandableListView expandableListView;
    private List<String> headers;


    private String routeName;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected()) {
            routeName = getArguments().getString("routeName");
            propagate();
        }
        else {
            busInfoList.add(new BusInfo());
            stopInfoList.add(new StopInfo());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.details_adv_route_fragment_layout, container, false);

        OnSwipeTouchListener swipeTouchListener = new OnSwipeTouchListener(getActivity()){
            @Override
            public void onSwipeLeft() {
                if(getFragmentManager() != null)
                getFragmentManager().popBackStackImmediate();
            }
        };

        ((DetailsViewActivity) getActivity()).registerSwipeListener(swipeTouchListener);

        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.adv_routeList_refresh_widget);
        swipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.gold);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                listAdapter.removeAll();
                propagate();
            }
        });

        expandableListView = (ExpandableListView) v.findViewById(R.id.expLV);
        expandableListView.setItemsCanFocus(true);
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                return true;
                //work here
            }
        });
        setUpList();
        listAdapter = new AdvRouteViewExpandableListViewAdapter(getContext(), headers, busInfoList, stopInfoList);
        expandableListView.setAdapter(listAdapter);
        for(int i =0; i < listAdapter.getGroupCount(); i++)
        {
            expandableListView.expandGroup(i);
        }

        return v;
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

        final RouteInfoService routeInfoService = retrofit.create(RouteInfoService.class);
            Call<RouteInfo> call = routeInfoService.getInfo(routeName.replace("ROUTE ",""));
            call.enqueue(new Callback<RouteInfo>() {

                @Override
                public void onResponse(Response<RouteInfo> response) {
                    if (response.isSuccess()) {
                        Log.d("<Success>","received data");
                        routeInfo = response.body();
                        listAdapter.removeAll();
                        listAdapter.add(routeInfo.getBusOnRoute());
                        listAdapter.add(routeInfo.getStopsOnRoute());
                    } else {
                        Log.d("<Error>", "" + response.code());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("<Error>", t.getLocalizedMessage());
                    Log.i("TEST", listAdapter.getChildrenCount(1) + " " + ((StopInfo)listAdapter.getChild(1, 0)).getName());
                }
            });
        if(swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }
}

