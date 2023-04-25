package lt.danielius.bakalauras.directions;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface DirectionsPathCallback {
    void onPath(List<LatLng> path);
}
