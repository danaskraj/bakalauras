package lt.danielius.bakalauras.xml;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class SiteLocation {

    private LatLng location;

    public SiteLocation(LatLng location) {
        this.location = location;
    }

    public LatLng getLocation() {
        return location;
    }
}
