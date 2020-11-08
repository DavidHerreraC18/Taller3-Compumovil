package com.taller.myapplication.model;

import android.os.Parcel;
import android.os.Parcelable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain =  true)
public class Ubicacion implements Parcelable {
    private Double latitude;
    private Double longitude;
    private String title;

    public Ubicacion(Double latitude, Double longitude, String title) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(title);
    }

    public Ubicacion(Parcel in) {
        this.latitude = in.readDouble();
        this.longitude = in.readDouble();
        this.title = in.readString();
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public static final Parcelable.Creator<Ubicacion> CREATOR = new Parcelable.Creator<Ubicacion>() {
        public Ubicacion createFromParcel(Parcel source) {
            return new Ubicacion(source);
        }

        public Ubicacion[] newArray(int size) {
            return new Ubicacion[size];
        }
    };
}
