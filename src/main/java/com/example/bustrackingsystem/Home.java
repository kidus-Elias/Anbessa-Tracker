package com.example.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Home extends AppCompatActivity {
    DocumentReference usercheck;
    Button citizen;
    Button driver;
    ProgressBar h;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkAndRequestPermissions();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        citizen=findViewById(R.id.btn_c);
        driver=findViewById(R.id.btn_d);
        h=findViewById(R.id.hbar);

        citizen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent cintent=new Intent(Home.this,citizenHome.class);

                startActivity(cintent);
            }
        });

        driver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                h.setVisibility(View.VISIBLE);
                FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
                if(
                        currentUser != null

                ){

                    usercheck= FirebaseFirestore.getInstance().collection("drivers").document(currentUser.getPhoneNumber());
                    usercheck.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {


                                    Intent home=new Intent(Home.this,DriverHome.class);
                                    h.setVisibility(View.INVISIBLE);
                                    Toast.makeText(Home.this,"Login Successful",Toast.LENGTH_SHORT).show();
                                    startActivity(home);

                                } else {
                                    h.setVisibility(View.INVISIBLE);
                                    Toast.makeText(Home.this,"your account does not exist",Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                h.setVisibility(View.INVISIBLE);
                                Toast.makeText(Home.this,"Problem with autologin please Try Again",Toast.LENGTH_SHORT).show();

                            }
                        }
                    });}
                else
                {
                    Intent login=new Intent(Home.this,Login.class);
                    h.setVisibility(View.INVISIBLE);
                    startActivity(login);
                }

            }
        });
    }
    public boolean checkAndRequestPermissions() {
        int internet = ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET);
        int loc = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int loc2 = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (internet != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.INTERNET);
        }
        if (loc != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (loc2 != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions((Activity) this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), 1);
            return false;
        }
        return true;
    }
}