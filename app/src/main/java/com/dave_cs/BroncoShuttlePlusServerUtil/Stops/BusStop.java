package com.dave_cs.BroncoShuttlePlusServerUtil.Stops;

import android.os.Parcel;
import android.os.Parcelable;

import com.dave_cs.BroncoShuttlePlusServerUtil.Location;

/**
 * Created by David on 12/18/2016.
 */

public class BusStop implements Parcelable {
    public static final Creator<BusStop> CREATOR = new Creator<BusStop>() {
        @Override
        public BusStop createFromParcel(Parcel in) {
            return new BusStop(in);
        }

        @Override
        public BusStop[] newArray(int size) {
            return new BusStop[size];
        }
    };
    public Location loc;
    public int stopID;
    public String name;

    public BusStop() {
    }

    protected BusStop(Parcel in) {
        loc = in.readParcelable(Location.class.getClassLoader());
        stopID = in.readInt();
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(loc, flags);
        dest.writeInt(stopID);
        dest.writeString(name);
    }
}
