package com.dave_cs.BroncoShuttlePlusServerUtil.Stops;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by David on 2/14/2016.
 */
public interface StopInfoService {
    @GET("/BroncoShuttlePlus/details/stop")
    Call<StopInfo> getInfo(@Query("stopNumber") String s);
}
