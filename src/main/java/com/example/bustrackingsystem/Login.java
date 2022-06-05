package com.example.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {

    EditText pnum;
    Button btnLogin;
    Button btnSignup;
    FirebaseAuth pauth;
    String otp;
    String mVerificationId;
    Boolean autologinflag=false;
    ProgressBar waitCode;
    DocumentReference usercheck;
    Bundle homeBundle;
    TextView s;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //if user is already authenticated let him in

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

         homeBundle=getIntent().getExtras();
        btnLogin=findViewById(R.id.btn_login);
        btnSignup=findViewById(R.id.btn_signup);
        pauth=FirebaseAuth.getInstance();
        pnum=findViewById(R.id.pnum);
        waitCode=findViewById(R.id.bar);
        s=findViewById(R.id.s);






        // if not show the interface for login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(TextUtils.isEmpty(pnum.getText().toString()))
                {
                    Toast.makeText(getApplicationContext(),"Phone Number Must be given",Toast.LENGTH_SHORT).show();
                }
                else
                {   String number="+251"+pnum.getText().toString();

                        usercheck=FirebaseFirestore.getInstance().collection("drivers").document(number);
                        usercheck.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {

                                        waitCode.setVisibility(View.VISIBLE);
                                        sendotp(number);
                                    } else {

                                        Toast.makeText(Login.this,"You don't have an account please sign up!",Toast.LENGTH_SHORT).show();
                                    }
                                } else {

                                    Toast.makeText(Login.this,"Please Try Again",Toast.LENGTH_SHORT).show();

                                }
                            }
                        });


                }
            }
        });
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent signupIntent=new Intent(Login.this,Signup.class);
                startActivity(signupIntent);
            }
        });
    }
   private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential)
        {
            otp=credential.getSmsCode();
          if(otp!=null)
          {
              autologinflag=true;
              waitCode.setVisibility(View.INVISIBLE);
              verifyIntent();
          }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            waitCode.setVisibility(View.INVISIBLE);
        Toast.makeText(Login.this,"Request Failed",Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(verificationId,token);
           mVerificationId=verificationId;
           Toast.makeText(getApplicationContext(),"code sent!",Toast.LENGTH_SHORT).show();
           waitCode.setVisibility(View.INVISIBLE);
           verifyIntent();
        }
    };

    public void sendotp(String n){
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(pauth)
                        .setPhoneNumber(n)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);


    }
    public void verifyIntent(){
        Bundle v=new Bundle();
        v.putString("otp",otp);
        v.putString("id",mVerificationId);
        v.putBoolean("auto",autologinflag);
        Intent verifyI=new Intent(Login.this,VerifyOtp.class);
        verifyI.putExtras(v);
        startActivity(verifyI);
    }

}