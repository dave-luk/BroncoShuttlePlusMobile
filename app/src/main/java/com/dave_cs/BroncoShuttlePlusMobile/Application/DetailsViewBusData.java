package com.dave_cs.BroncoShuttlePlusMobile.Application;

import android.util.Log;

import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusListService;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by David on 12/22/2016.
 */

public class DetailsViewBusData extends Observable {

    private static final String TAG = "DetailsViewBusData";

    public ArrayList<ArrayList<BusInfo>> busInfoList = new ArrayList<>();

    protected DetailsViewBusData(List<String> routes) {
        for (String r : routes) {
            busInfoList.add(new ArrayList<BusInfo>());
        }
        requestBusUpdate(routes);
    }

    private void getBusInfo(final int index, final String route) {
        // reach to server and pull route info
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();


        BusListService busListService = retrofit.create(BusListService.class);

        Call<List<BusInfo>> call = busListService.getInfo(route.replace("ROUTE ", ""));
        call.enqueue(new Callback<List<BusInfo>>() {
            @Override
            public void onResponse(Call<List<BusInfo>> call, Response<List<BusInfo>> response) {
                if (response.isSuccess()) {
                    busInfoList.get(index).addAll(response.body());
                    Log.i(TAG, "get bus on " + route);
                    setChanged();
                    Log.d(TAG, "observers# : " + countObservers());
                    notifyObservers();
                } else {
                    Log.e(TAG, response.code() + ":" + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<BusInfo>> call, Throwable t) {
                Log.e(TAG, "bus-fail" + t.getLocalizedMessage());
            }
        });

    }

    public void requestBusUpdate(List<String> routes) {
        for (int i = 0; i < routes.size(); i++) {
            busInfoList.get(i).clear();
            getBusInfo(i, routes.get(i));
        }
    }
}
