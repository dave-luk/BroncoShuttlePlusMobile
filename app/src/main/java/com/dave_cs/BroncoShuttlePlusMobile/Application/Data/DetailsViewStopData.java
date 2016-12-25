package com.dave_cs.BroncoShuttlePlusMobile.Application.Data;

import android.util.Log;

import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopListService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by David on 12/22/2016.
 */

public class DetailsViewStopData extends Observable {

    private static final String TAG = "DetailsViewStopData";

    public List<StopInfo> stopInfoList = new ArrayList<>();

    protected DetailsViewStopData() {
        getStopInfo();
    }

    private void getStopInfo() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        StopListService stopListService = retrofit.create(StopListService.class);

        Call<List<StopInfo>> call = stopListService.getInfo();
        call.enqueue(new Callback<List<StopInfo>>() {

            @Override
            public void onResponse(Call<List<StopInfo>> call, Response<List<StopInfo>> response) {
                if (response.isSuccess()) {
                    stopInfoList.addAll(response.body());
                    Collections.sort(stopInfoList);
                    setChanged();
                    Log.i(TAG, "got StopList");
                } else {
                    Log.e(TAG, response.code() + ":-" + response.message());
                    stopInfoList.clear();
                    setChanged();
                }
                notifyObservers(stopInfoList);
            }

            @Override
            public void onFailure(Call<List<StopInfo>> call, Throwable t) {
                Log.e(TAG, "Failed: " + t.getLocalizedMessage());
                stopInfoList.clear();
                setChanged();
                notifyObservers();
            }
        });
    }

    public void requestStopUpdate() {
        getStopInfo();
    }
}