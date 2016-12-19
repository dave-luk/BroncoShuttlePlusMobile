package com.dave_cs.BroncoShuttlePlusServerUtil.Package;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by David on 12/17/2016.
 */

public interface LiveMapDynamicPackageService {
    @GET("/BroncoShuttlePlus/LiveMapDynamicInfo")
    Call<List<DynamicRoutePackage>> getDynamicInfo(@Query(value = "ID") int ID);
}

