package eu.snigle.corsaire.itinerary.details;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lamarchelu on 05/05/16.
 */
public class TransitStep extends Step {
    public final String departureTime;
    public TransitStep(JSONObject step) throws JSONException {
        super(step);
        JSONObject transitDetails = step.getJSONObject("transit_details");
        JSONObject departureStop = transitDetails.getJSONObject("departure_stop");
        JSONObject arrivalStop = transitDetails.getJSONObject("arrival_stop");
        String shortname = "";
        if(transitDetails.has("line") && transitDetails.getJSONObject("line").has("short_name")){
            shortname = " ("+transitDetails.getJSONObject("line").getString("short_name")+")";
        }
        this.narrative = "Prendre le "+this.narrative + shortname+" de l'arrêt "+departureStop.getString("name")+" jusqu'à l'arrêt "+arrivalStop.getString("name");
        departureTime = transitDetails.getJSONObject("departure_time").getString("text");
    }
}
