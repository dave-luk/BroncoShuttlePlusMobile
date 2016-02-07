package com.dave_cs.BroncoShuttlePlusServerUtil.Stops;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by David on 2/6/2016.
 */
public interface StopInfoService {
    @GET("/BroncoShuttle/details/stopList")
    Call<List<StopInfo>> getInfo();
}
