package com.dave_cs.BroncoShuttlePlusMobile.Application.NetworkState;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by David on 12/24/2016.
 */

public class NetworkStateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null) {
            if (networkInfo.isConnected()) {
                EventBus.getDefault().post(new NetworkStateChangedEvent(NetworkStateChangedEvent.CONNECTED));
            } else if (networkInfo.isConnectedOrConnecting()) {
                EventBus.getDefault().post(new NetworkStateChangedEvent(NetworkStateChangedEvent.CONNECTING));
            } else {
                EventBus.getDefault().post(new NetworkStateChangedEvent(NetworkStateChangedEvent.DISCONNECTED));
            }
        } else {
            EventBus.getDefault().post(new NetworkStateChangedEvent(NetworkStateChangedEvent.DISCONNECTED));
        }
    }
}