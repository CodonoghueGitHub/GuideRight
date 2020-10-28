package com.example.guideright;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity
{
    EditText uEmail,uPassword,CPass;
    TextView RegisterLnk;
    Button RegisterBtn;
    ProgressBar lBar;
    FirebaseAuth Auth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uEmail = findViewById(R.id.UserEmail);
        uPassword = findViewById(R.id.UserPassword);
        CPass = findViewById(R.id.ConfirmPass);
        lBar = findViewById(R.id.progressBar);
        Auth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        RegisterBtn = findViewById(R.id.Register);
        RegisterLnk = findViewById(R.id.alreadyRegistered);

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
           Toast.makeText(MainActivity.this,"Permission given", Toast.LENGTH_SHORT).show();
        }
        else
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
        }



        RegisterLnk.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                lBar.setVisibility(View.VISIBLE);
                Intent in = new Intent(MainActivity.this,LoginPage.class);
                startActivity(in);
            }
        });

        RegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                lBar.setVisibility(View.VISIBLE);
                final String email = uEmail.getText().toString().trim();
                String password = uPassword.getText().toString().trim();
                String Pass2 = CPass.getText().toString().trim();

                if(!TextUtils.equals(password,Pass2))
                {
                    CPass.setError("Passwords Do Not Match!");
                    lBar.setVisibility(View.INVISIBLE);                                                     //Start of user input validation
                    return;
                }
                if(TextUtils.isEmpty(email))
                {
                    uEmail.setError("Email is Required");
                    lBar.setVisibility(View.INVISIBLE);
                    return;
                }
                if(TextUtils.isEmpty(password))
                {
                    uPassword.setError("Password is Required");
                    lBar.setVisibility(View.INVISIBLE);
                    return;
                }
                if(TextUtils.isEmpty(Pass2))
                {
                    uEmail.setError("Please repeat password");
                    lBar.setVisibility(View.INVISIBLE);
                    return;
                }

                Auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            String uId;
                            uId = Objects.requireNonNull(Auth.getCurrentUser()).getUid();
                            DocumentReference docR = fStore.collection("users").document(uId);
                            Map<String, Object> user = new HashMap<>();                                                  //Storing User Account information
                            user.put("Email", email);
                            docR.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(MainActivity.this, "Profile has been Created", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), User_Preferences.class));
                                }
                            });
                        } else {
                            Toast.makeText(MainActivity.this, "Error" + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                            lBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if(requestCode == 1)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(MainActivity.this,"Permission Granted", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(MainActivity.this,"Please grant permissions", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }
    }
}