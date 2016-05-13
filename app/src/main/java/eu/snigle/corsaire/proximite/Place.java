package eu.snigle.corsaire.proximite;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by lamarchelu on 04/05/16.
 */
public class Place implements Parcelable{
    public final String name;
    public final Location location ;
    public Place(String name, double lat, double lng) {
        this.name = name;
        location = new Location("");
        this.location.setLatitude(lat);
        this.location.setLongitude(lng);
    }

    protected Place(Parcel in) {
        name = in.readString();
        location = in.readParcelable(Location.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeParcelable(location, flags);
    }

    @Override
    public int describeContents() {
        return 0;
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
