package com.dave_cs.BroncoShuttlePlusServerUtil;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

//@JsonIgnoreProperties(ignoreUnknown = true)
public class Location implements Parcelable {
    public static final Creator<Location> CREATOR = new Creator<Location>() {
        @Override
        public Location createFromParcel(Parcel in) {
            return new Location(in);
        }

        @Override
        public Location[] newArray(int size) {
            return new Location[size];
        }
    };
    private double lat;
    private double lng;
    private int placeID;

    public Location() {
    }

    protected Location(Parcel in) {
        lat = in.readDouble();
        lng = in.readDouble();
        placeID = in.readInt();
    }

    public int getPlaceID() {
        return placeID;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
    public LatLng parseLatLng() {
        return new LatLng(getLat(), getLng());
    }

    @Override
    public String toString() {
        return "Lat: " + lat + "\nlng: " + lng + "\n";
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(lat);
        dest.writeDouble(lng);
        dest.writeInt(placeID);
    }
}
