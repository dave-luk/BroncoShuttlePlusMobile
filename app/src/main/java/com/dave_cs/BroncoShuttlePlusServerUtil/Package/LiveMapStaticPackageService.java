package com.dave_cs.BroncoShuttlePlusServerUtil.Package;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Created by David on 12/17/2016.
 */

public interface LiveMapStaticPackageService {
    @GET("/BroncoShuttlePlus/LiveMapStaticInfo")
    Call<List<StaticRoutePackage>> getStaticInfo();
}
