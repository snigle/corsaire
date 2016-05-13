package eu.snigle.corsaire.itinerary.details;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lamarchelu on 05/05/16.
 */
public class WalkingStep extends Step{
    public WalkingStep(JSONObject jsonObject) throws JSONException {
        super(jsonObject);
    }
}
