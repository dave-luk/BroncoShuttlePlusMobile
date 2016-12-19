package com.dave_cs.BroncoShuttlePlusServerUtil.Package;

import android.os.Parcel;
import android.os.Parcelable;

import com.dave_cs.BroncoShuttlePlusServerUtil.Location;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.BusStop;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David on 12/17/2016.
 */

public class StaticRoutePackage implements Parcelable {
    public static final Creator<StaticRoutePackage> CREATOR = new Creator<StaticRoutePackage>() {
        @Override
        public StaticRoutePackage createFromParcel(Parcel in) {
            return new StaticRoutePackage(in);
        }

        @Override
        public StaticRoutePackage[] newArray(int size) {
            return new StaticRoutePackage[size];
        }
    };
    public String routeName;
    public int routeNumber;
    public List<BusStop> stops;
    public List<Location> polyLine;

    public StaticRoutePackage() {
    }

    protected StaticRoutePackage(Parcel in) {
        routeName = in.readString();
        routeNumber = in.readInt();
        stops = new ArrayList<>();
        polyLine = new ArrayList<>();
        in.readList(stops, null);
        in.readList(polyLine, null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(routeName);
        dest.writeInt(routeNumber);
        dest.writeList(stops);
        dest.writeList(polyLine);
    }
}
