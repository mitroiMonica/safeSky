package com.example.hackathon;

import android.os.Vibrator;
import com.google.android.gms.maps.model.Circle;




import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    private MapView mapView;
    private LatLng coordinatesToSend;
    String description;
    private GoogleMap googleMap;

    private List<Marker> markers = new ArrayList<>();

    Button btnAlert;

    private List<Circle> circles = new ArrayList<>();


    /// handler && runnable
    private Handler locationCheckHandler;
    private Runnable locationCheckRunnable;
    private static final int LOCATION_CHECK_INTERVAL = 600 * 1000; // 60 seconds in milliseconds

    private static final int REQUEST_LOCATION_PERMISSION = 1;


    //for vibration
    private Vibrator vibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("locations");

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        // Initialize the handler and runnable
        locationCheckHandler = new Handler();
        locationCheckRunnable = new Runnable() {
            @Override
            public void run() {
                // Check if the user's location is inside any circles
                checkUserLocation();
                // Schedule the runnable to run again after LOCATION_CHECK_INTERVAL
                locationCheckHandler.postDelayed(this, LOCATION_CHECK_INTERVAL);
            }
        };

        locationCheckHandler.post(locationCheckRunnable);

        btnAlert = findViewById(R.id.sendToFirebaseButton);
        btnAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(coordinatesToSend!=null) {
                    openDialog();
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        // You can customize the map and add markers, polylines, etc. here.
// Inside the onMapReady method

        googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(@NonNull Location location) {
                // Get the user's current location
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                // Zoom to the user's location
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.5f));

                // Remove the listener to avoid unnecessary zooming
                googleMap.setOnMyLocationChangeListener(null);
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot locationSnapshot : dataSnapshot.getChildren()) {
                    MyLatLong myLatLng = locationSnapshot.getValue(MyLatLong.class);

                    // Create markers on the map for each saved location
                    if (myLatLng != null) {
                        LatLng latLng = new LatLng(myLatLng.latitude, myLatLng.longitude);
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(latLng)
                                .title(myLatLng.description)
                                .snippet(String.valueOf(myLatLng.date));
                        Marker newMarker = map.addMarker(markerOptions);
                        markers.add(newMarker);

                        CircleOptions circleOptions = new CircleOptions()
                                .center(latLng)  // Center of the circle
                                .radius(100)    // Radius in meters (adjust as needed)
                                .strokeColor(0xFFFF0000)  // Red outline color
                                .fillColor(0x44FF0000);   // Red fill color with transparency

                        Circle circle = map.addCircle(circleOptions);

                        circles.add(circle);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Database Error: " + error.getMessage());
            }
        });

        // Enable the My Location layer on the map
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
        googleMap.setMyLocationEnabled(true);

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {


                // Add a marker at the clicked location
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(description)
                        .snippet(String.valueOf(new Date()));

                coordinatesToSend = latLng; // for button click event

                // Add the new marker to the map and the list
                Marker newMarker = map.addMarker(markerOptions);
                markers.add(newMarker);
            }



        });

        // Set up a location change listener to zoom to the user's location

    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    // Send the coordinates to Firebase
    private void sendCoordinatesToFirebase(LatLng latLng, String description) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("locations");

        LatLng cord = coordinatesToSend; // Your LatLng object
        MyLatLong myLatLng = new MyLatLong(latLng.latitude, latLng.longitude, description, new Date());
        String key = databaseReference.push().getKey();

        if (key != null) {
            databaseReference.child(key).setValue(myLatLng);
        }
    }

    private void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter a description");

        // Set up the input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String description = input.getText().toString();

                if (!description.isEmpty()) {
                    // Save the coordinates and description to Firebase
                    sendCoordinatesToFirebase(coordinatesToSend, description);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    // Method to periodically check the user's location against circles
    private void checkUserLocation() {
        // Get the user's current location
        if (googleMap != null) {
            googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(@NonNull Location location) {
                    LatLng userCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                    // Loop through the circles and check if the user is inside any of them
                    for (Circle circle : circles) {
                        if (isLocationInsideCircle(userCurrentLocation, circle)) {
                            showAlert("Alert", "You are inside a circle.");
                        }
                    }
                }
            });
        }
    }


    // Helper method to find the circle associated with a marker
    private Circle findCircleForMarker(Marker marker) {
        // Loop through the circles and find the one associated with the marker
        for (Circle circle : circles) {
            LatLng circleCenter = circle.getCenter();
            if (circleCenter.equals(marker.getPosition())) {
                return circle;
            }
        }
        return null;
    }

    // Define a method to check if a location (LatLng) is inside a circle
    private boolean isLocationInsideCircle(LatLng location, Circle circle) {
        double radius = circle.getRadius();
        LatLng center = circle.getCenter();

        double distance = SphericalUtil.computeDistanceBetween(location, center);

        return distance <= radius;
    }


    private void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
    // Handle other lifecycle methods like onPause, onDestroy, etc.
}