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

import com.dave_cs.BroncoShuttlePlusMobile.Details.Advanced.DetailsAdvActivity;
import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Location;
import com.dave_cs.BroncoShuttlePlusServerUtil.LocationService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfoFastScrollAdapter;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopListService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by David on 1/20/2016.
 */
public class DetailsStopFragmentTab extends android.support.v4.app.Fragment implements Filterable {

    public boolean ready = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<StopInfo> stopInfoList = new ArrayList<>();
    private List<StopInfo> searchList = new ArrayList<>();

    private ListView stopListView;
    private StopInfoFastScrollAdapter listAdapter;

    @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            propagate();
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
                listAdapter.clear();
                propagate();
            }
        });

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

        return v;
    }

    private void propagate()
    {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        StopListService stopListService = retrofit.create(StopListService.class);

        Call<List<StopInfo>> call = stopListService.getInfo();
        call.enqueue(new Callback<List<StopInfo>>() {

            @Override
            public void onResponse(Call<List<StopInfo>> call, Response<List<StopInfo>> response) {
                Log.d("<data>", "size:" + stopInfoList.size());
                if (response.isSuccess()) {
                    stopInfoList.addAll(response.body());
                    Log.d("<DATA", "added stop to List: " + stopInfoList.size());
                    Collections.sort(listAdapter.getList());
                    listAdapter.notifyDataSetChanged();
                    listAdapter.initialize();
                    ready = true;
                    Log.d("<data>", "size:" + stopInfoList.size());
                } else {
                    Log.e("<Error>", response.code() + ":" + response.message());
                }
                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<StopInfo>> call, Throwable t) {
                Log.e("<FAIL>", t.getLocalizedMessage() + " ");
            }
        });
    }

    @Override
    public void filter(String query) {
        searchList.clear();
        for (StopInfo s : stopInfoList) {
            Log.d("<Iteration>: ", s.getName());
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

    public int getStopCount() {
        return stopInfoList.size();
    }

    public ArrayList<StopLocation> getStopLocations() {
        final ArrayList<StopLocation> stopLocations = new ArrayList<>();

        for (final StopInfo s : stopInfoList) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://dave-cs.com")
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();

            LocationService locationService = retrofit.create(LocationService.class);

            Call<Location> stopCall = locationService.getLocation(s.getStopNumber(), null);
            stopCall.enqueue(new Callback<Location>() {

                @Override
                public void onResponse(Call<Location> call, Response<Location> response) {
                    if (response.isSuccess()) {
                        Location l = response.body();
                        stopLocations.add(new StopLocation(s.getName(), s.getStopNumber(), l));
                    } else {
                        Log.e("<Error>", response.code() + ":" + response.message());
                    }
                }

                @Override
                public void onFailure(Call<Location> call, Throwable t) {
                    Log.e("<Error>", t.getLocalizedMessage() + "" + call.toString());
                }
            });
        }

        return stopLocations;
    }
}
