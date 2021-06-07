package com.example.etoemessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


//η αρχικη "σελίδα" της εφαρμογής
public class StartActivity extends AppCompatActivity {

    Button login, register;

    FirebaseUser firebaseUser;

    @Override
    protected void onStart() {
        super.onStart();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //Αν ο χρήστης είναι ηδη εγγεγραμένος η εφαρμογή μεταβαίνει απο την τρεχουσα "StartActivity" στην "MainActivity"
        if(firebaseUser != null){
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        login = findViewById(R.id.login);
        register = findViewById(R.id.register);


        //η μεθοδος setOnClickListener "ακουει" αν πατάμε κάποιο κουμπί

        login.setOnClickListener(new View.OnClickListener() {       //αν πατήσουμε το κουμπί login μεταφερόμαστε
            @Override                                               //στην LoginActivity για κάνουμε είσοδο με email και password
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, LoginActivity.class));
            }
        });

        register.setOnClickListener(new View.OnClickListener() {    //αν πατήσουμε το κουμπί register μεταφερόμαστε
            @Override                                               //στην RegisterActivity για να εγγραφούμε στην υπηρεσία
            public void onClick(View v) {
                startActivity(new Intent(StartActivity.this, RegisterActivity.class));
            }
        });
    }
}