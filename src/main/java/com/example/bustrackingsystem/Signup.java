package com.example.bustrackingsystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class Signup extends AppCompatActivity {
    EditText bNum,transit,pNum;
    ProgressBar s;
    Button signup;
    String otp;
    Boolean autologinflag=false;
    Bundle loginBundle;
    String mVerificationId;
    FirebaseAuth pauth;
    CollectionReference usercheck=FirebaseFirestore.getInstance().collection("drivers");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        bNum=findViewById(R.id.bNum);
        transit=findViewById(R.id.transit);
        pNum=findViewById(R.id.pNums);
        loginBundle=getIntent().getExtras();
        signup=findViewById(R.id.btn_signup);
        pauth=FirebaseAuth.getInstance();
        s=findViewById(R.id.sbar);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                creatAcc();
            }
        });
    }
    public void creatAcc(){
        if(TextUtils.isEmpty(bNum.getText().toString())||TextUtils.isEmpty(transit.getText().toString())||TextUtils.isEmpty(pNum.getText().toString()))
        {
            Toast.makeText(this,"All fields are required for a sign up",Toast.LENGTH_LONG).show();
        }
        else
        {

            String number="+251"+pNum.getText().toString();
              s.setVisibility(View.VISIBLE);
            if(pauth.getCurrentUser()==null){
                usercheck.document(number).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        DocumentSnapshot driver=task.getResult();
                        if(driver.exists())
                        {        s.setVisibility(View.INVISIBLE);
                            Toast.makeText(Signup.this,"Account Already exists please Login",Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Signup.this,Login.class));
                        }
                        else
                        {
                            sendotp(number);
                        }
                    }

                }
            });
            }


        }
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential)
        {
            otp=credential.getSmsCode();
            if(otp!=null)
            {
                autologinflag=true;
                s.setVisibility(View.INVISIBLE);
                verifyIntent();
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            s.setVisibility(View.INVISIBLE);
            Toast.makeText(Signup.this,"Request Failed",Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(verificationId,token);
            mVerificationId=verificationId;
            Toast.makeText(getApplicationContext(),"code sent!",Toast.LENGTH_SHORT).show();
           s.setVisibility(View.INVISIBLE);
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
    void verifyIntent(){
        Bundle v=new Bundle();
        v.putString("pnum","+251"+pNum.getText().toString());
        v.putString("otp",otp);
        v.putString("id",mVerificationId);
        v.putBoolean("auto",autologinflag);
        v.putString("bNum",bNum.getText().toString());
        v.putString("transit",transit.getText().toString());
        Intent verifyd=new Intent(Signup.this,VerifyOtp.class);
        verifyd.putExtras(v);
        startActivity(verifyd);
    }
}