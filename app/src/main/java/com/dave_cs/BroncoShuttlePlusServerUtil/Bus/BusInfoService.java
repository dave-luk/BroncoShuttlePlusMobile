package com.dave_cs.BroncoShuttlePlusServerUtil.Bus;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BusInfoService {
    @GET("/BroncoShuttlePlus/details/bus")
    Call<BusInfo> getInfo(@Query("busNumber") String b);
}
