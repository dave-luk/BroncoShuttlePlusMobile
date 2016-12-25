package com.dave_cs.BroncoShuttlePlusMobile.Application.Data;

import android.util.Log;

import com.dave_cs.BroncoShuttlePlusServerUtil.Routes.RouteOnlineService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by David on 12/22/2016.
 */

public class DetailsViewHeaderData extends Observable {

    private static final String TAG = "DetailsViewHeaderData";

    public ArrayList<String> routes = new ArrayList<>();

    protected DetailsViewHeaderData() {
        getHeaders();
    }

    private void getHeaders() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        RouteOnlineService routeOnlineService = retrofit.create(RouteOnlineService.class);
        Call<String[]> data = routeOnlineService.getInfo("name");
        data.enqueue(new Callback<String[]>() {
            @Override
            public void onResponse(Call<String[]> call, Response<String[]> response) {
                if (response.isSuccess()) {
                    routes.addAll(Arrays.asList(response.body()));
                    Log.i(TAG, "got headers " + routes.size());

                    setChanged();
                    notifyObservers();
                }
            }

            @Override
            public void onFailure(Call<String[]> call, Throwable t) {
                Log.e(TAG, "route-header-fail" + t.getLocalizedMessage());
                routes.clear();
                notifyObservers();
            }
        });
    }

    public void requestHeaderUpdate() {
        getHeaders();
    }
}
