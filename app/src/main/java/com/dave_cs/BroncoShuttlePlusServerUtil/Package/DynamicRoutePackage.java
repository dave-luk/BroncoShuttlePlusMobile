package com.dave_cs.BroncoShuttlePlusServerUtil.Package;

import android.os.Parcel;
import android.os.Parcelable;

import com.dave_cs.BroncoShuttlePlusServerUtil.Bus.BusInfo;
import com.dave_cs.BroncoShuttlePlusServerUtil.Stops.StopInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by David on 12/17/2016.
 */
public class DynamicRoutePackage implements Parcelable {
    public static final Creator<DynamicRoutePackage> CREATOR = new Creator<DynamicRoutePackage>() {
        @Override
        public DynamicRoutePackage createFromParcel(Parcel in) {
            return new DynamicRoutePackage(in);
        }

        @Override
        public DynamicRoutePackage[] newArray(int size) {
            return new DynamicRoutePackage[size];
        }
    };
    public String routeName;
    public int routeNumber;
    public List<StopInfo> stops;
    public List<BusInfo> buses;

    public DynamicRoutePackage() {
    }

    protected DynamicRoutePackage(Parcel in) {
        routeName = in.readString();
        routeNumber = in.readInt();
        stops = new ArrayList<>();
        buses = new ArrayList<>();
        in.readList(buses, getClass().getClassLoader());
        in.readList(stops, getClass().getClassLoader());
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
        dest.writeList(buses);
    }
}
