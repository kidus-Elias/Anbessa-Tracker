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
import android.widget.TextView;
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
import com.example.bustrackingsystem.databinding.ActivityTrackBusBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class TrackBus extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    LocationManager locationManager;
    Button selectb;
    Boolean isNetworkEnabled,isGpsEnabled;
    TextView bnum,dist,eta,t;
    private GoogleMap mMap;
    private ActivityTrackBusBinding binding;
    DocumentReference busRef;
    Driver d;
    Marker driver;
    Marker citizen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        binding = ActivityTrackBusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Bundle getid=getIntent().getExtras();
        busRef =FirebaseFirestore.getInstance().collection("drivers").document(getid.getString("id"));
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.tmap);
        mapFragment.getMapAsync(this);
            bnum=findViewById(R.id.bnum);
            dist=findViewById(R.id.dist);
            t=findViewById(R.id.transitracker);
            selectb=findViewById(R.id.btn_selectb);
            eta=findViewById(R.id.eta);
            selectb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(TrackBus.this,BusList.class));
                }
            });
        busRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {

                if (snapshot != null && snapshot.exists()) {
                   d=snapshot.toObject(Driver.class);
                    bnum.setText(d.getBusNum());
                    t.setText(d.getTransit());
                    Double z=distance(getLocation().getLatitude(),getLocation().getLongitude(),d.getLatitude(),d.getLongitude());
                    dist.setText (z.toString()+" Km" );
                    if(d.getSpeed()!=0){
                    Double g=((z * 1000)/d.getSpeed())/60;
                    eta.setText(Math.round((g*100.0)/100.0) + "mins");}
                    driver.setPosition(new LatLng(d.getLatitude(),d.getLongitude()));
                   mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(d.getLatitude(),d.getLongitude())));

                } else {
                    Toast.makeText(TrackBus.this,"driver currently Unavailable",Toast.LENGTH_SHORT).show();
                }
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        busRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        d=document.toObject(Driver.class);

                    } else {
                        System.out.println("no such doc");
                    }
                } else {
                    System.out.println("task failed");
                }
            }
        });
citizen=mMap.addMarker(new MarkerOptions().position(new LatLng(getLocation().getLatitude(),getLocation().getLongitude())));
        driver= mMap.addMarker(new MarkerOptions().position(new LatLng(getLocation().getLatitude(),getLocation().getLongitude())));
        driver.setIcon(BitmapFromVector(R.drawable.busicon));
        mMap.setMinZoomPreference(15.0f);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(getLocation().getLatitude(),getLocation().getLongitude())));

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
        citizen.setPosition(new LatLng( location.getLatitude(),location.getLongitude()));
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Problem with acquiring Location",Toast.LENGTH_SHORT).show();
        }
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

    public static double distance(double lat1,
                                  double lon2, double lat2,
                                  double lon1)
    {

        // The math module contains a function
        // named toRadians which converts from
        // degrees to radians.
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // Haversine formula
        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));
        ;

        // Radius of earth in kilometers. Use 3956
        // for miles
        double r = 6371;

        // calculate the result
        return(Math.round(c * r*100.0)/100.0);
    }
}
