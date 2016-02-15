package com.dave_cs.BroncoShuttlePlusServerUtil.Stops;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface StopListService {
    @GET("/BroncoShuttlePlus/details/stopList")
    Call<List<StopInfo>> getInfo();
}
