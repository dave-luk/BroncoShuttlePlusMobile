package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dave_cs.BroncoShuttlePlusMobile.Details.Advanced.DetailsAdvRouteActivity;
import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.RouteOnlineService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.SimpleRouteInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.SimpleRouteInfoAdapter;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.SimpleRouteInfoService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by David on 1/20/2016.
 */
public class DetailsRouteFragmentTab extends android.support.v4.app.Fragment implements Filterable {

    private static final String TAG = "DetailsRouteFragmentTab";

    private final ArrayList<String> routes = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<SimpleRouteInfo> listItems = new ArrayList<>();
    private ArrayList<SimpleRouteInfo> searchList = new ArrayList<>();
    private ListView listView;

    private SimpleRouteInfoAdapter adapter;

    @Override
    @SuppressWarnings("Unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        propagateHeaders();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.details_route_fragment_layout, container, false);

        //set up swipe refresh
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.routeList_refresh_widget);
        swipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.gold);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                propagateHeaders();
            }
        });

        //set up listView
        adapter = new SimpleRouteInfoAdapter(this.getContext(), listItems);

        listView = (ListView) v.findViewById(R.id.routeList);
        listView.setAdapter(adapter);
        listView.setItemsCanFocus(true);

        //make item clickable
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SimpleRouteInfo info = (SimpleRouteInfo) listView.getItemAtPosition(position);

                Intent intent = new Intent(getActivity(), DetailsAdvRouteActivity.class);
                intent.putExtra("routeName", info.getRouteName());
                startActivity(intent);
            }
        });

        return v;
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
                Log.i(TAG, "route head: " + Arrays.asList(response.body()).toString());
                if (response.isSuccess()) {
                    routes.addAll(Arrays.asList(response.body()));
                    for (String route : routes) {
                        propagateRoutes(route);
                    }
                }
            }

            @Override
            public void onFailure(Call<String[]> call, Throwable t) {
                Log.e(TAG, "route-head-fail: " + t.getLocalizedMessage());
            }
        });
    }

    private void propagateRoutes(String route) {
        // reach to server and pull route info
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        SimpleRouteInfoService simpleRouteInfoService = retrofit.create(SimpleRouteInfoService.class);
        Call<SimpleRouteInfo> call = simpleRouteInfoService.getInfo(route.replace("ROUTE ", ""));
        call.enqueue(new Callback<SimpleRouteInfo>() {

            @Override
            public void onResponse(Call<SimpleRouteInfo> call, Response<SimpleRouteInfo> response) {
                if (response.isSuccess()) {
                    boolean added = false;
                    for (SimpleRouteInfo s : listItems) {
                        if (s.compareTo(response.body()) == 0) {
                            added = true;
                        }

                    }
                    if (!added)
                        listItems.add(response.body());

                    Collections.sort(listItems);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e(TAG, "error: " + response.code() + ":" + response.message());
                }
                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<SimpleRouteInfo> call, Throwable t) {
                Log.e(TAG, "route-fail: " + t.getLocalizedMessage());
            }
        });
    }

    @Override
    public void filter(String query) {
        Log.i(TAG, "Query is: " + query);
        searchList.clear();
        for (SimpleRouteInfo s : listItems)
            if (s.getRouteName().toLowerCase().contains(query.toLowerCase()))
                searchList.add(s);
        SimpleRouteInfoAdapter searchAdapter = new SimpleRouteInfoAdapter(getContext(), searchList);
        listView.setAdapter(searchAdapter);
    }

    @Override
    public void clear() {
        listView.setAdapter(adapter);
    }
}