package eu.snigle.corsaire.navigation;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import eu.snigle.corsaire.R;

/**
 * Created by lamarchelu on 14/05/16.
 */
public class RemoteControlReceiver extends BroadcastReceiver{
    private static final String TAG = "RemoteControlReceiver";
    private NavigationService mService;

    public RemoteControlReceiver(){
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
                Log.i(TAG,"play pressed !");
                Intent myIntent = new Intent(context, NavigationService.class);
                myIntent.putExtra("speak", true);
                context.startService(myIntent);
            }
        }

    }
}
