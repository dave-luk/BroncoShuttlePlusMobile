package com.dave_cs.BroncoShuttlePlusMobile.Application;

import android.app.Application;
import android.util.Log;

import com.dave_cs.BroncoShuttlePlusMobile.Application.Data.DetailsViewData;
import com.dave_cs.BroncoShuttlePlusMobile.Application.Data.LiveMapData;
import com.dave_cs.BroncoShuttlePlusMobile.Application.NetworkState.NetworkStateChangedEvent;
import com.dave_cs.BroncoShuttlePlusMobile.Application.NetworkState.NetworkStateReceiver;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created by David on 12/21/2016.
 */

public class DataUpdateApplication extends Application {

    private static final String TAG = "DataUpdateApplication";

    private static DataUpdateApplication INSTANCE;
    public LiveMapData liveMapData;
    public DetailsViewData detailsViewData;

    public ApplicationReadyRelay applicationReadyRelay;

    private NetworkStateReceiver networkStateReceiver = new NetworkStateReceiver();

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
        //register activity on EventBus
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onEvent(NetworkStateChangedEvent event) {
        Log.i(TAG, "event triggered: " + event.getCurrentState());
        switch (event.getCurrentState()) {
            case NetworkStateChangedEvent.CONNECTED:
                if (!detailsViewData.isComplete()) {
                    detailsViewData.reset();
                }
                if (!liveMapData.isComplete()) {
                    liveMapData.reset();
                }
                break;
        }
    }

}
