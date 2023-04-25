package lt.danielius.bakalauras;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

import lt.danielius.bakalauras.files.FileManager;
import lt.danielius.bakalauras.markers.MapMarkersCreator;
import lt.danielius.bakalauras.markers.MarkersCreateCallback;
import lt.danielius.bakalauras.xml.XmlParser;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private String TAG = "maps";
    EditText addressText;
    EditText rangeText;
    SeekBar rangeSeekBar;
    SeekBar fullRangeSeekBar;
    EditText fullRangeText;
    Button startButton;



    int maxRange = 1200;
    int defaultRangeValue = 100;
    double range;
    double fullRange;
    private MapMarkersCreator mapMarkersCreator = new MapMarkersCreator();
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FileManager.init(getFilesDir());
        setContentView(R.layout.activity_main);
        addressText = (EditText) findViewById(R.id.editText);
        rangeText = (EditText) findViewById(R.id.rangeText);
        rangeSeekBar = (SeekBar) findViewById(R.id.rangeSeekBar);
        fullRangeSeekBar = (SeekBar) findViewById(R.id.fullRangeSeekBar);
        fullRangeText = (EditText) findViewById(R.id.fullRangeText);
        startButton = (Button) findViewById(R.id.button);

        range = defaultRangeValue;
        fullRange = defaultRangeValue;

        rangeSeekBar.setMax(maxRange);
        rangeSeekBar.setProgress(defaultRangeValue);
        fullRangeSeekBar.setMax(maxRange);
        fullRangeSeekBar.setProgress(defaultRangeValue);
        rangeText.setText(Integer.toString(defaultRangeValue));
        fullRangeText.setText(Integer.toString(defaultRangeValue));

//         Initialize the AutocompleteSupportFragment.
        Places.initialize(getApplicationContext(), "AIzaSyCJ84sMb6ss2v27-prjVInkzEz5HtnbI3c");
        addressText.setFocusable(false);
        addressText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(MapsActivity.this);
                startActivityForResult(intent, 100);
            }
        });
        rangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                range = rangeSeekBar.getProgress();
                rangeText.setText(Integer.toString(rangeSeekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        rangeText.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {

             }

             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {

             }

             @Override
             public void afterTextChanged(Editable s) {
                 rangeText.setSelection(rangeText.getText().length());
                String rangeString = rangeText.getText().toString();
                if (!rangeString.isEmpty()) {
                    range = Integer.parseInt(rangeString);
                    rangeSeekBar.setProgress(Integer.parseInt(rangeString));
                    rangeText.setText(Integer.toString((int) range));
                }
                else {
                    rangeSeekBar.setProgress(0);
                }
                if (range > fullRange) {
                    fullRange = range;
                    fullRangeSeekBar.setProgress(Integer.parseInt(rangeString));
                }
             }

        });
        fullRangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fullRange = fullRangeSeekBar.getProgress();
                fullRangeText.setText(Integer.toString(fullRangeSeekBar.getProgress()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        fullRangeText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                fullRangeText.setSelection(fullRangeText.getText().length()-1);
                String rangeString = fullRangeText.getText().toString();
                if (range > fullRange) {
                    fullRange = range;
                    fullRangeSeekBar.setProgress((int) fullRange);
                    fullRangeText.setText(Integer.toString((int) fullRange));
                    Toast.makeText(MapsActivity.this, "Full range cannot be shorter than range left", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (!rangeString.isEmpty()) {
                        fullRange = Integer.parseInt(rangeString);
                        fullRangeSeekBar.setProgress((int) fullRange);
                    } else {
                        fullRangeSeekBar.setProgress(0);
                    }
                }
            }

        });

        StrictMode.ThreadPolicy gfgPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(gfgPolicy);
//         Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mapFragment.getMapAsync(MapsActivity.this);
            }
        });

        XmlParser xmlParser = new XmlParser(tables -> {
            mapMarkersCreator.add(tables);
            String origin = "Neuzmirstuoliu, 8, 04124 Vilnius, Lithuania";
            String destination = "Dariaus ir Gireno, 50A, 91001 Klaipeda, Lithuania";
            mapMarkersCreator.getMarkersEveryNMeters(origin, destination, (range-20)*1000, (fullRange-20)*1000);
        });
        xmlParser.start();

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng center = new LatLng(55.251987,23.459867);

        mapMarkersCreator.registerMarkersCreateCallback((path, markers) -> {
            if (path.size() > 0) {
                PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
                mMap.addPolyline(opts);
            }

            if (markers.size() > 0) {
                for (LatLng marker : markers) {
                    MarkerOptions markerOptions = new MarkerOptions().position(marker);
                    mMap.addMarker(markerOptions);
                }
            }
        });

        /*//Define list to get all latlng for the route
        List<LatLng> path = this.getDirectionsPathFromWebService(origin, destination);

        //Draw the polyline


        try {
            pathMarkers = this.getMarkersEveryNMeters(path, (range-20)*1000, (fullRange-20)*1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

//        System.out.println(pathMarkers);

        if (path.size() > 0) {
            PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
            mMap.addPolyline(opts);
        }

        if (pathMarkers.size() > 0) {
            for (LatLng marker : pathMarkers) {
                MarkerOptions markerOptions = new MarkerOptions().position(marker);
                mMap.addMarker(markerOptions);
            }
        }*/

//        mMap.getUiSettings().setZoomControlsEnabled(true);
//
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 7));
    }
}