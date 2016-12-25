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

import com.dave_cs.BroncoShuttlePlusMobile.Application.Data.DetailsViewStopData;
import com.dave_cs.BroncoShuttlePlusMobile.Application.DataUpdateApplication;
import com.dave_cs.BroncoShuttlePlusMobile.Details.Advanced.DetailsAdvActivity;
import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfoFastScrollAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by David on 1/20/2016.
 */
public class DetailsStopTabFragment extends android.support.v4.app.Fragment implements Filterable, Observer {

    private static final String TAG = "DetailsStopTabFragment";

    public boolean ready = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<StopInfo> stopInfoList = new ArrayList<>();
    private List<StopInfo> searchList = new ArrayList<>();

    private ListView stopListView;
    private StopInfoFastScrollAdapter listAdapter;

    private boolean error = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((DataUpdateApplication) getActivity().getApplication()).detailsViewData.detailsViewStopData.addObserver(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.details_stop_fragment_layout, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.stopList_refresh_widget);
        swipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.gold);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                ((ViewPagerDetailsViewActivity) getActivity()).requestUpdate(0);
            }
        });

        if (!((DataUpdateApplication) getActivity().getApplication()).detailsViewData.detailsViewStopData.stopInfoList.isEmpty()) {
            stopInfoList = ((DataUpdateApplication) getActivity().getApplication()).detailsViewData.detailsViewStopData.stopInfoList;
        }

        listAdapter = new StopInfoFastScrollAdapter(getContext(), stopInfoList);

        stopListView = (ListView) v.findViewById(R.id.stopList);
        stopListView.setFastScrollEnabled(true);
        stopListView.setFastScrollAlwaysVisible(true);
        stopListView.setItemsCanFocus(true);
        stopListView.setAdapter(listAdapter);

        stopListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DetailsAdvActivity.class);
                intent.putExtra("stopName", ((StopInfo) stopListView.getItemAtPosition(position)).getName());
                intent.putExtra("stopNumber", ((StopInfo) stopListView.getItemAtPosition(position)).getStopNumber());
                startActivity(intent);
            }
        });

        if (error) {
            ((ViewPagerDetailsViewActivity) getActivity()).errorBoxLinearLayout.setVisibility(View.VISIBLE);
        }

        return v;
    }

    @Override
    public void filter(String query) {
        searchList.clear();
        for (StopInfo s : stopInfoList) {
            if (s.getName().toLowerCase().contains(query.toLowerCase()))
                searchList.add(s);
        }

        StopInfoFastScrollAdapter searchAdapter = new StopInfoFastScrollAdapter(getContext(), searchList);
        stopListView.setAdapter(searchAdapter);
    }

    @Override
    public void clear() {
        stopListView.setAdapter(listAdapter);
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.i(TAG, "updated!");
        if (observable instanceof DetailsViewStopData) {
            if (!((DetailsViewStopData) observable).stopInfoList.isEmpty()) {
                this.stopInfoList = ((DetailsViewStopData) observable).stopInfoList;
                if (getContext() != null) {
                    listAdapter = new StopInfoFastScrollAdapter(getContext(), stopInfoList);
                    listAdapter.initialize();
                    clear();
                    ready = true;
                    if (swipeRefreshLayout != null) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                    error = false;
                    if (getActivity() != null) {
                        ((ViewPagerDetailsViewActivity) getActivity()).errorBoxLinearLayout.setVisibility(View.GONE);
                    }
                    Log.i(TAG, "update complete with list");
                }

            }
        }
    }
}
