package eu.snigle.corsaire.itinerary;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import eu.snigle.corsaire.R;
import eu.snigle.corsaire.proximite.CategoriesActivity;
import eu.snigle.corsaire.proximite.SelectPlaceActivity;

/**
 * Created by lamarchelu on 03/05/16.
 */
public class ItineraryHelper {

    private static final String TAG = "ItineraryHelper";
    private static final String ENDPOINT = "https://maps.googleapis.com/maps/api/directions/json";
    private final Geocoder geocoder;
    private final GoogleApiClient mGoogleApiClient;
    private final Context context;
    private final ItineraryCallback callback;
    private boolean calculatingItinerary = false;
    private boolean gettingAddress = false;
    private boolean geocoding = false;

    public ItineraryHelper(GoogleApiClient mGoogleApiClient, Context context, ItineraryCallback callback) {
        geocoder = new Geocoder(context, Locale.getDefault());
        this.mGoogleApiClient = mGoogleApiClient;
        this.context = context;
        this.callback = callback;
    }

    public void getMyAddress(){
        if (mGoogleApiClient.isConnected() && !gettingAddress) {

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, context.getString(R.string.erreur_autorisation), Toast.LENGTH_LONG).show();
                return;
            }
            final Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                //List<Address> geocode = geocoder.getFromLocationName(result.get(0), 1, 43.265302, -0.443664, 43.339358, -0.280801);
                Toast.makeText(context,context.getString(R.string.recherche_en_cours),Toast.LENGTH_SHORT).show();
                new AsyncTask<Void,Void,Address>(){

                    private int error = 0;
                    @Override
                    protected Address doInBackground(Void... params) {
                        gettingAddress = true;
                        try {
                            List<Address> geocode = geocoder.getFromLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude(), 1);
                            if (geocode != null && geocode.size() > 0) {
                                //Take nearest geocode result
                                Address returnedArrivalAddress = geocode.get(0);
                                return returnedArrivalAddress;
                            } else {
                                error = R.string.adresse_introuvable;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            error = R.string.erreur_connexion;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Address returnedArrivalAddress) {
                        gettingAddress= false;
                        if(returnedArrivalAddress != null)
                            Toast.makeText(context,context.getString(R.string.vous_vous_trouvez_ici)+" "+returnedArrivalAddress.getAddressLine(0)+" "+returnedArrivalAddress.getLocality(),Toast.LENGTH_SHORT).show();
                        else if(error>0){
                            Toast.makeText(context, context.getString(error), Toast.LENGTH_LONG).show();
                        }
                    }
                }.execute();

            } else
                Toast.makeText(context, context.getString(R.string.erreur_localisation), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, context.getString(R.string.erreur_localisation), Toast.LENGTH_LONG).show();
        }
    }

    public void calculateItineraryWithGeocoding(final String s) {
        if(!geocoding) {

            if (mGoogleApiClient.isConnected()) {

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, context.getString(R.string.erreur_autorisation), Toast.LENGTH_LONG).show();
                    return;
                }
                final Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    //List<Address> geocode = geocoder.getFromLocationName(result.get(0), 1, 43.265302, -0.443664, 43.339358, -0.280801);
                    Toast.makeText(context, context.getString(R.string.recherche_en_cours), Toast.LENGTH_SHORT).show();
                    new AsyncTask<Void, Void, Address>() {

                        private int error = 0;

                        @Override
                        protected Address doInBackground(Void... params) {
                            geocoding = true;
                            try {
                                Pattern regex = Pattern.compile("^-?[0-9]+\\.[0-9]+,-?[0-9]+\\.[0-9]+$");
                                Log.i(TAG,"lat lng ? : "+s+" - "+ regex.matcher(s).find());
                                List<Address> geocode = !regex.matcher(s).find() ?
                                        geocoder.getFromLocationName(s, 15, mLastLocation.getLatitude() - 0.05, mLastLocation.getLongitude() - 0.05, mLastLocation.getLatitude() + 0.05, mLastLocation.getLongitude() + 0.05)
                                        : geocoder.getFromLocationName(s, 1);
                                if (geocode != null && geocode.size() > 0) {
                                    //Take nearest geocode result
                                    Address returnedArrivalAddress = geocode.get(0);
                                    for (Address i : geocode) {
                                        Location locCurrent = new Location("");
                                        Location locI = new Location("");
                                        locCurrent.setLongitude(returnedArrivalAddress.getLongitude());
                                        locCurrent.setLatitude(returnedArrivalAddress.getLatitude());
                                        locI.setLatitude(i.getLatitude());
                                        locI.setLongitude(i.getLongitude());

                                        if (locCurrent.distanceTo(mLastLocation) > locI.distanceTo(mLastLocation)) {
                                            returnedArrivalAddress = i;
                                        }
                                    }

                                    return returnedArrivalAddress;
                                } else {
                                    calculateItineraryWithPlaceAPI(s, mLastLocation);
                                    //                                error = R.string.adresse_introuvable;
                                    //                                Toast.makeText(context, context.getString(eu.snigle.proxygps.R.string.adresse_introuvable), Toast.LENGTH_LONG).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                error = R.string.erreur_connexion;
                                //                            Toast.makeText(context, context.getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Address returnedArrivalAddress) {
                            geocoding = false;
                            if (returnedArrivalAddress != null)
                                getItinerary(returnedArrivalAddress.getAddressLine(0) + " " + returnedArrivalAddress.getLocality(), new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new LatLng(returnedArrivalAddress.getLatitude(), returnedArrivalAddress.getLongitude()));
                            else if (error > 0) {
                                Toast.makeText(context, context.getString(error), Toast.LENGTH_LONG).show();
                            }
                        }
                    }.execute();

                } else
                    Toast.makeText(context, context.getString(R.string.erreur_localisation), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, context.getString(R.string.erreur_localisation), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void calculateItineraryWithPlaceAPI(String s, final Location position) {
        //https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=50.688697,3.174045&rankby=distance&name=quick%20roubaix&key=AIzaSyB6Tnnp4ZOYZUPuQYZ2v2t4Jt4IQhxVBcc
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = CategoriesActivity.ENDPOINT + "?";
        url += "location=" + position.getLatitude() + "," + position.getLongitude();

        url += "&language=" + Locale.getDefault().getLanguage();
        url += "&rankby=distance";
        try {
            url += "&key="+context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA).metaData.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(context,context.getString(R.string.erreur_505),Toast.LENGTH_SHORT).show();
        }
        url += "&name="+s.replace(" ","+");
        Log.i(TAG, url);
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            if (results.length() <= 0) {
                                Toast.makeText(context, context.getString(R.string.adresse_introuvable), Toast.LENGTH_SHORT).show();
                            }
                            else{
                                JSONObject place = results.getJSONObject(0);
                                JSONObject location = place.getJSONObject("geometry").getJSONObject("location");
                                getItinerary(place.getString("name"),new LatLng(position.getLatitude(), position.getLongitude()), new LatLng(location.getDouble("lat"),location.getDouble("lng")));
                            }
                        }catch(JSONException e){
                            Toast.makeText(context,context.getString(R.string.erreur_505),Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "error when getting places " + error);
                error.printStackTrace();
                if(error instanceof NoConnectionError){
                    Toast.makeText(context, context.getString(R.string.erreur_connexion), Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(context, context.getString(R.string.erreur_505), Toast.LENGTH_SHORT).show();
                }
                //itineraryLayout.setVisibility(View.GONE);

            }
        });
        //        // Add the request to the RequestQueue.
        queue.add(req);
    }

    public void getItinerary(final String name, LatLng depart, LatLng destination){
        getItinerary(name,depart,destination,false);
    }
    public void getItinerary(final String name, final LatLng depart, final LatLng destination, final boolean walking) {
        Log.i(TAG, "Adresse trouvée, calcul de l'itinéraire");
        if(calculatingItinerary)
            return;
        calculatingItinerary = true;
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = ENDPOINT + "?";
        url += "origin=" + depart.latitude + "," + depart.longitude;
        url += "&destination=" + destination.latitude + "," + destination.longitude;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean bus = !walking && sharedPref.getBoolean(context.getString(R.string.pref_bus_key), false);
        Log.i(TAG, "preference bus : " + bus);
        if (!bus) {
            url += "&mode=walking";
        } else {
            url += "&mode=transit";
        }
        url += "&language=" + Locale.getDefault().getLanguage();
        Log.d(TAG, url);
        if(!walking) Toast.makeText(context, context.getString(eu.snigle.corsaire.R.string.calcul_itineraire), Toast.LENGTH_SHORT).show();
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        calculatingItinerary = false;
                        try {
                            if(response.getJSONArray("routes").length()>0)
                                callback.callbackItinerary(new Itinerary(context,name, response));
                            else if(!walking)
                                getItinerary(name,depart,destination,true);
                            else
                                Toast.makeText(context, context.getString(R.string.erreur_itineraire), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, context.getString(R.string.erreur_505), Toast.LENGTH_LONG).show();
                        }
                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                calculatingItinerary = false;
                Log.e(TAG, "error when getting itinerary " + error);
                error.printStackTrace();
                if(error instanceof NoConnectionError){
                    Toast.makeText(context, context.getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(context, context.getString(R.string.erreur_505), Toast.LENGTH_LONG).show();
                }
                //itineraryLayout.setVisibility(View.GONE);

            }
        });
        //        // Add the request to the RequestQueue.
        queue.add(req);
    }
}
