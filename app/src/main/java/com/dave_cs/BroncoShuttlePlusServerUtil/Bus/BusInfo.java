package com.dave_cs.BroncoShuttlePlusServerUtil.Bus;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;


public class BusInfo implements Comparable, Parcelable
{
    public static final Creator<BusInfo> CREATOR = new Creator<BusInfo>() {
        @Override
        public BusInfo createFromParcel(Parcel in) {
            return new BusInfo(in);
        }

        @Override
        public BusInfo[] newArray(int size) {
            return new BusInfo[size];
        }
    };
    private String busName;
    private String route;
    private int fullness;
    private int lastUpdate;
    private String nextStop;
    private int busNumber;

    public BusInfo(JSONObject obj)
    {
        try
        {
            this.busName = obj.getString("busName");
            this.fullness = obj.getInt("fullness");
            this.route = obj.getString("route");
            this.lastUpdate = obj.getInt("lastUpdate");
            this.nextStop = obj.getString("nextStop");
            this.busNumber = obj.getInt("busNumber");
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
    }


    public BusInfo()
    {
        this.busName = "Unavailable!";
        this.route = "";
        this.fullness = 0;
        this.lastUpdate = 0;
        this.nextStop = "";
        this.busNumber = 0;
    }

    protected BusInfo(Parcel in) {
        busName = in.readString();
        route = in.readString();
        fullness = in.readInt();
        lastUpdate = in.readInt();
        nextStop = in.readString();
        busNumber = in.readInt();
    }

    public String getBusName() {
        return busName;
    }

    public int getFullness() {
        return fullness;
    }

    public String getNextStop() {
        return nextStop;
    }

    public int getLastUpdate() {
        return lastUpdate;
    }

    public int getBusNumber() {
        return busNumber;
    }

    public String getRoute() {
        return route;
    }

    @Override
    public int compareTo(Object another) {
        if(another instanceof BusInfo)
            return this.busName.compareTo(((BusInfo) another).getBusName());
        else
            return this.hashCode() - another.hashCode();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(busName);
        dest.writeString(route);
        dest.writeInt(fullness);
        dest.writeInt(lastUpdate);
        dest.writeString(nextStop);
        dest.writeInt(busNumber);
    }
}

