package com.dave_cs.BroncoShuttlePlusMobile.Application;

import android.app.Application;

/**
 * Created by David on 12/21/2016.
 */

public class DataUpdateApplication extends Application {

    private static DataUpdateApplication INSTANCE;
    public LiveMapData liveMapData;
    public DetailsViewData detailsViewData;

    public ApplicationReadyRelay applicationReadyRelay;

    public static DataUpdateApplication getInstance() {
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        liveMapData = new LiveMapData();
        detailsViewData = new DetailsViewData();
        INSTANCE = this;
        applicationReadyRelay = new ApplicationReadyRelay();
    }
}
