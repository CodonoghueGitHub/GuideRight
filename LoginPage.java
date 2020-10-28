package com.example.guideright;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.util.Objects;

public class LoginPage extends AppCompatActivity
{

    FirebaseAuth mAuth;
    EditText uEmail;
    EditText uPass;
    Button loginButton;
    ProgressBar pb1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        mAuth = FirebaseAuth.getInstance();
        uEmail = findViewById(R.id.UserEmail);
        uPass = findViewById(R.id.UserPassword);
        loginButton = findViewById(R.id.loginBtn);
        pb1 = findViewById(R.id.progressBar2);
        pb1.setVisibility(View.INVISIBLE);

        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View v)
            {
                pb1.setVisibility(View.VISIBLE);
                String email = uEmail.getText().toString().trim();
                String pass = uPass.getText().toString().trim();

                if(TextUtils.isEmpty(email))
                {
                    pb1.setVisibility(View.INVISIBLE);
                    uEmail.setError("Email Required");
                    return;
                }
                if(TextUtils.isEmpty(pass))                                                                     //Validation of user input
                {
                    pb1.setVisibility(View.INVISIBLE);
                    uPass.setError("Password is Required");
                    return;
                }

                mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(LoginPage.this,"User Login Successful", Toast.LENGTH_SHORT).show();
                            Intent in2 =  new Intent(v.getContext(), Dashboard.class);                                           //Links the next Activity For users to Login after authorization
                            startActivity(in2);
                        }
                        else
                        {
                            pb1.setVisibility(View.INVISIBLE);
                            Toast.makeText(LoginPage.this,"Error" + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });

    }
}