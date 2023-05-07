package lt.danielius.bakalauras.directions;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;
import com.google.maps.model.DirectionsStep;
import com.google.maps.model.EncodedPolyline;

import java.util.ArrayList;
import java.util.List;

public class DirectionsPathRetriever extends Thread {

    private static final String TAG = "DirectionsPathRetriever";

    private static final GeoApiContext context = new GeoApiContext.Builder()
            .apiKey("AIzaSyCJ84sMb6ss2v27-prjVInkzEz5HtnbI3c")
            .build();
    private final String origin, destination;
    private final DirectionsPathCallback callback;

    public DirectionsPathRetriever(String origin, String destination, DirectionsPathCallback callback){
        this.origin = origin;
        this.destination = destination;
        this.callback = callback;
    }

    @Override
    public void run() {
        List<LatLng> path = new ArrayList<>();

        //Execute Directions API request
        DirectionsApiRequest request = DirectionsApi.getDirections(context, origin, destination);
        try {
            DirectionsResult result = request.await();

            //Loop through legs and steps to get encoded polylines of each step
            if (result.routes != null && result.routes.length > 0) {
                DirectionsRoute route = result.routes[0];

                if (route.legs !=null) {
                    for(int i=0; i<route.legs.length; i++) {
                        DirectionsLeg leg = route.legs[i];
                        if (leg.steps != null) {
                            for (int j=0; j<leg.steps.length;j++){
                                DirectionsStep step = leg.steps[j];
                                if (step.steps != null && step.steps.length >0) {
                                    for (int k=0; k<step.steps.length;k++){
                                        DirectionsStep step1 = step.steps[k];
                                        EncodedPolyline points1 = step1.polyline;
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            List<com.google.maps.model.LatLng> coords1 = points1.decodePath();
                                            for (com.google.maps.model.LatLng coord1 : coords1) {
                                                path.add(new LatLng(coord1.lat, coord1.lng));
                                            }
                                        }
                                    }
                                } else {
                                    EncodedPolyline points = step.polyline;
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        List<com.google.maps.model.LatLng> coordinates = points.decodePath();
                                        for (com.google.maps.model.LatLng coordinate : coordinates) {
                                            path.add(new LatLng(coordinate.lat, coordinate.lng));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch(Exception ex) {
            Log.e(TAG, ex.toString());
        }

        if(callback != null){
            callback.onPath(path);
        }
    }
}
