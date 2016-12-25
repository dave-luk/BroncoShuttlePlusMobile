package com.dave_cs.BroncoShuttlePlusMobile.Application.NetworkState;

/**
 * Created by David on 12/24/2016.
 */

public class NetworkStateChangedEvent {
    public static final int DISCONNECTED = -1;
    public static final int CONNECTING = 0;
    public static final int CONNECTED = 1;

    private int currentState;

    public NetworkStateChangedEvent(int currentState) {
        this.currentState = currentState;
    }

    public int getCurrentState() {
        return currentState;
    }
}

