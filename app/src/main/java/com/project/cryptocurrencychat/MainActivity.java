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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvRegister, tvForgotPassword;
    private EditText edtTxtEmail, edtTxtPassword;
    private Button btnLogin;

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            startActivity(new Intent(MainActivity.this, GroupChatActivity.class));
        }
        else {
            setContentView(R.layout.activity_main);

            tvRegister = (TextView) findViewById(R.id.tvRegister);
            tvRegister.setOnClickListener(this);

            btnLogin = (Button) findViewById(R.id.btnLogin);
            btnLogin.setOnClickListener(this);

            edtTxtEmail = (EditText) findViewById(R.id.email);
            edtTxtPassword = (EditText) findViewById(R.id.password);

            progressBar = (ProgressBar) findViewById(R.id.progressBar);

            mAuth = FirebaseAuth.getInstance();

            tvForgotPassword = (TextView) findViewById(R.id.tvForgotPassword);
            tvForgotPassword.setOnClickListener(this);

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvRegister:
                startActivity(new Intent(this, RegisterUser.class));
                break;

            case R.id.btnLogin:
                userLogin();
                break;

            case R.id.tvForgotPassword:
                startActivity(new Intent(this, ForgotPassword.class));
                break;
        }
    }

    private void userLogin() {
        String email = edtTxtEmail.getText().toString().trim();
        String password = edtTxtPassword.getText().toString().trim();

        if (email.isEmpty()){
            edtTxtEmail.setError("Email is required!");
            edtTxtEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            edtTxtEmail.setError("Please enter a valid email!");
            edtTxtEmail.requestFocus();
            return;
        }
        if (password.isEmpty()){
            edtTxtPassword.setError("Password is reqiured!");
            edtTxtPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if (user.isEmailVerified()){
                        startActivity(new Intent(MainActivity.this, GroupChatActivity.class));
                    }
                    else{
                        user.sendEmailVerification();
                        Toast.makeText(
                                MainActivity.this,
                                "Check your email to verify your account!",
                                Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(
                            MainActivity.this,
                            "Failed to login! Please check your credentials!",
                            Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}