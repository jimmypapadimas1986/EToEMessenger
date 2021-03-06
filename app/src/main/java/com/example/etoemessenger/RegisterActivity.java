package com.example.etoemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText username, email, password;
    Button btn_register;

    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        Toolbar toolbar = findViewById(R.id.toolbar);           //με αυτές τις τεσσερις γραμμές κωδικα στηνουμε την πάνω οριζόντια μπάρα τις εφαρμογής
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username = findViewById(R.id.username);                 //συνδέδουμε τα interface στοιχεία με τον κώδικά μας
        password = findViewById(R.id.password);
        email = findViewById(R.id.email);
        btn_register = findViewById(R.id.btn_register);

        auth = FirebaseAuth.getInstance();      //Authentication Server της εφαρμογής μας

        btn_register.setOnClickListener(new View.OnClickListener() {        //πατώντας το κουμπί register γινεται το validation
            @Override                                                       //των πεδίων εγγραφής πυ έχει δώσει ο χρήστης
            public void onClick(View v) {
                String txt_username = username.getText().toString();        //παίρνουμε τα strings τον εισόδων
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {   //αν εχουμε αφήσει πεδία κενά
                    Toast.makeText(RegisterActivity.this, "All fields Are required", Toast.LENGTH_LONG).show();//τυπωνουμε αντιστοιχο μηνυμα
                }   else if (txt_password.length()<6) {     //αν το password είναι πολύ μικρό
                    Toast.makeText(RegisterActivity.this, "password is too short", Toast.LENGTH_LONG).show();  //τυπωνουμε αντιστοιχο μηνυμα
                }   else{                                               //αν τα στοιχεία είναι επαρκή
                    register(txt_username, txt_email, txt_password);    //εγγράφουμε τον χρήστη στην υπηρεσία
                }

            }
        });
    }

    private void register(String username, String email, String password){

        auth.createUserWithEmailAndPassword(email, password)    //δημιουργούμε χρήστη με email και password
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {       //παρακολουθούμε την ολοκηρωση της διαδικασιας
                    @Override                                                       //αυτής στο κύριο νήμα
                    public void onComplete(@NonNull Task<AuthResult> task) {        //και με την ολοκλήρωση αυτής
                        if (task.isSuccessful()){                                   //αν είναι επιτυχής
                            FirebaseUser firebaseUser = auth.getCurrentUser();      //αναθέτουμε στο αντικείμεμενο firebaseUser
                                                                                    //τον χρήστη που επεστράφη με το πέρας της εγγραφής
                            assert firebaseUser !=null; // αν είναι null το γραφουμε στο stacktrace
                            String userId = firebaseUser.getUid();                  //παίρνουμε το id του χρήστη

                            reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);     //με το οποίο κάνουμε αναφορά
                                                                                                                      //στη βάση δεδομένων
                            HashMap<String, String> hashMap = new HashMap<>();  //  φτιαχνουμε ένα Key:Value ζευγάρι για τον χρήστη
                            hashMap.put("id", userId);
                            hashMap.put("username", username);
                            hashMap.put("ImageURL", "default");
                            hashMap.put("status", "offline");
                            //hashMap.put("password", password);
                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {      //το οποίο "γράφουμε στη βάση δεδομένων"
                                @Override
                                /*στην ανωτέρω setValue() μέθοδο προσθέτουμε onCompleteListener και ελέγχουμε
                                * το πέρας της διαδικασίας όπως φαίνεται παρακάτω με την μέθοδο οnComplete*/

                                public void onComplete(@NonNull Task<Void> task) {
                                     if (task.isSuccessful()){
                                         Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                         startActivity(intent);
                                         finish();
                                     }
                                }
                            });
                        }else {
                            Toast.makeText(RegisterActivity.this, "You can't register with this email or password", Toast.LENGTH_LONG).show();
                        }

                    }
                });

    }
}