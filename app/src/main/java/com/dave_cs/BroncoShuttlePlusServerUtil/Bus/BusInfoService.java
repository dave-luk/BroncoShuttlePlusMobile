package com.dave_cs.BroncoShuttlePlusServerUtil.Bus;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by David on 1/27/2016.
 */
public interface BusInfoService {
    @GET("/BroncoShuttle/details/Bus")
    Call<BusInfo> getInfo(@Query("routeName") String name);
}
