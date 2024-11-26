package ca.tech.sense.it.smart.indoor.parking.system.utility;

import static ca.tech.sense.it.smart.indoor.parking.system.utility.AppConstants.API_PLACE_SEARCH;
import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import java.util.Arrays;

public class AutocompleteSearchHelper {

    public interface PlaceSelectionCallback {
        void onPlaceSelected(Place place);
        void onError(String errorMessage);
    }

    // Initialize the AutocompleteSearch with a listener for place selection
    public static void initializeAutocompleteSearch(AutocompleteSupportFragment autocompleteFragment, Context context, final PlaceSelectionCallback callback) {
        if (!Places.isInitialized()) {
            Places.initialize(context, API_PLACE_SEARCH );
        }

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.LOCATION, Place.Field.FORMATTED_ADDRESS));
        autocompleteFragment.setHint("Search for a location");

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                if (place.getLocation() != null) {
                    callback.onPlaceSelected(place);
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                callback.onError(status.getStatusMessage());
            }
        });
    }

}
