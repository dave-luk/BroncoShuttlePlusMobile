package com.dave_cs.BroncoShuttlePlusServerUtil.Routes;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by David on 8/31/2016.
 */
public interface RoutesToNameService {
    @GET("/BroncoShuttlePlus/details/routeNumberToName")
    Call<String> getInfo(@Query("routeNumber") int r);
}
