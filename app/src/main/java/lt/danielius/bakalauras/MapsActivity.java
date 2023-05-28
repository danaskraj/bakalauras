package lt.danielius.bakalauras;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;

import lt.danielius.bakalauras.files.FileManager;
import lt.danielius.bakalauras.markers.MapMarkersCreator;
import lt.danielius.bakalauras.xml.XmlParser;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    EditText addressText;
    EditText rangeText;
    SeekBar rangeSeekBar;
    SeekBar fullRangeSeekBar;
    EditText fullRangeText;
    Button startButton;
    Button directionsButton;
    Switch includePaidStations;
    String uriString;
    LinearLayout dragView;
    int maxRange = 830;
    int defaultRangeValue = 100;
    double range;
    double fullRange;

    int LOCATION_REFRESH_TIME = 500;
    int LOCATION_REFRESH_DISTANCE = 5;

    String origin;
    String destination;
    private MapMarkersCreator mapMarkersCreator = new MapMarkersCreator();
    private GoogleMap mMap;

    private final android.location.LocationListener mLocationListener = location -> {
        origin = location.getLatitude() + "," + location.getLongitude();
    };

    @SuppressLint("SetTextI18n")
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
        includePaidStations = (Switch) findViewById(R.id.switch1);
        dragView = (LinearLayout) findViewById(R.id.dragView);
        directionsButton = (Button) findViewById(R.id.directionsButton);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
                    (float) LOCATION_REFRESH_DISTANCE, mLocationListener);
        }

        range = defaultRangeValue;
        fullRange = defaultRangeValue;

        rangeSeekBar.setMax(maxRange);
        rangeSeekBar.setProgress(defaultRangeValue);
        fullRangeSeekBar.setMax(maxRange);
        fullRangeSeekBar.setProgress(defaultRangeValue);
        rangeText.setText(String.valueOf(defaultRangeValue));
        fullRangeText.setText(String.valueOf(defaultRangeValue));

        Places.initialize(getApplicationContext(), "AIzaSyCJ84sMb6ss2v27-prjVInkzEz5HtnbI3c");
        addressText.setFocusable(false);

        addressText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fieldList).setCountry("LT").build(MapsActivity.this);
                startActivityForResult(intent, 100);
            }
        });
        rangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                range = rangeSeekBar.getProgress();
                if (range == 0) {
                    range = 20;
                    rangeSeekBar.setProgress((int) range);
                    Toast.makeText(MapsActivity.this, "Range cannot be 0", Toast.LENGTH_SHORT).show();
                } else if (range > fullRange) {
                    fullRange = range;
                    fullRangeSeekBar.setProgress((int) range);
                }
                rangeText.setText(String.valueOf(rangeSeekBar.getProgress()));
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
                    range = Double.parseDouble(rangeString);
                    rangeSeekBar.setProgress(Integer.parseInt(rangeString));
                } else if (Integer.parseInt(rangeString) > maxRange) {
                    rangeText.setText(Integer.toString(maxRange));
                    Toast.makeText(MapsActivity.this, "Range cannot be longer than 1200km", Toast.LENGTH_SHORT).show();
                }  else {
                    rangeSeekBar.setProgress(0);
                }
            }

        });
        fullRangeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                fullRange = fullRangeSeekBar.getProgress();
                if (range > fullRange) {
                    fullRange = range;
                    fullRangeSeekBar.setProgress((int) fullRange);
                    Toast.makeText(MapsActivity.this, "Full range cannot be shorter than range left", Toast.LENGTH_SHORT).show();
                }
                fullRangeText.setText(Integer.toString((int) fullRange));
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
                fullRangeText.setSelection(fullRangeText.getText().length());
                String rangeString = fullRangeText.getText().toString();
                if (!rangeString.isEmpty()) {
                    fullRange = Double.parseDouble(rangeString);
                    fullRangeSeekBar.setProgress((int) fullRange);
                } else if (Integer.parseInt(rangeString) > maxRange) {
                    fullRangeText.setText(Integer.toString(maxRange));
                } else {
                    fullRangeSeekBar.setProgress(0);
                }
            }

        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                XmlParser xmlParser = new XmlParser(tables -> {
                    mapMarkersCreator.add(tables, includePaidStations.isChecked());
                    mapMarkersCreator.getMarkersEveryNMeters(origin, destination, (range - 20) * 1000, (fullRange - 20) * 1000);
                });
                xmlParser.start();
                mapFragment.getMapAsync(MapsActivity.this);
            }
        });

        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse(uriString);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        gmmIntentUri);
                startActivity(intent);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            addressText.setText(place.getAddress());
            destination = place.getAddress();
        }
        else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            Toast.makeText(getApplicationContext(), status.getStatusMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.clear();

        LatLng center = new LatLng(55.2865962103287, 23.95164971603239);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 6));

        mapMarkersCreator.registerMarkersCreateCallback((path, markers) -> {
            if (path.size() > 0) {
                PolylineOptions opts = new PolylineOptions().addAll(path).color(Color.BLUE).width(5);
                mMap.addPolyline(opts);
            }
            uriString = "https://www.google.com/maps/dir/?api=1" + "&destination=" + destination + "&waypoints=";
            if (markers.size() > 0) {
                mMap.addMarker(new MarkerOptions().position(markers.get(0)));
                for (LatLng marker : markers.subList(1, markers.size()-1)) {
                    MarkerOptions markerOptions = new MarkerOptions().position(marker);
                    mMap.addMarker(markerOptions);
                    uriString += marker.latitude + "," + marker.longitude + "|";
                }
                uriString += "&travelmode=driving&dir_action=navigate";
                mMap.addMarker(new MarkerOptions().position(markers.get(markers.size()-1)));
            }
            directionsButton.setVisibility(View.VISIBLE);
        });
    }
}