package com.dave_cs.BroncoShuttlePlusMobile.Application.Data;

import android.util.Log;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by David on 12/21/2016.
 */
public class DetailsViewData implements Observer {
    private static final String TAG = "DetailsViewData";

    //this is used by bus and route
    public DetailsViewHeaderData detailsViewHeaderData;

    public DetailsViewRouteData detailsViewRouteData;
    public DetailsViewBusData detailsViewBusData;

    public DetailsViewStopData detailsViewStopData;


    public DetailsViewData() {
        detailsViewStopData = new DetailsViewStopData();
        detailsViewHeaderData = new DetailsViewHeaderData();
        detailsViewRouteData = new DetailsViewRouteData();
        detailsViewBusData = new DetailsViewBusData();
        detailsViewHeaderData.addObserver(this);
    }

    public boolean isComplete() {
        return !(detailsViewBusData.busInfoList.isEmpty() || detailsViewRouteData.simpleRouteInfoList.isEmpty() || detailsViewHeaderData.routes.isEmpty() || detailsViewStopData.stopInfoList.isEmpty());
    }

    public void reset() {
        Log.i(TAG, "reset called!");
        detailsViewStopData.requestStopUpdate();
        detailsViewHeaderData.requestHeaderUpdate();
    }

    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof DetailsViewHeaderData) {
            if (!((DetailsViewHeaderData) observable).routes.isEmpty()) {
                Log.i(TAG, "populating bus/route");
                detailsViewBusData.init(((DetailsViewHeaderData) observable).routes);
                detailsViewRouteData.requestRouteUpdate(((DetailsViewHeaderData) observable).routes);
            }
        }
    }
}
