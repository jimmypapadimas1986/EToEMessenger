package com.example.etoemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


//23-7-2021
//Aυτό το Activity καλείται από το LoginActivity και το οποίο αποστέλλει email στον λογαριασμό που
//ορίζουμε στον TextView

public class PasswordResetActivity extends AppCompatActivity {

     EditText reset_password_email;
     Button btn_reset;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        Toolbar toolbar = findViewById(R.id.toolbar);           //με αυτές τις τεσσερις γραμμές κωδικα στηνουμε την πάνω οριζόντια μπάρα τις εφαρμογής
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Password reset");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        reset_password_email = findViewById(R.id.reset_password_email);
        btn_reset = findViewById(R.id.btn_reset);

        firebaseAuth = FirebaseAuth.getInstance();

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = reset_password_email.getText().toString();
                if(email.equals("")){
                    Toast.makeText(PasswordResetActivity.this, "An email address is required!",Toast.LENGTH_LONG).show();
                }
                else {
                    firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(PasswordResetActivity.this, "Password Reset email has been sent", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(PasswordResetActivity.this, LoginActivity.class));
                            }
                            else {
                                String error = task.getException().getMessage();
                                Toast.makeText(PasswordResetActivity.this, error, Toast.LENGTH_LONG);
                            }
                        }
                    });
                }

            }
        });
    }
}