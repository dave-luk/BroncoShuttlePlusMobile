package com.dave_cs.BroncoShuttlePlusServerUtil;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface LocationService {
    @GET("/BroncoShuttlePlus/latLng")
    Call<Location> getLocation(@Query("stopNumber") Integer stopNumber,
                               @Query("busNumber") Integer busNumber);
}
