package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dave_cs.BroncoShuttlePlusMobile.Details.Advanced.DetailsAdvRouteFragmentTab;
import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.SimpleRouteInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.SimpleRouteInfoAdapter;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.SimpleRouteInfoService;

import java.util.ArrayList;
import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.JacksonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by David on 1/20/2016.
 */
public class DetailsRouteFragmentTab extends android.support.v4.app.Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<SimpleRouteInfo> listItems = new ArrayList<>();
    private SimpleRouteInfoAdapter adapter;

    @Override
    @SuppressWarnings("Unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        propagateRoutes();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.details_route_fragment_layout, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.routeList_refresh_widget);
        swipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.gold);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adapter.clear();
                propagateRoutes();
            }
        });

        adapter = new SimpleRouteInfoAdapter(this.getContext(), listItems);

        final ListView listView = (ListView) v.findViewById(R.id.routeList);
        listView.setAdapter(adapter);
        listView.setItemsCanFocus(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SimpleRouteInfo info = (SimpleRouteInfo) listView.getItemAtPosition(position);
                android.support.v4.app.Fragment newFrag = new DetailsAdvRouteFragmentTab();
                Bundle bundle = new Bundle();
                bundle.putString("routeName", info.getRouteName());
                newFrag.setArguments(bundle);
                getFragmentManager()
                        .beginTransaction()
                        .hide(DetailsRouteFragmentTab.this)
                        .add(android.R.id.tabcontent, newFrag, info.getRouteName() + "")
                        .addToBackStack("simpleRoute")
                        .commit();
            }
        });
        return v;
    }

    private void propagateRoutes(){
        // reach to server and pull route info

        String[] routes = {"A", "B1", "B2", "C"};

        for(String str: routes) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://dave-cs.com")
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();

            SimpleRouteInfoService simpleRouteInfoService = retrofit.create(SimpleRouteInfoService.class);
            Call<SimpleRouteInfo> call = simpleRouteInfoService.getInfo(str);
            call.enqueue(new Callback<SimpleRouteInfo>() {

                @Override
                public void onResponse(Response<SimpleRouteInfo> response) {
                    if (response.isSuccess()) {
                        boolean added = false;
                        for(SimpleRouteInfo s: listItems)
                        {
                            if(s.compareTo(response.body()) == 0)
                            {
                                added = true;
                            }

                        }
                        if(!added)
                            listItems.add(response.body());

                        Collections.sort(listItems);
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("<Error>",response.code() +":" + response.message());
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("<FAIL-ROUTE>", t.getLocalizedMessage() + "");
                }
            });
        }
        if(swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }
}