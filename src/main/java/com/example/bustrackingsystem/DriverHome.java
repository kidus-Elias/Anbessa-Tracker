package com.example.bustrackingsystem;

import androidx.annotation.NonNull;
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
import android.widget.ImageButton;
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
import com.example.bustrackingsystem.databinding.ActivityDriverHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;



public class DriverHome extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    LocationManager locationManager;
    ImageButton logout,share,stop;
    Button editbus;
    Boolean isNetworkEnabled,isGpsEnabled;
    Bundle dInfo;
    Driver d;
    String userid=FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    Double latitude,longitude;
    Double plat,plng;
    FirebaseFirestore db=FirebaseFirestore.getInstance();
    DocumentReference usersRef;
   // driverLocation location=new driverLocation(9.0300,40.575);
    private GoogleMap mMap;

    private ActivityDriverHomeBinding binding;
    Marker marker;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityDriverHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //logout
        logout=findViewById(R.id.dLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        //share location
        share=findViewById(R.id.startsharing);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharelocation();
            }
        });
        //stop sharing location
        stop=findViewById(R.id.stopsharing);

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopsharing();
            }
        });
        //edit
        editbus=findViewById(R.id.edit);
        editbus.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(DriverHome.this,editbusinfo.class));
                    }
                }
        );

//this block is used to recognize a new or existing user and update their information accordingly
        dInfo=getIntent().getExtras();
        usersRef = db.collection("drivers").document(userid);


            usersRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful())
                    {
                        DocumentSnapshot driver= task.getResult();
                        if(driver.exists())
                        {

                            usersRef.update("longitude", getLocation().getLongitude());
                            usersRef.update("latitude", getLocation().getLatitude());

                        }
                        else
                        {

                            d = new Driver(dInfo.getString("pnum"), dInfo.getString("bNum").toString(), dInfo.getString("transit").toString(), getLocation().getLatitude(), getLocation().getLongitude(),true,getLocation().getSpeed());
                            db.collection("drivers").document(d.getpNum()).set(d);

                        }
                    }
                    else
                    {
                        Toast.makeText(DriverHome.this,"failed to fetch login information please check your internet connection",Toast.LENGTH_SHORT).show();

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
        plat=getLocation().getLatitude();
        plng=getLocation().getLongitude();
        marker= mMap.addMarker(new MarkerOptions().position(new LatLng(getLocation().getLatitude(),getLocation().getLongitude())));
        marker.setIcon(BitmapFromVector(R.drawable.busicon));
        mMap.setMinZoomPreference(15.0f);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng( getLocation().getLatitude(),getLocation().getLongitude())));
        // Add a marker in Sydney and move the camera
        sharelocation();
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

    @SuppressLint("MissingPermission")
    private Location getLocation() {
        Location currentLocation=null;
        //this creats a location manager with access to the device location service
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            //check if gps and network are enabled
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            isGpsEnabled =locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGpsEnabled) {
                //check the network permission

                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            3000, 0, this);

                    if (locationManager != null) {
                        currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    }


            }
            else if(isNetworkEnabled){
                try {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity)this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        3000,0 , this);
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
            savelocation(location.getLatitude(),location.getLongitude());
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Problem with acquiring Location",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getApplicationContext(),"Problem with acquiring Location",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getApplicationContext(),"Trying to acquire location",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Toast.makeText(getApplicationContext(),"Location Status changed",Toast.LENGTH_SHORT).show();
    }
    private void savelocation(Double lat,Double lng) {
     double speed=(distance(lat,lng,plat,plng)*1000)/3;
        usersRef.update("latitude",lat);
        usersRef.update("longitude",lng);
       usersRef.update("speed",speed);
        marker.setPosition(new LatLng(lat, lng));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
        plat=lat;
        plng=lng;
    }
    //logout method
    public void logout(){
        FirebaseAuth status=FirebaseAuth.getInstance();
        status.signOut();
        stopsharing();
        startActivity(new Intent(DriverHome.this,Home.class));
    }
    //manually start location sharing
   @SuppressLint("MissingPermission")
   public void sharelocation(){
      Location l=getLocation();
       usersRef.update("latitude",l.getLatitude());
       usersRef.update("longitude",l.getLongitude());
       usersRef.update("shareLocation",true);
       marker.setPosition(new LatLng(l.getLatitude(), l.getLongitude()));
       mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(l.getLatitude(), l.getLongitude())));
       share.setEnabled(false);
       stop.setEnabled(true);
       Toast.makeText(this,"Location sharing is On",Toast.LENGTH_LONG).show();
   }
   // manually stop sharing location
   @SuppressLint("MissingPermission")
   public void stopsharing(){
       if(locationManager != null){
           locationManager.removeUpdates(DriverHome.this);
           usersRef.update("shareLocation",false);
           Toast.makeText(this,"Location sharing is off",Toast.LENGTH_LONG).show();
           stop.setEnabled(false);
           share.setEnabled(true);
       }
   }



    //when you get permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==101){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Location location=getLocation();
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