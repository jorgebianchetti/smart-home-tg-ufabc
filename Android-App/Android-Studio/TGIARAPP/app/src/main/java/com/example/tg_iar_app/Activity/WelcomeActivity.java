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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.tg_iar_app.Adapter.DeviceAdapter;
import com.example.tg_iar_app.Class.Device;
import com.example.tg_iar_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser fbUser;
    private RecyclerView rvDevices;
    private DeviceAdapter adapter;
    private List<Device> devices;
    private DatabaseReference fbRef;
    private Device allDevices;
    private LinearLayoutManager llmAllDevices;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        rvDevices = findViewById(R.id.rvAllDevices);
        loadAllDevices();
    }

    private void loadAllDevices() {
        rvDevices.setHasFixedSize(true);

        llmAllDevices = new LinearLayoutManager(WelcomeActivity.this, LinearLayoutManager.VERTICAL, false);

        rvDevices.setLayoutManager(llmAllDevices);

        devices = new ArrayList<>();

        fbRef = FirebaseDatabase.getInstance().getReference();

        auth = FirebaseAuth.getInstance();
        fbUser = auth.getCurrentUser();
        if (fbUser != null) {
            fbRef = FirebaseDatabase.getInstance().getReference();
            fbRef.child("Users").orderByChild("email").equalTo(fbUser.getEmail())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                key = postSnapshot.child("userName").getValue().toString();

                                if (key != null) {
                                    fbRef = FirebaseDatabase.getInstance().getReference("Devices/" + key);
                                    //fbRef.child("Devices").child(key).orderByChild("key")
                                    fbRef.orderByChild("key")
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    devices.clear();
                                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                        //allDevices = postSnapshot.getValue(Device.class);
                                                        allDevices = new Device();
                                                        if(postSnapshot.child("type").getValue() != null &&
                                                                postSnapshot.child("value").getValue() != null &&
                                                                postSnapshot.child("key").getValue() != null) {
                                                            allDevices.setDeviceType(postSnapshot.child("type").getValue().toString());
                                                            allDevices.setDeviceValue(Integer.parseInt(postSnapshot.child("value").getValue().toString()));
                                                            allDevices.setDeviceKey(Integer.parseInt(postSnapshot.child("key").getValue().toString()));

                                                            //Toast.makeText(WelcomeActivity.this, String.valueOf(allDevices.getDeviceValue()), Toast.LENGTH_SHORT).show();

                                                            devices.add(allDevices);
                                                        }
                                                    }

                                                    //Toast.makeText(WelcomeActivity.this, String.valueOf(devices.size()), Toast.LENGTH_SHORT).show();
                                                    adapter = new DeviceAdapter(devices, WelcomeActivity.this);
                                                    rvDevices.setAdapter(adapter);
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });

                                    //Toast.makeText(WelcomeActivity.this, String.valueOf(devices.size()), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.welcome, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        Intent intent;
        auth = FirebaseAuth.getInstance();

        if (id == R.id.itSignout) {
            auth.signOut();

            intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.itAddDevice) {
            intent = new Intent(WelcomeActivity.this, AddDeviceActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
