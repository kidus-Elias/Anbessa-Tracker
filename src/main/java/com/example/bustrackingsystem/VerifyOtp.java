package com.example.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class VerifyOtp extends AppCompatActivity {

         EditText votp;
         Button btnVerify;
         Bundle v;
         ProgressBar vbar;
    FirebaseAuth mAuth=FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);
        votp=findViewById(R.id.verifyotp);
        btnVerify=findViewById(R.id.btn_verify);
        vbar=findViewById(R.id.vbar);
         v= getIntent().getExtras();
         //check if its an auto login and proceed accordingly
         if(v.getBoolean("auto")==true){
             vbar.setVisibility(View.VISIBLE);
             PhoneAuthCredential credential = PhoneAuthProvider.getCredential(v.getString("id"),v.getString("otp"));
             signInWithPhoneAuthCredential(credential);
         }
         else //for a non auto login
         {  btnVerify.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if(TextUtils.isEmpty(votp.getText().toString()))
                 {
                     Toast.makeText(getApplicationContext(),"Please Enter THe Otp value",Toast.LENGTH_SHORT).show();
                 }
                 else
                 {
                     vbar.setVisibility(View.VISIBLE);
                     PhoneAuthCredential credential = PhoneAuthProvider.getCredential(v.getString("id"),votp.getText().toString());
                     signInWithPhoneAuthCredential(credential);
                 }
             }
         });}

    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(getApplicationContext(),"Login Successful",Toast.LENGTH_SHORT).show();
                                Intent dHome=new Intent(VerifyOtp.this,DriverHome.class);
                                dHome.putExtras(v);
                                startActivity(dHome);
                            vbar.setVisibility(View.INVISIBLE);
                            // Update UI
                        } else {
                            // Sign in failed, display a message and update the UI
                            vbar.setVisibility(View.INVISIBLE);
                            Toast.makeText(getApplicationContext(),"Login Failed",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
}}