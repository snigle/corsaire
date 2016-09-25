package eu.snigle.corsaire.itinerary.favorite;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.JsonWriter;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import eu.snigle.corsaire.R;

/**
 * Created by lamarchelu on 27/08/16.
 */
public class FavoriteHelper {

    private static final String TAG = "FavoriteHelper";
    private final SharedPreferences preferences;
    private final Context context;
    private final Map<String,Favorite> favorites;
    private final Gson gson;

    public FavoriteHelper(Context context) {
        preferences = context.getSharedPreferences(
                context.getString(R.string.preference_favorite_key), Context.MODE_PRIVATE);
        this.context = context;
        gson = new GsonBuilder().create();
        favorites = gson.fromJson(preferences.getString(context.getString(R.string.preference_favorite_key), gson.toJson(new HashMap<String,Favorite>())), new TypeToken<HashMap<String,Favorite>>(){}.getType());
    }

    public void add(String name, LatLng location) {
        favorites.put(name, new Favorite(name, location.latitude, location.longitude));
        save();
    }

    public void save() {
        preferences.edit().putString(context.getString(R.string.preference_favorite_key), gson.toJson(favorites)).commit();
        Log.i(TAG,gson.toJson(favorites));
    }

    public void delete(String name) {
        favorites.remove(name);
        save();
    }

    public Set<String> getFavoriteKeys() {
        return favorites.keySet();
    }

    public Map<String, Favorite> getFavorites() {
        return favorites;
    }
}
