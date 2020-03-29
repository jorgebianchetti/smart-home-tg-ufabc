/************************************************************************************/
/* Trabalho de Graduação do curso Engenharia de Instrumentação, Automação e Robótica
/* Universidade Federal do ABC
/*
/* Aplicativo Android para comunicação com banco de dados Firebase
/* para controle de dispositivos "smart"
/*
/* Autor: Jorge Bianchetti
/* Data:  01/2020
/*
/* Código: https://github.com/jorgebianchetti/tg-iar-smart-home
/************************************************************************************/

package com.example.tg_iar_app.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.tg_iar_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddDeviceActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference fbRef;
    private FirebaseDatabase database;
    private FirebaseUser fbUser;
    private Button btnAddDevicePowerPlug;
    private Button btnAddDeviceAddGasSensor;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        fbUser = auth.getCurrentUser();

        btnAddDevicePowerPlug = findViewById(R.id.btnAddDevicePowerPlug);
        btnAddDeviceAddGasSensor = findViewById(R.id.btnAddDeviceAddGasSensor);

        btnAddDevicePowerPlug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fbUser != null){
                    fbRef = database.getReference();
                    fbRef.child("Users").orderByChild("email").equalTo(fbUser.getEmail())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                                key = postSnapshot.child("userName").getValue().toString();

                                if(key != null){
                                    fbRef = database.getReference().child("Devices").child(key).child("powerplug").child("value");
                                    fbRef.setValue(0);
                                    fbRef = database.getReference().child("Devices").child(key).child("powerplug").child("type");
                                    fbRef.setValue("Tomada Inteligente");
                                    fbRef = database.getReference().child("Devices").child(key).child("powerplug").child("key");
                                    fbRef.setValue(1);
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    Intent intentPowerPlug = new Intent(AddDeviceActivity.this, WelcomeActivity.class);
                    startActivity(intentPowerPlug);
                    finish();
                }else{
                    Toast.makeText(AddDeviceActivity.this, "Não foi possível adicionar o dispositivo.", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnAddDeviceAddGasSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fbUser != null){
                    fbRef = database.getReference();
                    fbRef.child("Users").orderByChild("email").equalTo(fbUser.getEmail())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                                        key = postSnapshot.child("userName").getValue().toString();

                                        if(key != null){
                                            fbRef = database.getReference().child("Devices").child(key).child("gassensor").child("value");
                                            fbRef.setValue(0);
                                            fbRef = database.getReference().child("Devices").child(key).child("gassensor").child("type");
                                            fbRef.setValue("Sensor de Gás");
                                            fbRef = database.getReference().child("Devices").child(key).child("gassensor").child("key");
                                            fbRef.setValue(2);
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                    Intent intentGasSensor = new Intent(AddDeviceActivity.this, WelcomeActivity.class);
                    startActivity(intentGasSensor);
                    finish();
                }else{
                    Toast.makeText(AddDeviceActivity.this, "Não foi possível adicionar o dispositivo.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
