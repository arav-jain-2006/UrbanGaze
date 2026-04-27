package com.urbangaze.app.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.ola.mapsdk.model.OlaLatLng;

public class Place implements Parcelable {
    public final String title;
    public final String address;
    public final String placeId;
    public final OlaLatLng loc;
    public int distance; // in meters

    public Place(String placeId, String address, String title, double lat, double lng, int distance) {
        this.title = title != null ? title : "";
        this.address = address != null ? address : "";
        this.placeId = placeId != null ? placeId : "";
        this.loc = new OlaLatLng(lat, lng, 0);
        this.distance = distance;
    }

    protected Place(Parcel in) {
        title = in.readString();
        address = in.readString();
        placeId = in.readString();
        distance = in.readInt();

        double lat = in.readDouble();
        double lng = in.readDouble();
        loc = new OlaLatLng(lat, lng, 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(address);
        dest.writeString(placeId);
        dest.writeInt(distance);

        dest.writeDouble(loc.getLatitude());
        dest.writeDouble(loc.getLongitude());
    }

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
}
