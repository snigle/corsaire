package eu.snigle.corsaire.itinerary.details;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by lamarchelu on 07/04/16.
 */
public abstract class Step{

//    private final String street;
    public final String maneuver;
    public Location start;
    public Location end;
    public Long distance;
    public String narrative;
    public final List<LatLng> polyline;

    public Step(JSONObject jsonObject) throws JSONException {
        start = new Location("");
        end = new Location("");

        start.setLongitude(jsonObject.getJSONObject("start_location").getDouble("lng"));
        start.setLatitude(jsonObject.getJSONObject("start_location").getDouble("lat"));
        end.setLongitude(jsonObject.getJSONObject("end_location").getDouble("lng"));
        end.setLatitude(jsonObject.getJSONObject("end_location").getDouble("lat"));
//        street = jsonObject.getJSONArray("streets").length() > 0 ? jsonObject.getJSONArray("streets").getString(0) : "";
//        turnType = jsonObject.getInt("turnType");
        maneuver = jsonObject.has("maneuver") ? jsonObject.getString("maneuver") : "";
        narrative = jsonObject.getString("html_instructions");
        distance = jsonObject.getJSONObject("distance").getLong("value");
        polyline = PolyUtil.decode(jsonObject.getJSONObject("polyline").getString("points"));

    }


}
