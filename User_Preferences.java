package com.example.guideright;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.rpc.context.AttributeContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class User_Preferences extends AppCompatActivity
{
    Spinner spin,spin1;
    Button nxt;
    String unit;
    String landmark;
    int unitId;
    FirebaseAuth Auth;
    FirebaseFirestore fStore;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user__preferences);
        spin = findViewById(R.id.spinner);
        spin1 = findViewById(R.id.spinner1);
        nxt = findViewById(R.id.NextBtn);
        Auth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.Preferred_LandMark, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);                                                                                 //Assign array string to dropdown lists
        spin.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,R.array.Units, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin1.setAdapter(adapter1);

        spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                landmark = spin.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                    landmark = spin.getSelectedItem().toString();
            }
        });

        spin1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                unit = spin1.getSelectedItem().toString();
                if(unit.equals("Kilometers"))
                {
                    unitId = 0;
                }
                else
                {
                    unitId =1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                unit = spin1.getSelectedItem().toString();
                if(unit.equals("Kilometers"))
                {
                    unitId = 0;
                }
                else
                {
                    unitId =1;
                }
            }
        });



        nxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                String uid;
                uid = Objects.requireNonNull(Auth.getCurrentUser()).getUid();

                DocumentReference docR = fStore.collection("users").document(uid);
                Map<String,Object> userData = new HashMap<>();
                userData.put("Preferred LandMark",landmark);
                userData.put("Unit",unitId);
                docR.update(userData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        Toast.makeText(User_Preferences.this, "Details have been saved", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), LoginPage.class));
                        finish();
                    }
                });


            }
        });


    }
}