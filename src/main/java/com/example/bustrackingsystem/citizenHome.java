package com.example.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;

import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.bustrackingsystem.databinding.ActivityCitizenHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class citizenHome extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    protected LocationManager locationManager;
    Bundle cInfo;

    private GoogleMap mMap;
    private ActivityCitizenHomeBinding binding;
    Location location; // location
    double latitude; // latitude
    double longitude;
    Boolean  isNetworkEnabled;
    Boolean isGpsEnabled;
    Marker citizen;
    Button next;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityCitizenHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        next=findViewById(R.id.btn_select);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent go=new Intent(citizenHome.this,BusList.class);
                startActivity(go);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        db.collection("drivers").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                loadBuses(mMap);
            }
        });

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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
       // mMap.setTrafficEnabled(true);

       //this sets the minimum zoom level
        mMap.setMinZoomPreference(13.0f);
         location=getLocation();
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        loadBuses(mMap);
        // Add a marker in Sydney and move the camera
        LatLng current = new LatLng(latitude, longitude);
       citizen= mMap.addMarker(new MarkerOptions().position(current).title("You Are Here"));
       citizen.setIcon(BitmapFromVector(R.drawable.citizen));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));

    }

    @SuppressLint("MissingPermission")
    private Location getLocation() {
        Location currentLocation=null;
        //this creats a location manager with access to the device location service
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //check if gps and network are enabled
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            isGpsEnabled =locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isNetworkEnabled) {
                //check the network permission
                try {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity)this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        3000, 0, this);
                if (locationManager != null) {
                    currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                }

            }
            else if(isGpsEnabled){
                try {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity)this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        3000, 0, this);
                if (locationManager != null) {
                    currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                }
            }
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),"Problem with acquiring Location",Toast.LENGTH_SHORT).show();
        }
    return  currentLocation;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        if(location!=null)
        {
            savelocation(location);

        }
        else
        {
            Toast.makeText(getApplicationContext(),"Problem with acquiring Location",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    private void savelocation(Location location) {
        citizen.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==101){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                location=getLocation();
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
                // Add a marker in Sydney and move the camera
                LatLng current = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(current).title("My location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
            }
            else
            {
                Toast.makeText(this,"permission is required for these services",Toast.LENGTH_SHORT).show();
            }
        }
    }
    //method to add all bus icons on citizen home
    public void loadBuses(GoogleMap maps){
        maps.clear();
        citizen= mMap.addMarker(new MarkerOptions().position(new LatLng(getLocation().getLatitude(),getLocation().getLongitude())).title("You Are Here"));
        citizen.setIcon(BitmapFromVector(R.drawable.citizen));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(getLocation().getLatitude(),getLocation().getLongitude())));
        db.collection("drivers").whereEqualTo("shareLocation",true).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful())
                {
                    for(QueryDocumentSnapshot bus:task.getResult())
                    {
                        Driver d=bus.toObject(Driver.class);
                        maps.addMarker(new MarkerOptions().position(new LatLng(d.getLatitude(),d.getLongitude())).title(d.getBusNum()+" " +d.getTransit())).setIcon(BitmapFromVector(R.drawable.busicon));
                    }
                }
                else
                {
                    Toast.makeText(citizenHome.this,"Cannot fetch Bus information check your connection",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private BitmapDescriptor BitmapFromVector(int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(getApplicationContext(), vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth()+10, vectorDrawable.getIntrinsicHeight()+10);

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
}