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

package com.example.tg_iar_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {
    private List<Device> deviceList;
    private List<Device> devices;
    private Context context;
    private DatabaseReference fbRef;
    private Device allDevice;

    private String key;
    private FirebaseUser fbUser;
    private FirebaseAuth auth;

    public DeviceAdapter(List<Device> d, Context c) {
        deviceList = d;
        context = c;
    }

    @NonNull
    @Override
    public DeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_devices, parent, false);

        return new DeviceAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final DeviceAdapter.ViewHolder holder, int position) {
        final Device item = deviceList.get(position);
        devices = new ArrayList<>();

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
                                    fbRef = FirebaseDatabase.getInstance().getReference();
                                    fbRef.child("Devices").child(key).orderByChild("key").equalTo(item.getDeviceKey())
                                            .addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    devices.clear();

                                                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                                                        allDevice = postSnapshot.getValue(Device.class);

                                                        devices.add(allDevice);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });


            holder.tvDeviceType.setText(item.getDeviceType());
            if(item.getDeviceKey() == 1){
                if(item.getDeviceValue() == 0){
                    holder.tvDeviceValue.setText("Tomada desligada.");
                }else if(item.getDeviceValue() == 1) {
                    holder.tvDeviceValue.setText("Tomada ligada.");
                }
            }else if(item.getDeviceKey() == 2){
                if(item.getDeviceValue() == 0){
                    holder.tvDeviceValue.setText("Nenhum gás identificado.");
                }else if(item.getDeviceValue() == 1) {
                    holder.tvDeviceValue.setText("Presença de gás identificada.");
                }
            }
            holder.linearLayoutDevices.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.d("TESTE", "Elemento " + item.getDeviceKey() + " clicado.");

                    int i = item.getDeviceKey();
                    if(i == 1) {
                        fbRef = FirebaseDatabase.getInstance().getReference().child("Devices").child(key)
                                .child("powerplug").child("value");
                        fbRef.setValue(item.getDeviceValue() == 0 ? 1 : 0);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        protected TextView tvDeviceType;
        protected TextView tvDeviceValue;
        protected LinearLayout linearLayoutDevices;

        public ViewHolder(View itemView) {
            super(itemView);

            tvDeviceType = itemView.findViewById(R.id.tvDeviceType);
            tvDeviceValue = itemView.findViewById(R.id.tvDeviceValue);
            linearLayoutDevices = itemView.findViewById(R.id.llListDevices);
        }
    }
}
