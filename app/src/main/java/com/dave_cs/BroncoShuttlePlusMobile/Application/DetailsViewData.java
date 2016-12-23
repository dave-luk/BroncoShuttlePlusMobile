package com.dave_cs.BroncoShuttlePlusMobile.Application;

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


    protected DetailsViewData() {
        detailsViewStopData = new DetailsViewStopData();
        detailsViewHeaderData = new DetailsViewHeaderData();
        detailsViewHeaderData.addObserver(this);
    }

    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof DetailsViewHeaderData) {
            if (!((DetailsViewHeaderData) observable).routes.isEmpty()) {
                detailsViewBusData = new DetailsViewBusData(((DetailsViewHeaderData) observable).routes);
                detailsViewRouteData = new DetailsViewRouteData(((DetailsViewHeaderData) observable).routes);
            }
        }
    }
}
