package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dave_cs.BroncoShuttlePlusMobile.Details.Advanced.DetailsAdvFragmentTab;
import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfoFastScrollAdapter;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopListService;

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
public class DetailsStopFragmentTab extends android.support.v4.app.Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;

    private List<StopInfo> stopInfoList = new ArrayList<>();

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
                android.support.v4.app.Fragment newFrag = new DetailsAdvFragmentTab();
                Bundle bundle = new Bundle();
                bundle.putString("stopName", stopInfoList.get(position).getName());
                bundle.putInt("stopNumber", stopInfoList.get(position).getStopNumber());
                newFrag.setArguments(bundle);
                getFragmentManager()
                        .beginTransaction()
                        .hide(DetailsStopFragmentTab.this)
                        .add(android.R.id.tabcontent, newFrag, "stop frag")
                        .addToBackStack("simpleStop")
                        .commit();
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
            public void onResponse(Response<List<StopInfo>> response) {
                Log.d("<data>", "size:" + stopInfoList.size());
                if (response.isSuccess()) {
                    listAdapter.addAll(response.body());
                    Collections.sort(listAdapter.getList());
                    listAdapter.notifyDataSetChanged();
                    listAdapter.initialize();
                    Log.d("<data>", "size:" + stopInfoList.size());
                } else {
                    Log.e("<Error>", response.code() + ":" + response.message());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e("<FAIL>", t.getLocalizedMessage() + " ");
            }
        });

        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }


}
