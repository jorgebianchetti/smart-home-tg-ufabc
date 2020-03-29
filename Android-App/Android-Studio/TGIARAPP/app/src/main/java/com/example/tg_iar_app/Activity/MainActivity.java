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
import android.widget.EditText;
import android.widget.Toast;

import com.example.tg_iar_app.Class.User;
import com.example.tg_iar_app.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText edtMainEmail;
    private EditText edtMainPassword;
    private Button btnMainSignin;
    private Button btnMainSignup;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtMainEmail = findViewById(R.id.edtMainEmail);
        edtMainPassword = findViewById(R.id.edtMainPassword);
        btnMainSignin = findViewById(R.id.btnMainSignin);
        btnMainSignup = findViewById(R.id.btnMainSignup);

        FirebaseUser fbUser = FirebaseAuth.getInstance().getCurrentUser();
        if (fbUser != null) {
            goToWelcomeScreen();
        } else {
            btnMainSignin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (edtMainEmail.getText().toString().equals("") || edtMainPassword.getText().toString().equals("")) {
                        Toast.makeText(MainActivity.this, "Preencha os campos de Email e Senha", Toast.LENGTH_LONG).show();
                    } else {
                        user = new User();

                        user.setEmail(edtMainEmail.getText().toString());
                        user.setPassword(edtMainPassword.getText().toString());

                        configFB();
                    }
                }
            });

            btnMainSignup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goToSignupScreen();
                }
            });
        }
    }

    private void configFB() {
        if (auth == null) auth = FirebaseAuth.getInstance();

        auth.signInWithEmailAndPassword(user.getEmail(), user.getPassword()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    

                    goToWelcomeScreen();

                    Toast.makeText(MainActivity.this, "Login efetuado com sucesso!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Não foi possível fazer o login. Tente Novamente.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void goToWelcomeScreen() {
        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToSignupScreen() {
        Intent intent = new Intent(MainActivity.this, SignupActivity.class);
        startActivity(intent);
        finish();
    }
}
