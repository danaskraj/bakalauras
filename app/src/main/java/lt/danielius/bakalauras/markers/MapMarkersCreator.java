package lt.danielius.bakalauras.markers;

import android.location.Location;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import lt.danielius.bakalauras.directions.DirectionsPathRetriever;
import lt.danielius.bakalauras.xml.InfrastructureSite;
import lt.danielius.bakalauras.xml.InfrastructureStation;
import lt.danielius.bakalauras.xml.InfrastructureTable;

public class MapMarkersCreator {

    private static final String TAG = "MapMarkersCreator";
    private Handler handler = new Handler();
    private CreatorStatus status = CreatorStatus.UNINITIALIZED;
    private final List<LatLng> stationLocationList = new ArrayList<>();
    private MarkersCreateCallback callback;

    public MapMarkersCreator(){}

    public MapMarkersCreator(HashMap<String, InfrastructureTable> tables){
        add(tables);
        status = CreatorStatus.READY;
    }

    public void add(HashMap<String, InfrastructureTable> tables){
        for (InfrastructureTable table : tables.values()) {
            for (InfrastructureSite site : table.getSites()) {
                for (InfrastructureStation station : site.getStations()) {
                    stationLocationList.add(station.getSiteLocation().getLocation());
                }
            }
        }
        status = CreatorStatus.READY;
    }

    public void add(List<LatLng> stations){
        this.stationLocationList.addAll(stations);
        status = CreatorStatus.READY;
    }

    public CreatorStatus getStatus() {
        return status;
    }

    public void registerMarkersCreateCallback(MarkersCreateCallback callback){
        this.callback = callback;
    }

    public void getMarkersEveryNMeters(String origin, String destination, double firstDistance, double otherDistances) {
        if(status == CreatorStatus.UNINITIALIZED){
            Log.d(TAG, "Uninitialized");
            handler.post(() -> {
                if(callback != null)
                    callback.onMarkers(new ArrayList<>(), new ArrayList<>());
            });
        }

        DirectionsPathRetriever directionsPathRetriever = new DirectionsPathRetriever(origin, destination, path -> {
            List<LatLng> markers = getMarkersEveryNMeters(path, firstDistance, otherDistances);
            handler.post(() -> {
                if(callback != null)
                    callback.onMarkers(path, markers);
            });
        });
        directionsPathRetriever.start();
    }

    private List<LatLng> getMarkersEveryNMeters(List<LatLng> path, double firstDistance, double otherDistances){
        List<LatLng> res = new ArrayList<>();
        double distance = firstDistance;
        LatLng p0 = path.get(0);
        res.add(p0);
        if (path.size() > 2) {
            //Initialize temp variables for sum distance between points and
            //and save the previous point
            double tmp = 0;
            LatLng prev = p0;
            int pathIndex = 0;
            int previousPointIndex = 0;
            while (pathIndex < path.size()) {
                LatLng p = path.get(pathIndex);
                //Sum the distance
                tmp += SphericalUtil.computeDistanceBetween(prev, p);
                if (tmp < distance) {
                    //If it is less than certain value continue sum
                    prev = p;
                } else {
                    //If distance is greater than certain value lets calculate
                    //how many meters over desired value we have and find position of point
                    //that will be at exact distance value

                    double diff = tmp - distance;
                    double heading = SphericalUtil.computeHeading(prev, p);

                    LatLng pp = SphericalUtil.computeOffsetOrigin(p, diff, heading);

                    HashMap<Double, LatLng> stationsAroundMarker = new HashMap<>();

                    Location loc = new Location("");
                    loc.setLatitude(pp.latitude);
                    loc.setLongitude(pp.longitude);
                    for (LatLng stationLocation : stationLocationList) {
                        Location loc2 = new Location("");
                        loc2.setLatitude(stationLocation.latitude);
                        loc2.setLongitude(stationLocation.longitude);
                        double radius = 20000.0;
                        double distanceBetweenTwoMarkers = loc.distanceTo(loc2);
                        if (distanceBetweenTwoMarkers < radius) {
                            if (!stationsAroundMarker.values().contains(stationLocation)) {
                                stationsAroundMarker.put(distanceBetweenTwoMarkers, stationLocation);
                            }
                        }
                    }
                    if (!stationsAroundMarker.isEmpty()) {
                        Set<Double> keySet = stationsAroundMarker.keySet();
                        ArrayList<Double> distances = new ArrayList<Double>(keySet);
                        Collections.sort(distances);
                        prev = pp;
                        res.add(stationsAroundMarker.get(distances.get(0)));
                        distance = otherDistances;
                        previousPointIndex = path.indexOf(pp);
                    }
                    else {
                        distance = distance-20000.0;
                        pathIndex = previousPointIndex;
                    }
                    stationsAroundMarker.clear();
                    tmp = 0;


                }
                pathIndex++;
            }

            //Add the last point of route
            LatLng plast = path.get(path.size()-1);
            res.add(plast);
        }

        return res;
    }
}
