package com.dave_cs.BroncoShuttlePlusMobile.Application.Data;

import android.util.Log;

import com.dave_cs.BroncoShuttlePlusServerUtil.Package.DynamicRoutePackage;
import com.dave_cs.BroncoShuttlePlusServerUtil.Package.LiveMapDynamicPackageService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Package.LiveMapStaticPackageService;
import com.dave_cs.BroncoShuttlePlusServerUtil.Package.StaticRoutePackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * Created by David on 12/21/2016.
 */

public class LiveMapData extends Observable {

    public static final String TAG = "LiveMapData";
    //liveMap data
    public List<DynamicRoutePackage> liveMapDynamicRoutePackages = new ArrayList<>();
    public List<StaticRoutePackage> liveMapStaticRoutePackages = new ArrayList<>();

    public LiveMapData() {
        getStaticPackages();
        getDynamicPackages(-1, 0);
    }

    private void getStaticPackages() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        LiveMapStaticPackageService liveMapStaticPackageService = retrofit.create(LiveMapStaticPackageService.class);
        Call<List<StaticRoutePackage>> data = liveMapStaticPackageService.getStaticInfo();
        data.enqueue(new Callback<List<StaticRoutePackage>>() {

            @Override
            public void onResponse(Call<List<StaticRoutePackage>> call, Response<List<StaticRoutePackage>> response) {
                if (response.isSuccess()) {
                    Log.i(TAG, "got Static packages");
                    liveMapStaticRoutePackages.clear();
                    liveMapStaticRoutePackages.addAll(response.body());
                    setChanged();
                    notifyObservers();

                }
            }

            @Override
            public void onFailure(Call<List<StaticRoutePackage>> call, Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
                liveMapStaticRoutePackages.clear();
                setChanged();
                notifyObservers();
            }
        });
    }

    private void getDynamicPackages(final int index, final int routeID) {
        //configure longer Timeout...
        final OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://dave-cs.com")
                .client(okHttpClient)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        final LiveMapDynamicPackageService liveMapDynamicPackageService = retrofit.create(LiveMapDynamicPackageService.class);
        Call<List<DynamicRoutePackage>> data = liveMapDynamicPackageService.getDynamicInfo((index != -1) ? routeID : -1);
        data.enqueue(new Callback<List<DynamicRoutePackage>>() {

            @Override
            public void onResponse(Call<List<DynamicRoutePackage>> call, Response<List<DynamicRoutePackage>> response) {
                Log.i(TAG, "Successfully get dynamic data");
                if (response.isSuccess()) {
                    if (index == -1) {
                        liveMapDynamicRoutePackages = response.body();
                    } else {
                        liveMapDynamicRoutePackages.set(index, response.body().get(0));
                    }
                    setChanged();
                    notifyObservers();
                }
            }

            @Override
            public void onFailure(Call<List<DynamicRoutePackage>> call, Throwable t) {
                Log.e(TAG + ".DP", t.getLocalizedMessage());
                liveMapDynamicRoutePackages.clear();
                setChanged();
                notifyObservers();
            }
        });
    }

    public void requestUpdate(int index, int routeId) {
        getDynamicPackages(index, routeId);
    }

    public boolean isComplete() {
        return !(liveMapStaticRoutePackages.isEmpty() || liveMapDynamicRoutePackages.isEmpty());
    }

    public void reset() {
        getStaticPackages();
        getDynamicPackages(-1, 0);
    }
}

