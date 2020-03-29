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


public class User {

    private String userName;
    private String name;
    private String email;
    private String password;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
