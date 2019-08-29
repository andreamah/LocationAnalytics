package io.github.andreamah.locationtracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private double longg;
    private Circle capture = null;
    private Circle capture_center = null;
    private double lat;
    private LocationViewModel locationViewModel;
    private List<LocationEntity> location_array;
    private static final String TAG = "MainActivity";
    private GoogleMap mMap;
    private TextView capture_location;
    private TextView capture_distance;
    private TextView marker_results;
    private Button refresh_button;
    private SeekBar radius_sb;
    private boolean mLocationPermissionGranted = false;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private Boolean mLocationPermissionsGranted = false;
    private static final float DEFAULT_ZOOM = 15f;
    private int radius = 100;
    private LatLng capture_point = null;

    private Set<Marker> in_capture = null;
    @Override
    protected void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        location_array = new ArrayList<>();
        in_capture = new HashSet<>();

        capture_distance = (TextView) findViewById(R.id.capture_radius);
        capture_distance.setText(radius + "m");
        capture_location = (TextView) findViewById(R.id.capture_center);
        marker_results = (TextView) findViewById(R.id.results_found);
        radius_sb = (SeekBar) findViewById(R.id.radius_seek_bar);
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);
        locationViewModel.getAllLocations().observe(this, new Observer<List<LocationEntity>>() {
            @Override
            public void onChanged(@Nullable List<LocationEntity> locationEntities) {
                location_array = locationEntities;
                setCircle(capture_point);
            }
        });
        getLocationPermission();

        radius_sb.setMax(400);
        radius_sb.setProgress(100);
        radius_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radius = progress;
                capture_distance.setText(progress + "m");
                capture.setRadius(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (capture_point != null)
                    setCircle(capture_point);
            }
        });

            // Create the Handler object (on the main thread by default)
        final Handler handler = new Handler();
        // Define the code block to be executed
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {
                // Do something here on the main thread
                if (location_array != null)
                    Log.d("Handlers", Integer.toString(location_array.size()));
                   getDeviceLocation();

                    ///////////////////
                    Date date = new Date();
                    getDeviceLocation();
                    LocationEntity le = new LocationEntity(lat, longg, date, 1);
                    locationViewModel.insert(le);
                    getHeatMap();
                // Repeat this the same runnable code block again another 2 seconds
                handler.postDelayed(this, 600000);
            }
        };
        // Start the initial runnable task by posting through the handler
        handler.post(runnableCode);


    }

    private void setCircle(LatLng point) {
        in_capture = new HashSet<>();
        mMap.clear();
//                mMap.addMarker(new MarkerOptions().position(point));

        capture = mMap.addCircle(new CircleOptions()
                .center(point)
                .radius(radius)
                .strokeColor(0x150000FF)
                .fillColor(0x220000FF));
        capture_center = mMap.addCircle(new CircleOptions()
                .center(point)
                .radius(1)
                .strokeColor(0x100000FF)
                .fillColor(0x100000FF));
        getHeatMap();
        capture_point = point;


        DecimalFormat numberFormat = new DecimalFormat("#.000000");
        capture_location.setText(numberFormat.format(point.latitude) + ", " + numberFormat.format(point.longitude));
        refreshCapturePoints(point);
    }

    private void refreshCapturePoints(LatLng point) {
        int num_results = 0;
        for (LocationEntity l : location_array) {
            LatLng ll = new LatLng(l.getLatitude(), l.getLongitude());
            float ret[] = new float[1];
            Location.distanceBetween(l.getLatitude(), l.getLongitude(), point.latitude, point.longitude, ret);

            if (ret[0] < radius) {
                num_results++;
                mMap.addMarker(new MarkerOptions().position(ll).title(l.getDate().toString()));
            }
        }

        marker_results.setText(num_results + " results found");
    }
    private void getHeatMap () {
            ArrayList<WeightedLatLng> latlngs = new ArrayList<>();

            for (LocationEntity loc : location_array) {
                LatLng latlng = new LatLng(loc.getLatitude(), loc.getLongitude());
                WeightedLatLng wlatlng = new WeightedLatLng(latlng, loc.getWeight());
                latlngs.add(wlatlng);

//        mMap.addMarker(new MarkerOptions().position(latlng).title(loc.getDate().toString()));
            }
            HeatmapTileProvider mProvider;
            // Create a heat map tile provider, passing it the latlngs of the police stations.
            if (!latlngs.isEmpty()) {
                mProvider = new HeatmapTileProvider.Builder()
                        .weightedData(latlngs)
                        .build();
                // Add a tile overlay to the map, using the heat map tile provider.
                mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
            }
        }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
//                allPoints.add(point);
                setCircle(point);
//                radius_sb.setVisibility(View.VISIBLE);
            }
        });

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        if (mLocationPermissionsGranted) {
            getDeviceLocation();
            LatLng latLng = new LatLng(lat, longg);
            moveCamera(latLng, DEFAULT_ZOOM);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

        }
    }


    private FusedLocationProviderClient mFusedLocationProviderClient;

    public void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the devices current location");
        if(mLocationPermissionsGranted){
            GPSLocationManager gps = new GPSLocationManager(MapsActivity.this);
            int status = 0;
            if(gps.canGetLocation())

            {
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext());

                if (status == ConnectionResult.SUCCESS) {
                    lat = gps.getLatitude();
                    longg = gps.getLongitude();
                    if (capture == null)
                        setCircle(new LatLng(lat,longg));

                    Log.d("dashlatlongon",  lat + "-"
                            + longg);

                }
            }
            else
            {
                gps.showSettingsAlert();
            }
        }
    }


    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }
}
