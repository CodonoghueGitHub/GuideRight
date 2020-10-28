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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class SettingsPage extends AppCompatActivity {

    Spinner spin2,spin3;
    String unit;
    int landmark,unitId;
    Button saveData;
    FirebaseAuth Auth;
    FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);

        spin2 = findViewById(R.id.spinner2);
        spin3 = findViewById(R.id.spinner3);
        saveData = findViewById(R.id.Save);
        Auth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(SettingsPage.this,R.array.Preferred_LandMark, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);                                                                                 //Assign array string to dropdown lists
        spin2.setAdapter(adapter2);

        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(SettingsPage.this,R.array.Units, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spin3.setAdapter(adapter3);

        saveData.setOnClickListener(new View.OnClickListener() {
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
                        Toast.makeText(SettingsPage.this, "Details have been saved", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), Dashboard.class));
                        finish();
                    }
                });

            }
        });

        spin2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                landmark = spin2.getSelectedItemPosition();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                landmark = spin2.getSelectedItemPosition();
            }
        });

        spin3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                unit = spin3.getSelectedItem().toString();
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
                unit = spin3.getSelectedItem().toString();
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

    }
}