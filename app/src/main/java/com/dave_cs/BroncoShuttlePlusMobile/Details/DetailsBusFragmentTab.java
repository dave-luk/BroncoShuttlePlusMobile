package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.dave_cs.BroncoShuttlePlusMobile.Details.Advanced.DetailsAdvFragmentTab;
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
public class DetailsBusFragmentTab extends android.support.v4.app.Fragment {

    private final ArrayList<String> routes = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<ArrayList<BusInfo>> masterList = new ArrayList<>();

    private busViewExpandableListViewAdapter listAdapter;
    private ExpandableListView expandableListView;
    private ArrayList<String> headers = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        propagateHeaders();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.details_bus_fragment_layout, container, false);

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
                android.support.v4.app.Fragment newFrag = new DetailsAdvFragmentTab();
                Bundle bundle = new Bundle();
                BusInfo busInfo = (BusInfo) listAdapter.getChild(groupPosition, childPosition);
                bundle.putString("busName", busInfo.getBusName());
                bundle.putInt("busNumber", busInfo.getBusNumber());
                newFrag.setArguments(bundle);
                getFragmentManager()
                        .beginTransaction()
                        .hide(DetailsBusFragmentTab.this)
                        .add(android.R.id.tabcontent, newFrag, "bus frag")
                        .addToBackStack("simpleBus")
                        .commit();
                return true;
            }
        });


        return v;
    }

    private void setUpList() {
        headers = new ArrayList<>();
        headers = routes;
        Log.d("<LIST>", "List size is : " + headers.size());
        for (String s : headers)
            masterList.add(new ArrayList<BusInfo>());
        Log.d("<LIST>", "MASTER List size is : " + masterList.size());
        propagateBuses();
    }

    private void finalizeList() {
        Log.e("<List>", "finalized");
        listAdapter = new busViewExpandableListViewAdapter(getContext(), headers, masterList);

        expandableListView.setAdapter(listAdapter);
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            expandableListView.expandGroup(i);
        }
    }

    private void propagateHeaders() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        RouteOnlineService routeOnlineService = retrofit.create(RouteOnlineService.class);
        Call<String[]> data = routeOnlineService.getInfo();
        data.enqueue(new Callback<String[]>() {
            @Override
            public void onResponse(Call<String[]> call, Response<String[]> response) {
                Log.d("<ROUTE_HEAD>", Arrays.asList(response.body()).toString());
                if (response.isSuccess()) {
                    routes.addAll(Arrays.asList(response.body()));
                    setUpList();
                }
            }

            @Override
            public void onFailure(Call<String[]> call, Throwable t) {
                Log.e("<FAIL-ROUTE>", t.getLocalizedMessage() + "");
            }
        });
    }

    private void propagateBuses() {
        // reach to server and pull route info
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://dave-cs.com")
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();
        for (final String str : routes) {

            BusListService busListService = retrofit.create(BusListService.class);

            Call<List<BusInfo>> call = busListService.getInfo(str.replace("ROUTE ", ""));
            call.enqueue(new Callback<List<BusInfo>>() {

                @Override
                public void onResponse(Call<List<BusInfo>> call, Response<List<BusInfo>> response) {
                    if (response.isSuccess()) {
                        masterList.get(headers.indexOf(str)).addAll(response.body());


                        if (headers.indexOf(str) == headers.size() - 1) {
                            Log.d("<DEBUG>", "last reached");
                            finalizeList();
                            listAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("<Error>", response.code() + ":" + response.message());
                        }
                    if (swipeRefreshLayout != null)
                        swipeRefreshLayout.setRefreshing(false);
                    }

                @Override
                public void onFailure(Call<List<BusInfo>> call, Throwable t) {
                    Log.e("<Error>", t.getLocalizedMessage() + "");
                }
            });
        }
    }
}

