package eu.snigle.corsaire.proximite;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import eu.snigle.corsaire.R;
import eu.snigle.corsaire.settings.MainSettingsActivity;

public class CategoriesActivity extends AppCompatActivity {

    private static final String TAG = "CategoriesActivity";
    public static final String ENDPOINT = "https://maps.googleapis.com/maps/api/place/nearbysearch/json";
    private static final int REQ_PLACE = 1;
    private Map<String, List<String>> categories = null;
    private Map<String, Integer> images = null;
    private LatLng position;
    private ArrayList<String> autres;
    private boolean recherche = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        position = new LatLng(extras.getDouble("latitude"),extras.getDouble("longitude"));

        initCategories();

        SimpleAdapter adapter = new SimpleAdapter(this,getData(),R.layout.list_view_categories, new String[]{"category_name","img"}, new int[]{R.id.titre,R.id.img});
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!recherche){
                    if(position<categories.size())
                        getProximite((String) categories.keySet().toArray()[position]);
                    else{
                        getProximite(getString(R.string.autre));
                    }
                }
            }
        });

    }

    private List<Map<String, Object>> getData() {
        List<Map<String,Object>> data = new ArrayList<>();
        for(String key : categories.keySet()){
            Map<String,Object> map = new HashMap<>();
            map.put("category_name",key);
            if(images.containsKey(key))
                map.put("img",images.get(key));
            data.add(map);
        }
        Map<String,Object> map = new HashMap<>();
        String key = getString(R.string.autre);
        map.put("category_name", key);
        if(images.containsKey(key))
            map.put("img",images.get(key));
        data.add(map);

        return data;
    }

    private void getProximite(String key) {
        Log.i(TAG,"Catégorie selectionée : "+key);
        recherche = true;
        List<String> category;
        if(key == getString(R.string.autre)){
            category = autres;
        }
        else{
            category = categories.get(key);
        }
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = ENDPOINT + "?";
        url += "location=" + position.latitude + "," + position.longitude;

        url += "&language=" + Locale.getDefault().getLanguage();
        url += "&rankby=distance";
        try {
            url += "&key="+getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this,getString(R.string.erreur_505),Toast.LENGTH_LONG).show();
            finish();
        };
        url += "&types="+ category.get(0);
        for (int i = 1; i < category.size(); i++) {
            url += "|"+ category.get(i);
        }
        Log.i(TAG, url);
        Toast.makeText(this,getString(R.string.recherche_en_cours),Toast.LENGTH_SHORT).show();
        JsonObjectRequest req = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        recherche = false;
                        Intent intent = new Intent(getApplication(),SelectPlaceActivity.class);
                        intent.putExtra("json",response.toString());
                        intent.putExtra("latitude",position.latitude);
                        intent.putExtra("longitude",position.longitude);
                        startActivityForResult(intent,REQ_PLACE);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                recherche = false;
                Log.e(TAG, "error when getting places " + error);
                error.printStackTrace();
                if(error instanceof NoConnectionError){
                    Toast.makeText(getApplicationContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), getString(R.string.erreur_505), Toast.LENGTH_LONG).show();
                }
                //itineraryLayout.setVisibility(View.GONE);

            }
        });
        //        // Add the request to the RequestQueue.
        queue.add(req);
    }

    private void initCategories() {
        categories = new TreeMap<String, List<String>>() {
            {
                put(getString(R.string.bar), new ArrayList<String>() {
                    {
                        add("bar");
                    }
                });
                put(getString(R.string.boulangerie), new ArrayList<String>() {
                    {
                        add("bakery");
                    }
                });
                put(getString(R.string.bijouterie), new ArrayList<String>() {
                    {
                        add("jewelry_store");
                    }
                });

                put(getString(R.string.coiffeur), new ArrayList<String>() {
                    {
                        add("hair_care");
                    }
                });
                put(getString(R.string.culture), new ArrayList<String>() {
                    {
                        add("library");
                        add("museum");
                    }
                });
                put(getString(R.string.atm), new ArrayList<String>() {
                    {
                        add("atm");
                    }
                });
                put(getString(R.string.pharmacie), new ArrayList<String>() {
                    {
                        add("pharmacy");
                        add("museum");
                    }
                });
                put(getString(R.string.restaurant), new ArrayList<String>() {
                    {
                        add("restaurant");
                    }
                });
                put(getString(R.string.supermarche), new ArrayList<String>() {
                    {
                        add("grocery_or_supermarket");
                    }
                });
                put(getString(R.string.transport), new ArrayList<String>() {
                    {
                        add("subway_station");
                        add("bus_station");
                        add("train_station");
                    }
                });
                put(getString(R.string.shopping), new ArrayList<String>() {
                    {
                        add("shoe_store");
                        add("clothing_store");
                    }
                });
                //{"Bars","Restaurants","Supermarché","Distributeur de billet"}
            }
        };

        autres = new ArrayList<String>() {
            {
                add("amusement_park");
                add("aquarium");
                add("art_gallery");
                add("book_store");
                add("casino");
                add("cemetery");
                add("gym");
                add("mosque");
                add("pet_store");
                add("church");
                add("hindu_temple");
                add("synagogue");
            }
        };
        images = new HashMap<String, Integer>(){
            {
                put(getString(R.string.autre),R.drawable.publicc);
                put(getString(R.string.bar),R.drawable.bar);
                put(getString(R.string.bijouterie),R.drawable.necklace);
                put(getString(R.string.boulangerie),R.drawable.bread);
                put(getString(R.string.coiffeur),R.drawable.barberchair);
                put(getString(R.string.culture),R.drawable.lyre);
                put(getString(R.string.atm),R.drawable.bankcards);
                put(getString(R.string.pharmacie),R.drawable.doctorsbag);
                put(getString(R.string.restaurant),R.drawable.restaurant);
                put(getString(R.string.shopping),R.drawable.europricetag);
                put(getString(R.string.supermarche),R.drawable.shoppingcart);
                put(getString(R.string.transport),R.drawable.shuttle);
            }
        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_PLACE: {
                if (resultCode == RESULT_OK && null != data) {
                    setResult(RESULT_OK, data);
                    finish();
                }
                break;
            }
        }
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

}
