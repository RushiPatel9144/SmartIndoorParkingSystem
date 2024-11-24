/*Name: Kunal Dhiman, StudentID: N01540952,  section number: RCB
  Name: Raghav Sharma, StudentID: N01537255,  section number: RCB
  Name: NisargKumar Pareshbhai Joshi, StudentID: N01545986,  section number: RCB
  Name: Rushi Manojkumar Patel, StudentID: N01539144, section number: RCB
 */
package ca.tech.sense.it.smart.indoor.parking.system.manager.favoriteManager;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoriteManager {
    private static final String PREFS_NAME = "favorites_prefs";
    private static final String FAVORITES_KEY = "favorite_locations";
    private SharedPreferences prefs;
    private Gson gson;

    public FavoriteManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void addFavorite(LatLng location) {
        List<LatLng> favorites = getFavorites();
        if (!favorites.contains(location)) {
            favorites.add(location);
            saveFavorites(favorites);
        }
    }

    public void removeFavorite(LatLng location) {
        List<LatLng> favorites = getFavorites();
        favorites.remove(location);
        saveFavorites(favorites);
    }

    public List<LatLng> getFavorites() {
        String json = prefs.getString(FAVORITES_KEY, null);
        if (json != null) {
            Type type = new TypeToken<List<LatLng>>() {}.getType();
            return gson.fromJson(json, type);
        }
        return new ArrayList<>();
    }

    private void saveFavorites(List<LatLng> favorites) {
        String json = gson.toJson(favorites);
        prefs.edit().putString(FAVORITES_KEY, json).apply();
    }
}
