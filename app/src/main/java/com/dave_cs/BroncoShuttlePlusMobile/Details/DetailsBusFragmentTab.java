package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;

import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfoAdapter;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfoService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.busViewExpandableListViewAdapter;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.SimpleRouteInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.SimpleRouteInfoService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.JacksonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by David on 1/20/2016.
 */
public class DetailsBusFragmentTab extends android.support.v4.app.Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<BusInfo> listA = new ArrayList<>();
    private ArrayList<BusInfo> listB1 = new ArrayList<>();
    private ArrayList<BusInfo> listB2 = new ArrayList<>();
    private ArrayList<BusInfo> listC = new ArrayList<>();

    private busViewExpandableListViewAdapter listAdapter;
    private ExpandableListView expandableListView;
    private List<String> headers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                listAdapter.removeAll();
                propagateBuses();
            }
        });

        expandableListView = (ExpandableListView) v.findViewById(R.id.busList);
        expandableListView.setItemsCanFocus(true);
//        expandableListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                BusInfo info = (BusInfo) listView.getItemAtPosition(position);
//                android.support.v4.app.Fragment newFrag = new DetailsAdvBusFragmentTab();
//                Bundle bundle = new Bundle();
//                bundle.putString("busName", info.getBusName());
//                newFrag.setArguments(bundle);
//                getFragmentManager()
//                        .beginTransaction()
//                        .replace(android.R.id.tabcontent, newFrag, info.getBusName() + "")
//                        .addToBackStack("bus")
//                        .commit();
//            }
//        });
        setUpList();
        propagateBuses();
        listAdapter = new busViewExpandableListViewAdapter(getContext(), headers, listA, listB1, listB2, listC);
        expandableListView.setAdapter(listAdapter);
        for(int i =0; i < listAdapter.getGroupCount(); i++)
        {
            expandableListView.expandGroup(i);
        }
        return v;
    }

    private void setUpList() {
        headers = new ArrayList<>();
        headers.add("Route A");
        headers.add("Route B1");
        headers.add("Route B2");
        headers.add("Route C");
    }

    private void propagateBuses() {
        // reach to server and pull route info
        String[] routes = {"A", "B1", "B2", "C"};

        for (final String str : routes) {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://dave-cs.com")
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();

            BusInfoService busInfoService = retrofit.create(BusInfoService.class);

            Call<List<BusInfo>> call = busInfoService.getInfo(str);
            call.enqueue(new Callback<List<BusInfo>>() {

                @Override
                public void onResponse(Response<List<BusInfo>> response) {
                    if (response.isSuccess()) {
                        switch (str){
                            case "A":
                                listA.addAll(response.body());
                                Collections.sort(listA);
                                break;
                            case "B1":
                                listB1.addAll(response.body());
                                Collections.sort(listB1);
                                break;
                            case "B2":
                                listB2.addAll(response.body());
                                Collections.sort(listB2);
                                break;
                            case "C":
                                listC.addAll(response.body());
                                Collections.sort(listC);
                                break;
                        }

                        listAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("<Error>", response.code() + ":" + response.message());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("<Error>", t.getLocalizedMessage() + "");
                }
            });

            if (swipeRefreshLayout != null)
                swipeRefreshLayout.setRefreshing(false);
        }
    }
}

