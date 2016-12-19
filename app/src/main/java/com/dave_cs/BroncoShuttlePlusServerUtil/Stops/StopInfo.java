package com.dave_cs.BroncoShuttlePlusServerUtil.Stops;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by David on 1/27/2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StopInfo implements Comparable<StopInfo>, Parcelable {

    public static final Creator<StopInfo> CREATOR = new Creator<StopInfo>() {
        @Override
        public StopInfo createFromParcel(Parcel in) {
            return new StopInfo(in);
        }

        @Override
        public StopInfo[] newArray(int size) {
            return new StopInfo[size];
        }
    };
    private String name;
    private String nextBusOfRoute;
    private String onRoute;
    private int timeToNext;
    private int stopNumber;
    private LatLng location;

    public StopInfo() {
        name = "Error!";
        nextBusOfRoute = "";
        onRoute = "";
        timeToNext = 0;
        stopNumber = 0;
    }

    public StopInfo(JSONObject object) {
        try {
            name = object.getString("name");
            nextBusOfRoute = object.getString("nextBusOfRoute");
            onRoute = object.getString("onRoute");
            timeToNext = object.getInt("timeToNext");
            stopNumber = object.getInt("stopNumber");
        } catch (JSONException ignored) {

        }
    }

    protected StopInfo(Parcel in) {
        name = in.readString();
        nextBusOfRoute = in.readString();
        onRoute = in.readString();
        timeToNext = in.readInt();
        stopNumber = in.readInt();
        location = in.readParcelable(LatLng.class.getClassLoader());
    }

    public String getName() {
        return name;
    }

    public String getNextBusOfRoute() {
        return nextBusOfRoute;
    }

    public int getTimeToNext() {
        return timeToNext;
    }

    public int getStopNumber() {
        return stopNumber;
    }

    public String getOnRoute() {
        return onRoute;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(nextBusOfRoute);
        dest.writeString(onRoute);
        dest.writeInt(timeToNext);
        dest.writeInt(stopNumber);
        dest.writeParcelable(location, 0);
    }

    @Override
    public int compareTo(@NonNull StopInfo another) {
        return this.getName().compareTo(another.getName());
    }
}
