package eu.snigle.corsaire.itinerary.favorite;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

/**
 * Created by lamarchelu on 25/09/16.
 */
public class Favorite implements Parcelable{
    public final String name;
    public final Double lat;
    public final Double lng;

    public Favorite(String name, Double lat, Double lng) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
    }

    protected Favorite(Parcel in) {
        name = in.readString();
        lat = in.readDouble();
        lng = in.readDouble();
    }

    public static final Creator<Favorite> CREATOR = new Creator<Favorite>() {
        @Override
        public Favorite createFromParcel(Parcel in) {
            return new Favorite(in);
        }

        @Override
        public Favorite[] newArray(int size) {
            return new Favorite[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(lat);
        dest.writeDouble(lng);
    }
}
