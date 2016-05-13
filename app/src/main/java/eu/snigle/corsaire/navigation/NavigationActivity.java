package eu.snigle.corsaire.navigation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import eu.snigle.corsaire.R;
import eu.snigle.corsaire.itinerary.details.DetailsActivity;
import eu.snigle.corsaire.itinerary.details.Step;
import eu.snigle.corsaire.itinerary.details.TransitStep;
import eu.snigle.corsaire.settings.MainSettingsActivity;

public class NavigationActivity extends AppCompatActivity implements SensorEventListener, OnMapReadyCallback {

    private static final int REQ_NAVIGATION = 1;
    private static final String TAG = "NavigationActivity";
    private static final int DEFAULT_ZOOM = 18;
    private Marker myPosition = null;
    NavigationService mService;
    boolean mBound = false;

    ResultReceiver mReceiver = new ResultReceiver(new Handler()) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            switch (resultCode) {
                case NavigationService.EXIT:
                    Log.i(TAG, "exit activity");
                    finish();
                case NavigationService.UPDATE_CODE:
                    updateView();
                default:
                    Log.i(TAG, "receive result " + resultCode);
            }
        }
    };
    private boolean vibrate = false;
    private Vibrator vibrator = null;
    private SensorManager mSensorManager;
    private boolean isExploreByTouchEnabled = false;
    private GoogleMap mMap;
    private boolean centred = true;
    private Polyline polyline;
    private View.OnTouchListener boussoleListener;
    private ImageButton myLocationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        Button bigBoussole = (Button) findViewById(R.id.big_boussole);
        myLocationButton = (ImageButton) findViewById(R.id.myLocation);
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        isExploreByTouchEnabled = am.isTouchExplorationEnabled();
        if (isExploreByTouchEnabled) {
            mapFragment.getView().setVisibility(View.GONE);
            findViewById(R.id.boussole).setVisibility(View.GONE);
            myLocationButton.setVisibility(View.GONE);
        } else {
            findViewById(R.id.ma_position).setVisibility(View.GONE);
            bigBoussole.setVisibility(View.GONE);
            mapFragment.getMapAsync(this);
        }


        boussoleListener = new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int code = event.getAction() & MotionEvent.ACTION_MASK;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    //mSensorManager.unregisterListener(listener);
                    vibrate = false;
                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.i(TAG, "register listener");
                    //mSensorManager.registerListener(listener, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);
                    vibrate = true;
                    if (mBound && mService.getAngle() == null) {
                        Toast.makeText(getApplicationContext(), getString(R.string.erreur_localisation), Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        };
        View.OnLongClickListener longClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        };
        findViewById(R.id.boussole).setOnTouchListener(boussoleListener);
        findViewById(R.id.big_boussole).setOnTouchListener(boussoleListener);
        findViewById(R.id.boussole).setOnLongClickListener(longClickListener);
        findViewById(R.id.big_boussole).setOnLongClickListener(longClickListener);

    }

    public void help(View view){
        Toast.makeText(NavigationActivity.this, getString(R.string.message_aide_boussole), Toast.LENGTH_LONG).show();
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            NavigationService.LocalBinder binder = (NavigationService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            if (mService.getItinerary() == null) {
                mService.quit();
                Toast.makeText(NavigationActivity.this, getString(R.string.error_lancement_navigation), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                mService.setReceiver(mReceiver);
                updateView();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private void updateView() {
        Step step = mService.getCurrent();
        String maneuver = mService.getManeuver();
        ((LinearLayout) findViewById(R.id.direction_layout)).setContentDescription(getString(R.string.dans)+" "+mService.getDistance()+" mètres, "+Html.fromHtml(mService.getNarrative()).toString());
        ((TextView) findViewById(R.id.distance)).setText(mService.getDistance() + " m");
        ((TextView) findViewById(R.id.narrative)).setText(Html.fromHtml(mService.getNarrative()));
        if(step instanceof TransitStep){
            ((ImageView) findViewById(R.id.image_direction)).setImageResource(R.drawable.ic_directions_transit_white_48dp);
        }
        else if(maneuver.contains("right")){
            ((ImageView) findViewById(R.id.image_direction)).setImageResource(R.drawable.right_turn);
        }else if(maneuver.contains("left")){
            ((ImageView) findViewById(R.id.image_direction)).setImageResource(R.drawable.left_turn);
        }
        else{
            ((ImageView) findViewById(R.id.image_direction)).setImageResource(R.drawable.straight);
        }
        Location mLastLocation = mService.getLocation();
        if (mMap != null) {
            myPosition.setPosition(mService.getLatLng());
            if (centred && mLastLocation != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), DEFAULT_ZOOM));
            }
            if (polyline != null)
                polyline.remove();
                polyline = mMap.addPolyline(new PolylineOptions().addAll(PolyUtil.decode(mService.getItinerary().polyline)).color(getResources().getColor(R.color.colorAccent)));
        }

    }

    @Override
    protected void onPause() {
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "resume " + getIntent().getExtras());
        Intent intent = new Intent(this, NavigationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onBackPressed() {
        if (mBound) {
            mService.quit();
        }
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "result " + resultCode);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        //now getIntent() should always return the last received intent
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case android.R.id.undo:
                onBackPressed();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, MainSettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                // Log.i(TAG,"Item : "+item+" id : "+item.getItemId());
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    public void details(View view) {
        Intent intent = new Intent(this, DetailsActivity.class);
        startActivity(intent);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mBound && mService.getAngle() != null && event.values != null && event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            float[] mRotationMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix, event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(mRotationMatrix, orientation);
            if (orientation != null) {
                double bearing = Math.toDegrees(orientation[0]) + mService.getDeclination();
                orienteCamera((float) bearing);
                long diff = Math.abs(Math.round(bearing) - Math.round(mService.getAngle()));
                if (vibrate && diff < 30 && diff != 0) {
                    vibrator.cancel();
                    vibrator.vibrate((1 / diff) * 2000 + 100);
                    //Log.i(TAG, "Mon angle : " + Math.round(bearing) + "  --- lieux : " + Math.round(angle));
                }

            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Location location = mService.getLocation();
                NumberFormat f = new DecimalFormat("000.0000");
                if(location != null){
                    if(!f.format(location.getLatitude()).equals(f.format(cameraPosition.target.latitude))
                            || !f.format(location.getLongitude()).equals(f.format(cameraPosition.target.longitude))
                            || DEFAULT_ZOOM!=Math.round(cameraPosition.zoom)){
                        centred = false;
                        myLocationButton.setBackgroundResource(R.drawable.rounded_button_desactivated);
                    }
                    else{
                        centred = true;
                        myLocationButton.setBackgroundResource(R.drawable.rounded_button);
                    }
                }
//                if(location != null && !cameraPosition.target.equals(new LatLng(location.getLatitude(),location.getLongitude()))){
//                    Log.i(TAG,"camera change");
//                }
            }
        });
        myPosition= mMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.my_location)).title(getString(R.string.ma_position)));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });
        myLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                centred = true;
                myLocationButton.setBackgroundResource(R.drawable.rounded_button);
                if(mBound) {
                    Location location = mService.getLocation();
                    if(location != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
                    }
                }
            }
        });
    }

    private void orienteCamera(float bearing) {
        if(mMap != null && centred) {
            CameraPosition oldPos = mMap.getCameraPosition();

            CameraPosition pos = CameraPosition.builder(oldPos).bearing(bearing).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(pos));
        }
    }

    public void monAdresse(View v){
        if(mBound)
            mService.getMyAddress();
    }
}

