package com.example.etoemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.etoemessenger.Adapter.MessageAdapter;
import com.example.etoemessenger.Model.Chat;
import com.example.etoemessenger.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//22-5-2021

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    FirebaseUser fuser;
    DatabaseReference reference;

    ImageButton send_btn;
    EditText send_text;

    MessageAdapter messageAdapter;
    List<Chat> mychat;

    RecyclerView recyclerView;

    Intent intent;


    //η μέθοδος onCreate() δημιουργείται αυτόματα και θεωρείται η 'main()' του κάθε activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);       //θετουμε το συνήθες toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MessageActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                finish();
            }
        });

        /*Στις 10 επόμενες γραμμές κώδικα 'στήνουμε' το user interface το παρόντος activity, αρχικά
        βρίσκοντας το recyclerView το αντίστοιχου layout αρχείου (στην προκειμένη περίπτωση
        activity_message.xml) και το ορίζουμε να είναι γραμμικό-κάθετο layout. Στο στοιχείο
        recycler_view θα προβάλετε η συνομιλία μεταξύ του τοπικού και απομακρισμένου χρήστη.Επίσης
        συνδλεουμε όλα τα UI elements μετον κώδικα του Activity*/
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);
        send_btn = findViewById(R.id.sendBtn);
        send_text = findViewById(R.id.sendText);


        /*παίρνουμε' το userid του απομακρισμένου χρήστη από το extra της κλήσης του παρόντος activity*/
        intent = getIntent();
        final String userid = intent.getStringExtra("userid");
        fuser = FirebaseAuth.getInstance().getCurrentUser();    //Αυθεντικοποιούμαστε

        //και αποστέλλουμε μηνύματα
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = send_text.getText().toString();
                if(!msg.equals("")){
                    sendMessage(fuser.getUid(), userid, msg);
                }
                else {
                    Toast.makeText(MessageActivity.this, "message must not be empty", Toast.LENGTH_SHORT).show();
                }
                send_text.setText("");
            }
        });

        //συνδεόμαστε στη βάση δεδομένων πραγματικού χρόνου
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        //διαβάζουμε τα μηνύματά μας αν υπάρχουν
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageUrl().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                }
                else{
                    Glide.with(MessageActivity.this).load(user.getImageUrl()).into(profile_image);
                }

                readMessages(fuser.getUid(), userid, user.getImageUrl());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    //αποστέλλουμε μηνυμα ως hashMap με ta 'πεδία' sender, receiver και message
    private void sendMessage (String sender, String receiver, String message){

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        reference.child("Chats").push().setValue(hashMap);
    }

    /*διαβάζουμε τα υπάρχοντα μηνύμα που αφορούν τον τρέχοντα και απομακρισμένο χρήστη, τα
    τοποθετούμε στο τοπικό ΑrrayList<> myChat το οποίο τροφοδοτούμε στο 'global' messageAdapter του
    παρόντος Activity για προβολή */

    private void readMessages(String myid, String userid, String imageurl){
        mychat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mychat.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                       chat.getReceiver().equals(userid) && chat.getSender().equals(myid) ){
                        mychat.add(chat);
                    }

                    messageAdapter = new MessageAdapter(MessageActivity.this, mychat, imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }
}