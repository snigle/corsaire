package eu.snigle.corsaire.settings;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import eu.snigle.corsaire.R;

public class MainSettingsActivity extends AppCompatActivity {

    private static final String TAG = "MainSettings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_settings);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case android.R.id.undo:
                onBackPressed();
                return true;
             default:
                 // Log.i(TAG,"Item : "+item+" id : "+item.getItemId());
                return super.onOptionsItemSelected(item);
        }
    }
}
