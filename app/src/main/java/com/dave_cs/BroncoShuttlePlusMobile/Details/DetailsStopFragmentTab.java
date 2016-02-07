package com.dave_cs.BroncoShuttlePlusMobile.Details;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.dave_cs.BroncoShuttlePlusMobile.R;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfoAdapter;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfoFastScrollAdapter;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfoService;

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

        return v;
    }

    private void propagate()
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        StopInfoService stopInfoService = retrofit.create(StopInfoService.class);

        Call<List<StopInfo>> call = stopInfoService.getInfo();
        call.enqueue(new Callback<List<StopInfo>>() {

            @Override
            public void onResponse(Response<List<StopInfo>> response) {
                if (response.isSuccess()) {
                    stopInfoList.addAll(response.body());
                    Collections.sort(stopInfoList);
                    listAdapter.notifyDataSetChanged();
                    listAdapter.initialize();
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
