package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.dave_cs.BroncoShuttlePlusMobile.Application.Data.DetailsViewBusData;
import com.dave_cs.BroncoShuttlePlusMobile.Application.Data.DetailsViewHeaderData;
import com.dave_cs.BroncoShuttlePlusMobile.Application.DataUpdateApplication;
import com.dave_cs.BroncoShuttlePlusMobile.Details.Advanced.DetailsAdvActivity;
import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.busViewExpandableListViewAdapter;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by David on 1/20/2016.
 */
public class DetailsBusTabFragment extends android.support.v4.app.Fragment implements Filterable, Observer {

    private static final String TAG = "DetailsBusTabFragment";

    public ArrayList<String> routes = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<ArrayList<BusInfo>> masterList = new ArrayList<>();

    private busViewExpandableListViewAdapter listAdapter;
    private ExpandableListView expandableListView;

    private boolean error = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DataUpdateApplication) getActivity().getApplication()).detailsViewData.detailsViewHeaderData.addObserver(this);
        ((DataUpdateApplication) getActivity().getApplication()).detailsViewData.detailsViewBusData.addObserver(this);
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
                ((ViewPagerDetailsViewActivity) getActivity()).requestUpdate(0);
            }
        });

        expandableListView = (ExpandableListView) v.findViewById(R.id.busList);
        listAdapter = new busViewExpandableListViewAdapter(getContext(), routes, masterList);
        expandableListView.setAdapter(listAdapter);

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

        if (!((DataUpdateApplication) getActivity().getApplication()).detailsViewData.detailsViewHeaderData.routes.isEmpty()) {
            this.routes = ((DataUpdateApplication) getActivity().getApplication()).detailsViewData.detailsViewHeaderData.routes;
            if (((DataUpdateApplication) getActivity().getApplication()).detailsViewData.detailsViewBusData.busInfoList.size() == routes.size()) {
                this.masterList = ((DataUpdateApplication) getActivity().getApplication()).detailsViewData.detailsViewBusData.busInfoList;
                listAdapter = new busViewExpandableListViewAdapter(getContext(), routes, masterList);
                expandableListView.setAdapter(listAdapter);
                Log.i(TAG, "preloaded! busroute");
                expandAll();
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
        //do nothing
    }

    @Override
    public void clear() {
        //do nothing
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.i(TAG, "Updating!");
        if (observable instanceof DetailsViewBusData) {
            if (!((DetailsViewBusData) observable).busInfoList.isEmpty()) {
                this.masterList = ((DetailsViewBusData) observable).busInfoList;
                listAdapter.notifyDataSetChanged();

                if (expandableListView != null) {
                    expandableListView.setAdapter(listAdapter);
                    expandAll();
                }
                error = false;

                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(false);
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

    private void expandAll() {
        for (int i = 0; i < masterList.size(); i++) {
            expandableListView.expandGroup(i);
        }
    }
}
