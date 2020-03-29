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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference fbRef;
    private User user;
    private EditText edtSignupUsername;
    private EditText edtSignupName;
    private EditText edtSignupEmail;
    private EditText edtSignupPassword1;
    private EditText edtSignupPassword2;
    private Button btnSignupSingup;
    private Button btnSignupCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        database = FirebaseDatabase.getInstance();

        edtSignupEmail = findViewById(R.id.edtSignupEmail);
        edtSignupUsername = findViewById(R.id.edtSignupUsername);
        edtSignupPassword1 = findViewById(R.id.edtSignupPassword1);
        edtSignupPassword2 = findViewById(R.id.edtSignupPassword2);
        edtSignupName = findViewById(R.id.edtSignupName);
        btnSignupCancel = findViewById(R.id.btnSignupCancel);
        btnSignupSingup = findViewById(R.id.btnSignupSingup);

        btnSignupSingup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtSignupPassword1.getText().toString().equals(edtSignupPassword2.getText().toString())) {
                    user = new User();

                    user.setEmail(edtSignupEmail.getText().toString());
                    user.setName(edtSignupName.getText().toString());
                    user.setPassword(edtSignupPassword1.getText().toString());
                    user.setUserName(edtSignupUsername.getText().toString());

                    if (auth == null) auth = FirebaseAuth.getInstance();

                    auth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                            .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                try {
                                    fbRef = database.getReference().child("Users").child(user.getUserName());
                                    fbRef.setValue(user);

                                    auth.signOut();
                                    goToMainScreen();
                                    finish();
                                    Toast.makeText(SignupActivity.this, "Usuário cadastrado com sucesso", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(SignupActivity.this, "Erro ao gravar o usuário.", Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            } else {
                                String errorExcep = "";

                                try {
                                    throw task.getException();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    errorExcep = "Digite uma com no mínimo 8 caracteres com letras e números.";
                                    e.printStackTrace();
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    errorExcep = "Email inválido.";
                                    e.printStackTrace();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    errorExcep = "Email já cadastrado.";
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    errorExcep = "Erro ao efetuar cadastro. Tente novamente.";
                                    e.printStackTrace();
                                }

                                Toast.makeText(SignupActivity.this, errorExcep, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(SignupActivity.this, "As senhas não se correspondem.", Toast.LENGTH_LONG).show();
                }
            }
        });

        btnSignupCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMainScreen();
            }
        });
    }

    private void goToMainScreen() {
        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
