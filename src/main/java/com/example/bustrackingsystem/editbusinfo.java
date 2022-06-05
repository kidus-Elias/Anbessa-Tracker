package com.example.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class editbusinfo extends AppCompatActivity {

    DocumentReference busref= FirebaseFirestore.getInstance().collection("drivers").document(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
    EditText bnum,transit;
    Button update,delete;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editbusinfo);
        bnum=findViewById(R.id.ubNum);
        transit=findViewById(R.id.utransit);
        update=findViewById(R.id.btn_update);
        delete=findViewById(R.id.btn_delete);
        busref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    DocumentSnapshot bus=task.getResult();
                    if(bus.exists())
                    {
                        Driver d=bus.toObject(Driver.class);
                        bnum.setText(d.getBusNum());
                        transit.setText(d.getTransit());
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"Account not found",Toast.LENGTH_SHORT).show();
                    }
                }
                    else
                {
                    Toast.makeText(getApplicationContext(),"Unable to connect to the Internet!",Toast.LENGTH_SHORT).show();
                }
            }
        });
       update.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               if(TextUtils.isEmpty(bnum.getText())||TextUtils.isEmpty(transit.getText()))
               {
                   Toast.makeText(getApplicationContext(),"All fields must be provided",Toast.LENGTH_SHORT).show();
               }
               else
               {
                   busref.update("busNum",bnum.getText().toString());
                   busref.update("transit",transit.getText().toString());
                   Toast.makeText(getApplicationContext(),"Update successful",Toast.LENGTH_SHORT).show();

               }
           }
       });
       delete.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               busref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                       if(task.isSuccessful())
                       {   FirebaseAuth.getInstance().signOut();

                           Toast.makeText(getApplicationContext(),"Delete successful",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(editbusinfo.this,Home.class));
                       }
                   }
               });
           }
       });
    }
}