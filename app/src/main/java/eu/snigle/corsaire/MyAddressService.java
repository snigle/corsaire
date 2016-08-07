package eu.snigle.corsaire;

import android.Manifest;
import android.app.IntentService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.places.Places;

import org.json.JSONException;
import org.json.JSONObject;

import eu.snigle.corsaire.itinerary.Itinerary;
import eu.snigle.corsaire.itinerary.ItineraryCallback;
import eu.snigle.corsaire.itinerary.ItineraryHelper;
import eu.snigle.corsaire.navigation.RemoteControlReceiver;

public class MyAddressService extends Service implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, TextToSpeech.OnInitListener, ItineraryCallback {
    private static final String TAG = "MyAddressService";
    private AudioManager am;
    private GoogleApiClient mGoogleApiClient;
    private ItineraryHelper itineraryHelper;
    private ComponentName componentName;
    private TextToSpeech mTts;
    private SensorManager mSensorManager;
    private float[] mRotationMatrix = new float[9];
    private Double bearing;

    public MyAddressService() {
        Log.i(TAG, "Create");

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "on create");
        componentName = new ComponentName(this, MainRemoteControlReceiver.class);
        am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        Log.i(TAG, MainRemoteControlReceiver.class.getPackage().getName() + " " + MainRemoteControlReceiver.class.getSimpleName());


        //Premier lancement
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        itineraryHelper = new ItineraryHelper(mGoogleApiClient, getApplicationContext(), this);
        mTts = new TextToSpeech(this, this);
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);



    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                builder.build());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onInit(int status) {

    }

    @Override
    public void callbackItinerary(Itinerary itinerary) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"Service startCommand");
        am.registerMediaButtonEventReceiver(componentName);
        if(intent != null) {
            if (intent.getExtras() != null && intent.getExtras().getBoolean("myAddress", false)) {
                Log.i(TAG, "give my address ! ");
                itineraryHelper.getMyAddress(mTts, bearing);
            } else if (intent.getExtras() != null && intent.getExtras().getBoolean("close", false)) {
                Log.i(TAG, "close my address ! ");
                stopSelf();
            }
        }
        return super.onStartCommand(intent, START_FLAG_REDELIVERY, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "fin du service give my address");
        if(am != null){
            Log.i(TAG,"am ok");
            am.unregisterMediaButtonEventReceiver(componentName);
        }
        if(mTts != null){
            mTts.stop();
            mTts.shutdown();
        }
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        super.onDestroy();
    }

    public void onTaskRemoved(Intent rootIntent) {

        //unregister listeners
        //do any other cleanup if required

        //stop service
        stopSelf();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.values != null && event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
//            Log.i(TAG,"sensor changed");
            SensorManager.getRotationMatrixFromVector(
                    mRotationMatrix , event.values);
            float[] orientation = new float[3];
            SensorManager.getOrientation(mRotationMatrix, orientation);
            if(orientation != null ) {
                bearing = Math.toDegrees(orientation[0]);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
