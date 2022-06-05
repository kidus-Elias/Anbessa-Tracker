package com.example.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class BusList extends AppCompatActivity {
    ListView buslist;

    ArrayAdapter<Driver> adapter;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference busesRef=db.collection("drivers");
    ArrayList <Driver> drivers=new ArrayList<>();

    String TAG="BusList";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_list);
        buslist=findViewById(R.id.busList);

         adapter=new ArrayAdapter<Driver>(this, android.R.layout.simple_list_item_1,new ArrayList<Driver>());
          Query query = busesRef.whereEqualTo("shareLocation", true);

         query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Driver d=document.toObject(Driver.class);
                        drivers.add(d);
                    }
                    adapter.addAll(drivers);;

                } else {
               Toast.makeText(getApplicationContext(),"Trouble connecting to the internet",Toast.LENGTH_SHORT).show();
                }

            }
        });

        buslist.setAdapter(adapter);
        buslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Bundle sendbusid=new Bundle();
                System.out.println(drivers.get(i).getBusNum());
            sendbusid.putString("id",drivers.get(i).getpNum());
            Intent track=new Intent(BusList.this,TrackBus.class);
            track.putExtras(sendbusid);
            startActivity(track);
            }
        });
    }
}