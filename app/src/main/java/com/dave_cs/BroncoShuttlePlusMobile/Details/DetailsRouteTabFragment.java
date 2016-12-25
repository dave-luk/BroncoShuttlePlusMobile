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

import com.dave_cs.BroncoShuttlePlusMobile.Application.Data.DetailsViewHeaderData;
import com.dave_cs.BroncoShuttlePlusMobile.Application.Data.DetailsViewRouteData;
import com.dave_cs.BroncoShuttlePlusMobile.Application.DataUpdateApplication;
import com.dave_cs.BroncoShuttlePlusMobile.Details.Advanced.DetailsAdvRouteActivity;
import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.SimpleRouteInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.SimpleRouteInfoAdapter;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by David on 1/20/2016.
 */
public class DetailsRouteTabFragment extends android.support.v4.app.Fragment implements Filterable, Observer {

    private static final String TAG = "DetailsRouteTabFragment";

    public ArrayList<String> routes = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<SimpleRouteInfo> listItems = new ArrayList<>();
    private ArrayList<SimpleRouteInfo> searchList = new ArrayList<>();
    private ListView listView;

    private SimpleRouteInfoAdapter adapter;

    private boolean error = false;

    @Override
    @SuppressWarnings("Unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DataUpdateApplication) getActivity().getApplication()).detailsViewData.detailsViewHeaderData.addObserver(this);
        ((DataUpdateApplication) getActivity().getApplication()).detailsViewData.detailsViewRouteData.addObserver(this);
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
                ((ViewPagerDetailsViewActivity) getActivity()).requestUpdate(0);
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

        if (!((DataUpdateApplication) getActivity().getApplication()).detailsViewData.detailsViewHeaderData.routes.isEmpty()) {
            this.routes = ((DataUpdateApplication) getActivity().getApplication()).detailsViewData.detailsViewHeaderData.routes;
            if (((DataUpdateApplication) getActivity().getApplication()).detailsViewData.detailsViewRouteData.simpleRouteInfoList.size() == routes.size()) {
                this.listItems = ((DataUpdateApplication) getActivity().getApplication()).detailsViewData.detailsViewRouteData.simpleRouteInfoList;
                adapter = new SimpleRouteInfoAdapter(getContext(), listItems);
                listView.setAdapter(adapter);

                error = false;
            }
        } else {
            error = true;
        }


        if (error) {
            ((ViewPagerDetailsViewActivity) getActivity()).errorBoxLinearLayout.setVisibility(View.VISIBLE);
        }

        return v;
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

    @Override
    public void update(Observable observable, Object data) {
        Log.i(TAG, "Updating!");
        if (observable instanceof DetailsViewRouteData) {
            if (((DetailsViewRouteData) observable).simpleRouteInfoList.size() == routes.size()) {
                Log.i(TAG, "matching header size");
                this.listItems = ((DetailsViewRouteData) observable).simpleRouteInfoList;
                //if not detached.
                if (getContext() != null) {
                    adapter = new SimpleRouteInfoAdapter(getContext(), listItems);
                    listView.setAdapter(adapter);

                    error = false;

                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }
        }
        if (observable instanceof DetailsViewHeaderData) {
            if (!((DetailsViewHeaderData) observable).routes.isEmpty()) {
                this.routes = ((DetailsViewHeaderData) observable).routes;
                error = false;
            }
        }
    }
}