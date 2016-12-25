package com.dave_cs.BroncoShuttlePlusMobile.Application;

import com.dave_cs.BroncoShuttlePlusMobile.Application.Data.DetailsViewHeaderData;
import com.dave_cs.BroncoShuttlePlusMobile.Application.Data.LiveMapData;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by David on 12/23/2016.
 */

public class ApplicationReadyRelay extends Observable implements Observer {
    private static final String TAG = "ApplicationReadyRelay";

    public boolean liveMapReady = false;
    public boolean detailsReady = false;

    public ApplicationReadyRelay() {
        DataUpdateApplication.getInstance().liveMapData.addObserver(this);
        DataUpdateApplication.getInstance().detailsViewData.detailsViewHeaderData.addObserver(this);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof DetailsViewHeaderData) {
            detailsReady = !((DetailsViewHeaderData) observable).routes.isEmpty();
        } else if (observable instanceof LiveMapData) {
            liveMapReady = !((LiveMapData) observable).liveMapStaticRoutePackages.isEmpty();
        }

        if (liveMapReady && detailsReady) {
            setChanged();
            notifyObservers();
        }
    }
}
