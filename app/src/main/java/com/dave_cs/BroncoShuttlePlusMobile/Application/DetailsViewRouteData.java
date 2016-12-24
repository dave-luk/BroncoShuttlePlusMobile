package com.dave_cs.BroncoShuttlePlusMobile.Application;

import android.util.Log;

import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.SimpleRouteInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.SimpleRouteInfoService;

import java.util.ArrayList;
import java.util.Collections;
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

public class DetailsViewRouteData extends Observable {

    private static final String TAG = "DetailsViewRouteData";

    public ArrayList<SimpleRouteInfo> simpleRouteInfoList = new ArrayList<>();

    protected DetailsViewRouteData(List<String> routes) {
        requestRouteUpdate(routes);
    }

    private void getSimpleRouteInfos(String route) {
        // reach to server and pull route info
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        final SimpleRouteInfoService simpleRouteInfoService = retrofit.create(SimpleRouteInfoService.class);
        Call<SimpleRouteInfo> call = simpleRouteInfoService.getInfo(route.replace("ROUTE ", ""));
        call.enqueue(new Callback<SimpleRouteInfo>() {

            @Override
            public void onResponse(Call<SimpleRouteInfo> call, Response<SimpleRouteInfo> response) {
                if (response.isSuccess()) {
                    boolean added = false;
                    for (SimpleRouteInfo s : simpleRouteInfoList) {
                        if (s.compareTo(response.body()) == 0) {
                            added = true;
                            s.setInService(response.body().isInService());
                            s.setBusCount(response.body().getBusCount());
                        }

                    }
                    if (!added) {
                        simpleRouteInfoList.add(response.body());
                    }
                    Collections.sort(simpleRouteInfoList);
                    setChanged();
                    notifyObservers();

                } else {
                    Log.e(TAG, "error: " + response.code() + ":" + response.message());
                    simpleRouteInfoList.clear();
                    setChanged();
                    notifyObservers();
                }
            }

            @Override
            public void onFailure(Call<SimpleRouteInfo> call, Throwable t) {
                Log.e(TAG, "route-fail: " + t.getLocalizedMessage());
                simpleRouteInfoList.clear();
                setChanged();
                notifyObservers();
            }
        });
    }

    public void requestRouteUpdate(List<String> routes) {
        for (String r : routes) {
            getSimpleRouteInfos(r);
        }
    }
}
