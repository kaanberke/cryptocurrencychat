package com.project.cryptocurrencychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterUser extends AppCompatActivity implements View.OnClickListener{

    private TextView banner;
    private Button btnRegister;
    private EditText edtTxtName, edtTxtAge, edtTxtEmail, edtTxtPassword;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        mAuth = FirebaseAuth.getInstance();

        banner = (TextView) findViewById(R.id.banner);
        banner.setOnClickListener(this);

        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(this);

        edtTxtAge = (EditText) findViewById(R.id.age);
        edtTxtEmail = (EditText) findViewById(R.id.email);
        edtTxtName = (EditText) findViewById(R.id.name);
        edtTxtPassword = (EditText) findViewById(R.id.password);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.banner:
                startActivity(new Intent(this, MainActivity.class));
            case R.id.btnRegister:
                registerUser();
                break;


        }
    }

    private void registerUser(){
        String email = edtTxtEmail.getText().toString().trim();
        String password = edtTxtPassword.getText().toString().trim();
        String name = edtTxtName.getText().toString().trim();
        String age = edtTxtAge.getText().toString().trim();

        if (name.isEmpty()){
            edtTxtName.setError("Full name is required!");
            edtTxtName.requestFocus();
            return;
        }
        if (age.isEmpty()){
            edtTxtAge.setError("Age is required!");
            edtTxtAge.requestFocus();
            return;
        }
        if (Integer.parseInt(age) > 120 || 16 > Integer.parseInt(age)){
            edtTxtAge.setError("Age cannot be greater than 120 or less than 16!");
            edtTxtAge.requestFocus();
            return;
        }
        if (email.isEmpty()){
            edtTxtEmail.setError("Email is required!");
            edtTxtEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            edtTxtEmail.setError("Please provice a valid email!");
            edtTxtEmail.requestFocus();
            return;
        }
        if (password.isEmpty()){
            edtTxtPassword.setError("Password is required!");
            edtTxtPassword.requestFocus();
            return;
        }
        if (password.length()<6){
            edtTxtPassword.setError("Password is too short! 6 characters min.");
            edtTxtPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            User user = new User(name, age, email);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                        user.sendEmailVerification();
                                        Toast.makeText(
                                                RegisterUser.this,
                                                "User has been registered successfully.",
                                                Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                        finish();
                                    }
                                    else {
                                        Toast.makeText(
                                                RegisterUser.this,
                                                "Failed to register! Try again!",
                                                Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                        else {
                            Toast.makeText(
                                    RegisterUser.this,
                                    "Failed to register!",
                                    Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });

    }
}