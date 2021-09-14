package com.example.etoemessenger;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import org.apache.commons.codec.binary.Hex;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
//import java.security.KeyPair;
//import java.security.KeyPairGenerator;
//import java.security.KeyStore;
//import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Random;
//import javax.crypto.KeyAgreement;


import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;


import static java.nio.charset.StandardCharsets.UTF_8;

//14-9-2021

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;
    boolean isSecureFlag = false;

    //Shared secret

    FirebaseUser fuser;
    DatabaseReference reference, pukReference;

    ImageButton send_btn;
    EditText send_text;

    MessageAdapter messageAdapter;
    List<Chat> mychat;

    RecyclerView recyclerView;

    Intent intent;

    SecretKey sharedAESKey;

    PublicKey remotePublicKey;

    PrivateKey myPrivateKey;

    SharedPreferences myKeyPairSharedPreferences, myAESKeys;


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

        //ανταλλαγή δημοσίων κλειδιών και δημιουργεία Shared secret με το οποίο κρυπτογραφώ και αποκρυπτογραφώ τα μηνύματα
        pukReference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        pukReference.addValueEventListener(new ValueEventListener() {
           @Override
               public void onDataChange(@NonNull DataSnapshot snapshot) {
                 User user = snapshot.getValue(User.class);

                    if (user.getPublicKey().equals("none")){

                        isSecureFlag =false;
                    }
                    else {
                            isSecureFlag=true;
                            KeyFactory factory;
                            //KeyFactory factory1 = null;
                            if (getSharedPreferences("myAESKeys",MODE_PRIVATE).getString(fuser.getUid() + "to" + userid,null)==null){
                                Log.i("Info", "Exchange Diagnostic: Executing Key agreement...");
                                String strPublicKey = user.getPublicKey();
                                Log.i("Info", "Exchange Diagnostic: his public key is "+strPublicKey);
                                myKeyPairSharedPreferences = getSharedPreferences(fuser.getUid(), MODE_PRIVATE);
                                myAESKeys = getSharedPreferences("myAESKeys", MODE_PRIVATE);
                                String strMyPrivateKey = myKeyPairSharedPreferences.getString("myPrKey", null);
                                Log.i("Info", "Exchange Diagnostic: my private key is "+strMyPrivateKey);

                                try {
                                    factory = KeyFactory.getInstance("DH", "BC");
                                    remotePublicKey = factory.generatePublic(new X509EncodedKeySpec(Base64.
                                            getDecoder().
                                            decode(strPublicKey)));//Μετατροπή του String public Key απο string σε PublicKey
                                            Log.i("Info", "Exchange Diagnostic:   his public key generated by the String is"+remotePublicKey);

                                    //factory = KeyFactory.getInstance("DH", "BC");
                                    myPrivateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(Base64.
                                            getDecoder().
                                            decode(strMyPrivateKey)));
                                            Log.i("Info", "Exchange Diagnostic:  my private key generated by the String is"+myPrivateKey);
                                    KeyAgreement ka = KeyAgreement.getInstance("DH", "BC");
                                    Log.i("Info","Exchange Diagnostic: Key Agreement instantiated");
                                    ka.init(myPrivateKey);
                                    Log.i("Info","Exchange Diagnostic: Key Agreement initialized with "+myPrivateKey);
                                    ka.doPhase(remotePublicKey, true);
                                    Log.i("Info","Exchange Diagnostic: Key Agreement finalized with "+remotePublicKey);
                                    sharedAESKey = ka.generateSecret("AES");    //δημιουργία συμμετρικού κλειδιου
                                    Log.i("Info","Exchange Diagnostic: The Shared AES Key is"+sharedAESKey);
                                    Base64.Encoder encoder = Base64.getEncoder();
                                    String strSharedAESKey = encoder.encodeToString(sharedAESKey.getEncoded());
                                    SharedPreferences.Editor editor = myAESKeys.edit();
                                    editor.putString(fuser.getUid() + "to" + userid, strSharedAESKey);
                                    editor.commit();
                                } catch (NoSuchAlgorithmException e) {
                                    e.printStackTrace();
                                } catch (NoSuchProviderException e) {
                                    e.printStackTrace();
                                } catch (InvalidKeySpecException e) {
                                    e.printStackTrace();
                                } catch (InvalidKeyException e) {
                                    e.printStackTrace();
                                } catch (IllegalStateException e){
                                    e.printStackTrace();
                                }

                            }
                            else{
                                myAESKeys = getSharedPreferences("myAESKeys", MODE_PRIVATE);
                                String strSharedAESKey = myAESKeys.getString(fuser.getUid() + "to" + userid,null);
                                Log.i("Info","Agreement already in place");
                                Log.i("Info","String Shared AES key is  "+strSharedAESKey);
                                byte[] decodedKey = Base64.getDecoder().decode(strSharedAESKey);
                                    // rebuild key using SecretKeySpec
                                sharedAESKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
                            }

                        Log.i("Info", "established Shared secret with user: "+userid+" is "+sharedAESKey);
                        }

                    readMessages(fuser.getUid(), userid, user.getImageUrl());
           }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //και αποστέλλουμε μηνύματα
        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = send_text.getText().toString();
                if(!msg.equals("")){
                    try {
                        sendMessage(fuser.getUid(), userid, msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Toast.makeText(MessageActivity.this, "message must not be empty", Toast.LENGTH_SHORT).show();
                }
                send_text.setText("");
            }
        });

        //συνδεόμαστε στη βάση δεδομένων πραγματικού χρόνου
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                username.setText(user.getUsername());
                if (user.getImageUrl().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);//διαβάζουμε το thumbnail το χρήστη
                }
                else{
                    Glide.with(MessageActivity.this).load(user.getImageUrl()).into(profile_image);
                }

                readMessages(fuser.getUid(), userid, user.getImageUrl()); //διαβάζουμε τα μηνυματα απο και προς τον χρήστη
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public static byte[] encrypt(byte[] pText, SecretKey secret, byte[] iv) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        Log.i("Info", "AES ολα καλα...");
        cipher.init(Cipher.ENCRYPT_MODE, secret, new GCMParameterSpec(128, iv));
        Log.i("Info", "AES ολα καλα...2");
        byte[] encryptedText = cipher.doFinal(pText);
        return encryptedText;

    }

    public static String decrypt(byte[] cText, SecretKey secret, byte[] iv) throws Exception {

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        Log.i("Info","Decryption Diagnostic: secret Key is "+secret);
        cipher.init(Cipher.DECRYPT_MODE, secret, new GCMParameterSpec(128, iv));
        byte[] plainText = cipher.doFinal(cText);
        return new String(plainText, UTF_8);
    }

    public static byte[] getRandomNonce(int numBytes) {
        byte[] nonce = new byte[numBytes];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    public static String getRandomString(int i)
    {

        // bind the length
        byte[] bytearray = new byte[256];
        String mystring;
        StringBuffer thebuffer;
        String theAlphaNumericS;

        new Random().nextBytes(bytearray);

        mystring = new String(bytearray, Charset.forName("UTF-8"));

        thebuffer = new StringBuffer();

        //remove all special char
        theAlphaNumericS = mystring.replaceAll("[^A-Z0-9]", "");

        //random selection
        for (int m = 0; m < theAlphaNumericS.length(); m++) {

            if (Character.isLetter(theAlphaNumericS.charAt(m)) && (i > 0) || Character.isDigit(theAlphaNumericS.charAt(m)) && (i > 0)) {
                thebuffer.append(theAlphaNumericS.charAt(m));
                i--;
            }
        }

        // the resulting string
        return thebuffer.toString();
    }

    //αποστέλλουμε μηνυμα ως hashMap με ta 'πεδία' sender, receiver και message
    private void sendMessage (String sender, String receiver, String message) throws Exception {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String iv_string, cipher_str;

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        if (isSecureFlag) {
            iv_string = getRandomString(12);//byte[] iv=getRandomNonce(12);
            byte[] cipherBytes = encrypt(message.getBytes("UTF8"),sharedAESKey, iv_string.getBytes("UTF8"));//byte[] cipherBytes = encrypt(Base64.getMimeDecoder().decode(message),sharedAESKey, iv);
            //cipher_str = cipherBytes.toString();//cipher_str =new String(Base64.getEncoder().encode(cipherBytes));
            char[] hexChars = Hex.encodeHex(cipherBytes);
            cipher_str = String.valueOf(hexChars);
            Log.i("Info", "Diagnostic: CipherText to be sent is "+cipher_str+" in its Hexadecimal Representation");
            hashMap.put("message", cipher_str);

            //iv_str = iv.toString();//iv_str = new String(Base64.getEncoder().encode(iv));
            Log.i("Info", "Diagnostic: iv byte array length is "+iv_string.getBytes("UTF8").length+" and the length of its string representation is "+iv_string.length());

            hashMap.put("iv", iv_string);
        }
        else {
            hashMap.put("message", message);
            hashMap.put("iv", "none");
        }

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
                        //chat.get
                        if(chat.getIv().equals("none")){
                            mychat.add(chat);
                        }
                        else{
                            try {

                                byte[] iv = chat.getIv().getBytes("UTF8");//byte[] iv = Base64.getDecoder().decode(chat.getIv());
                                Log.i("Info","Decryption Diagnostic: String IV is"+ chat.getIv());
                                Log.i("Info","Decryption Diagnostic: String IV length is"+ chat.getIv().length()+" and its byte representation is "+iv.length);
                                String hexCipherText = chat.getMessage();
                                byte[] cipherBytes = Hex.decodeHex(hexCipherText);
                                Log.i("Info", "Decryption Diagnostic: CipherText to be decrypted is"+chat.getMessage()+" and its byte representation is "+cipherBytes);
                                String plaintext = decrypt(cipherBytes, sharedAESKey, iv);
                                chat.setMessage(plaintext);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            mychat.add(chat);

                        }
                        //εδώ κάνω την αποκρυτπογράφηση των μηνυμάτων βάση του shared secret
                        //mychat.add(chat);
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