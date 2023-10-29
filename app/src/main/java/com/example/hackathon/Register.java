package com.example.hackathon;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class Register extends AppCompatActivity {
    TextInputEditText editTextEmail, editTextPassword, editTextPasswordConfirm,
            editTextCounty, editTextName;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(Register.this, MainActivity.class);
            Register.this.startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email_register);
        editTextName = findViewById(R.id.name_register);
        editTextCounty = findViewById(R.id.county_register);
        editTextPassword = findViewById(R.id.password_register);
        editTextPasswordConfirm = findViewById(R.id.password_confirm_register);
        progressBar = findViewById(R.id.progressBar);

        buttonReg = findViewById(R.id.btn_register);

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                buttonReg.setVisibility(View.GONE);
                String email, password, name, county, passwordConfirm;
                email = String.valueOf(editTextEmail.getText());
                name = String.valueOf(editTextName.getText());
                county = String.valueOf(editTextCounty.getText());
                password = String.valueOf(editTextPassword.getText());
                passwordConfirm = String.valueOf(editTextPasswordConfirm.getText());

                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(name) || TextUtils.isEmpty(county)
                        || TextUtils.isEmpty(password) || TextUtils.isEmpty(passwordConfirm)) {
                    Toast.makeText(Register.this, "Please complete all the fields! ", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    buttonReg.setVisibility(View.VISIBLE);
                } else if (!password.equals(passwordConfirm)) {
                    Toast.makeText(Register.this, "Passwords don't match!", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    buttonReg.setVisibility(View.VISIBLE);
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    buttonReg.setVisibility(View.VISIBLE);
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Toast.makeText(Register.this, "User created.",
                                                Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), Login.class);
                                        Register.this.startActivity(intent);
                                        finish();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(Register.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}