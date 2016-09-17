package eu.snigle.corsaire.proximite;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.snigle.corsaire.R;

public class SelectPlaceActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "SelectPlace";
    private final List<Place> places = new ArrayList<>();
    private final Location location = new Location("");
    private boolean showMap = false;
    private GoogleMap mMap = null;
    private boolean isExploreByTouchEnabled = false;
    private Marker selectedMarker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_place);

        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        isExploreByTouchEnabled = am.isTouchExplorationEnabled();

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();

        location.setLatitude(extras.getDouble("latitude"));
        location.setLongitude(extras.getDouble("longitude"));
        try {
            JSONArray results = new JSONObject(extras.getString("json")).getJSONArray("results");
            if (results.length() <= 0) {
                Toast.makeText(this, getString(R.string.erreur_pas_de_place_proximite), Toast.LENGTH_LONG).show();
                finish();
                return;
            }
            for (int i = 0; i < results.length(); i++) {
                JSONObject item = results.getJSONObject(i);
                JSONObject location = item.getJSONObject("geometry").getJSONObject("location");
                places.add(new Place(item.getString("name"), location.getDouble("lat"), location.getDouble("lng")));
            }
            Log.i(TAG, "Reponse : " + results.toString());

            List<Map<String,Object>> data = new ArrayList<>();
            for (Place place : places) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("contentDescription",place.name + " - " + Math.round(place.location.distanceTo(location)) + "m");
                map.put("name",place.name);
                map.put("distance",Math.round(place.location.distanceTo(location)) + "m");
                data.add(map);
            }
            SimpleAdapter adapter = new SimpleAdapter(this,data,R.layout.list_view_select_place, new String[]{"name","distance"}, new int[]{R.id.name,R.id.distance});


            ListView listView = (ListView) findViewById(R.id.listView);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent result = new Intent();
                    Place place = places.get(position);
                    result.putExtra("name", place.name);
                    result.putExtra("latitude", place.location.getLatitude());
                    result.putExtra("longitude", place.location.getLongitude());
                    setResult(RESULT_OK, result);
                    finish();
                }
            });

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            if(mapFragment != null)
            mapFragment.getMapAsync(this);
            findViewById(R.id.map).setVisibility(View.GONE);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.erreur_505), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case android.R.id.undo:
                onBackPressed();
                return true;
            case R.id.action_map:
                toggleMap();
                return true;
            default:
                // Log.i(TAG,"Item : "+item+" id : "+item.getItemId());
                return super.onOptionsItemSelected(item);
        }
    }

    private void toggleMap() {
        showMap = !showMap;
        if (!showMap) {
            findViewById(R.id.map).setVisibility(View.GONE);
        } else {
            findViewById(R.id.map).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(!isExploreByTouchEnabled) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_select_place, menu);
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
        final LatLngBounds.Builder bounds = LatLngBounds.builder();
        for(Place place : places){
            mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(place.location.getLatitude(),place.location.getLongitude()))
                    .title(place.name)
                    );
            bounds.include(new LatLng(place.location.getLatitude(),place.location.getLongitude()));
        }
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i(TAG,"select place ");
                if(marker.equals(selectedMarker)) {
                    Log.i(TAG,"info window shown");
                    Intent result = new Intent();
                    result.putExtra("name", marker.getTitle());
                    result.putExtra("latitude", marker.getPosition().latitude);
                    result.putExtra("longitude", marker.getPosition().longitude);
                    setResult(RESULT_OK, result);
                    finish();
                }
                selectedMarker = marker;
                marker.showInfoWindow();
                return true;
            }
        });

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),200));

            }
        });
    }

    @Override
    public void onBackPressed() {
        if(showMap){
            toggleMap();
        }
        else{
            super.onBackPressed();
        }
    }
}
