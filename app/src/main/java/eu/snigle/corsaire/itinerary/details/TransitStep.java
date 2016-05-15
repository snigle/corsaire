package eu.snigle.corsaire.itinerary.details;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import eu.snigle.corsaire.R;

/**
 * Created by lamarchelu on 05/05/16.
 */
public class TransitStep extends Step {
    public final String departureTime;
    public TransitStep(Context context, JSONObject step) throws JSONException {
        super(step);
        JSONObject transitDetails = step.getJSONObject("transit_details");
        JSONObject departureStop = transitDetails.getJSONObject("departure_stop");
        JSONObject arrivalStop = transitDetails.getJSONObject("arrival_stop");
        String shortname = "";
        if(transitDetails.has("line") && transitDetails.getJSONObject("line").has("short_name")){
            shortname = " ("+transitDetails.getJSONObject("line").getString("short_name")+")";
        }
        this.narrative = context.getString(R.string.prendre_le)+" "+this.narrative + shortname+" "+context.getString(R.string.de_larret)+" "+departureStop.getString("name")+" "+context.getString(R.string.jusqua_larret)+" "+arrivalStop.getString("name");
        departureTime = transitDetails.getJSONObject("departure_time").getString("text");
    }
}
