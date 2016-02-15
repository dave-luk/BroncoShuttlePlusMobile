package com.dave_cs.BroncoShuttlePlusServerUtil.Bus;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by David on 1/27/2016.
 */
public interface BusListService {
    @GET("/BroncoShuttlePlus/details/busList")
    Call<List<BusInfo>> getInfo(@Query("routeName") String name);
}
