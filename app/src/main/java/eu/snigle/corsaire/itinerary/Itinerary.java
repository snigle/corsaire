package eu.snigle.corsaire.itinerary;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

import eu.snigle.corsaire.itinerary.details.Step;
import eu.snigle.corsaire.itinerary.details.TransitStep;
import eu.snigle.corsaire.itinerary.details.WalkingStep;

/**
 * Created by lamarchelu on 03/05/16.
 */
public class Itinerary {
    public final String startAddress;
    public final String destination;
    public final int temps;
    public final int distance;
    public final LinkedList<Step> steps;
    public final String polyline;
    public final String tempsText;
    public final String distanceText;
    public final String name;
    public final String json;

    public Itinerary(String name, JSONObject json) throws JSONException {
        JSONObject leg = json.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0);
        this.destination = leg.getString("end_address");
        this.startAddress = leg.getString("start_address");
        this.temps = leg.getJSONObject("duration").getInt("value");
        this.distance = leg.getJSONObject("distance").getInt("value");
        this.tempsText = leg.getJSONObject("duration").getString("text");
        this.distanceText = leg.getJSONObject("distance").getString("text");
        JSONArray json_steps = leg.getJSONArray("steps");
        steps = new LinkedList<>();
        for (int i = 0; i < json_steps.length(); i++) {
            JSONObject step = new JSONObject(json_steps.getJSONObject(i).toString());
            if(step.getString("travel_mode").equals("WALKING")){
                if(step.has("steps")) {
                    for (int j = 0; j < step.getJSONArray("steps").length(); j++) {
                        JSONObject subStep = new JSONObject(step.getJSONArray("steps").getJSONObject(j).toString());
                        if (!subStep.has("html_instructions")) {
                            subStep.put("html_instructions", step.getString("html_instructions"));
                        }
//                        subStep.put("html_instructions", subStep.getString("html_instructions").split("<div")[0]);
//                        if(j>0)
//                            subStep.put("polyline",step.getJSONArray("steps").getJSONObject(j-1).getJSONObject("polyline"));
                        steps.add(new WalkingStep(subStep));
                    }
//                    JSONObject lastStep = step.getJSONArray("steps").getJSONObject(step.getJSONArray("steps").length()-1);
//                    if(lastStep.getString("travel_mode").equals("WALKING")) {
//                        lastStep.put("html_instructions", Html.fromHtml(lastStep.getString("html_instructions")).toString().split("\n\n")[1]);
//                        lastStep.put("start_location", lastStep.getJSONObject("end_location"));
//                    }
//                    steps.add(new WalkingStep(lastStep));
                }
                else{
//                    if(i>0)
//                        step.put("polyline",json_steps.getJSONObject(i-1).getJSONObject("polyline"));
//                    step.put("html_instructions", step.getString("html_instructions").split("<div")[0]);

                    steps.add(new WalkingStep(step));
                }
            }
            else if(step.getString("travel_mode").equals("TRANSIT")){
                steps.add(new TransitStep(step));
            }
        }
//        JSONObject lastStep = json_steps.getJSONObject(json_steps.length() - 1);
//        if(lastStep.getString("travel_mode").equals("WALKING")) {
//            Log.i("ITINERARY",Html.fromHtml(lastStep.getString("html_instructions")).toString());
//            lastStep.put("html_instructions", Html.fromHtml(lastStep.getString("html_instructions")).toString().split("\n\n")[1]);
//            lastStep.put("start_location", lastStep.getJSONObject("end_location"));
//        }
//        steps.add(new WalkingStep(lastStep));

        polyline = json.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
        this.name = name;
        this.json = json.toString();
    }

    public String getGPX(){
        String res = "<?xml version=\"1.0\"?>\n" +
                "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\" version=\"1.1\" creator=\"EditGPX\">\n" +
                "  <trk>\n" +
                "    <name>2016-04-08T17:04:06Z</name>\n" +
                "    <trkseg>";
        for(Step step : steps){
            for(LatLng location : step.polyline) {
                res += "<trkpt lat=\"" + location.latitude + "\" lon=\""+ location.longitude +"\">\n" +
                        "        <ele>214</ele>\n" +
                        "      </trkpt>";
            }
        }
        res += "</trkseg>\n" +
                "  </trk>\n" +
                "</gpx>";
        return res;
    }

    public LatLng getDest(){
        Location last = steps.getLast().end;
        return new LatLng(last.getLatitude(),last.getLongitude());
    }

}
