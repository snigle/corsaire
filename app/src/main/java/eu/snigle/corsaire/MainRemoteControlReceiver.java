package eu.snigle.corsaire;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by lamarchelu on 14/05/16.
 */
public class MainRemoteControlReceiver extends BroadcastReceiver{
    private static final String TAG = "MainRemoteControlR";

    public MainRemoteControlReceiver(){
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG,"play pressed ! 1");
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            Log.i(TAG,"play pressed ! 2");
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            Log.i(TAG,"key eveny "+event);
            if (KeyEvent.ACTION_UP == event.getAction()) {
                Log.i(TAG,"play myaddress pressed !");
                Intent myIntent = new Intent(context, MyAddressService.class);
                myIntent.putExtra("myAddress", true);
                context.startService(myIntent);
            }
        }

    }
}
