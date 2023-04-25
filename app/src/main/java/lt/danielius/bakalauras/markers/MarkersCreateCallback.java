package lt.danielius.bakalauras.markers;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface MarkersCreateCallback {
    void onMarkers(List<LatLng> path, List<LatLng> markers);
}
