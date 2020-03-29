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

package com.example.tg_iar_app.Class;

public class Device {
    private String type;
    private int value;
    private int key;

    public String getDeviceType() {
        return type;
    }

    public void setDeviceType(String deviceType) {
        this.type = deviceType;
    }

    public int getDeviceValue() {
        return value;
    }

    public void setDeviceValue(int deviceValue) {
        this.value = deviceValue;
    }

    public int getDeviceKey() {
        return key;
    }

    public void setDeviceKey(int key) {
        this.key = key;
    }
}
