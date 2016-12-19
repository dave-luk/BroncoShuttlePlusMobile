package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.dave_cs.BroncoShuttlePlusMobile.Details.Advanced.DetailsAdvActivity;
import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusListService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.busViewExpandableListViewAdapter;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.RouteOnlineService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by David on 1/20/2016.
 */
public class DetailsBusFragmentTab extends android.support.v4.app.Fragment implements Filterable {

    private static final String TAG = "DetailsBusFragmentTab";

    private ArrayList<String> routes = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<ArrayList<BusInfo>> masterList = new ArrayList<>();

    private busViewExpandableListViewAdapter listAdapter;
    private ExpandableListView expandableListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.details_bus_fragment_layout, container, false);
        routes = new ArrayList<>();
        masterList = new ArrayList<>();
        propagateHeaders();

        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.busList_refresh_widget);
        swipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.gold);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (listAdapter != null)
                    listAdapter.removeAll();
                propagateHeaders();
            }
        });

        expandableListView = (ExpandableListView) v.findViewById(R.id.busList);

        expandableListView.setItemsCanFocus(true);
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                BusInfo busInfo = (BusInfo) listAdapter.getChild(groupPosition, childPosition);

                Intent intent = new Intent(getActivity(), DetailsAdvActivity.class);
                intent.putExtra("busName", busInfo.getBusName());
                intent.putExtra("busNumber", busInfo.getBusNumber());
                startActivity(intent);

                return true;
            }
        });

        return v;
    }

    private void setUpList() {
        Log.i(TAG, "List size is : " + routes.size());
        for (String route : routes) {
            masterList.add(new ArrayList<BusInfo>());
            propagateBuses(route);
        }
        listAdapter = new busViewExpandableListViewAdapter(getContext(), routes, masterList);
        expandableListView.setAdapter(listAdapter);
        Log.i(TAG, "MASTER List size is : " + masterList.size());
    }

    private void propagateHeaders() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        RouteOnlineService routeOnlineService = retrofit.create(RouteOnlineService.class);
        Call<String[]> data = routeOnlineService.getInfo("name");
        data.enqueue(new Callback<String[]>() {
            @Override
            public void onResponse(Call<String[]> call, Response<String[]> response) {
                Log.i(TAG, "route header" + Arrays.asList(response.body()).toString());
                if (response.isSuccess()) {
                    routes.addAll(Arrays.asList(response.body()));
                    setUpList();
                }
            }

            @Override
            public void onFailure(Call<String[]> call, Throwable t) {
                Log.e(TAG, "route-header-fail" + t.getLocalizedMessage());
            }
        });
    }

    private void propagateBuses(final String route) {
        // reach to server and pull route info
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();


        BusListService busListService = retrofit.create(BusListService.class);

        Call<List<BusInfo>> call = busListService.getInfo(route.replace("ROUTE ", ""));
        call.enqueue(new Callback<List<BusInfo>>() {
            @Override
            public void onResponse(Call<List<BusInfo>> call, Response<List<BusInfo>> response) {
                if (response.isSuccess()) {
                    int index = routes.indexOf(route);
                    masterList.get(index).addAll(response.body());
                    listAdapter.notifyDataSetChanged();
                    expandableListView.expandGroup(index);

                } else {
                    Log.e(TAG, response.code() + ":" + response.message());
                }
                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<BusInfo>> call, Throwable t) {
                Log.e(TAG, "bus-fail" + t.getLocalizedMessage());
            }
        });

    }

    @Override
    public void filter(String query) {
        //do nothing
    }

    @Override
    public void clear() {
        //do nothing
    }
}

