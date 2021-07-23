package com.example.etoemessenger.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.etoemessenger.Adapter.UserAdapter;
import com.example.etoemessenger.Model.User;
import com.example.etoemessenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

//fragment χρηστών
//19-5-2021
//12-7-2021 αναζήτηση απομακρυσμένων χρηστών της υπηρεσίας (case insensitive??  )
public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;  //δυναμική λίστα
    private UserAdapter userAdapter;    //προσαρμογέας
    private List<User> mUsers;

    EditText searchUserByName;

    //Η μέθοδος onCreateView καλείτε αυτόματα με το που εμφανίζουμε - μεταβαίνουμε στο παρων Fragment
    //και επιστρέφει

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users, container, false);  //κάνουμε inflate το layout xml αρχείο fragment_users.xml

        recyclerView = view.findViewById(R.id.reycler_view); //'παίρνουμε το στοιχείο recycler_view'
        recyclerView.setHasFixedSize(true);                  //και το προσαρμόζουμε
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUsers = new ArrayList<>();     //δημιουργούμε μια νέα κενή λίστα

        readUsers();                    //την οποία γεμίζουμε με τους εγγεγραμμένους FirebaseUsers

        searchUserByName = view.findViewById(R.id.search_user_by_name);  //αναζητούμε χρήστη
        searchUserByName.addTextChangedListener(new TextWatcher() {     //όταν γράφουμε στο πλαίσιο αναζήτητσης
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               search_User(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    private void search_User(String s) {        //12-7-2021 αναζήτηση απομακρισμένων χρηστών

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();    //αυθεντικοποιούμε τον τοπικό χρήστη
        Query user_query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username")
                .startAt(s).endAt(s+"\uf8ff");          //
        user_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!searchUserByName.getText().toString().equals("")) {
                    mUsers.clear();
                    for (DataSnapshot i : snapshot.getChildren()) {
                        User user = i.getValue(User.class);

                        assert user != null;
                        assert firebaseUser != null;
                        if (!user.getId().equalsIgnoreCase(firebaseUser.getUid())) {
                            mUsers.add(user);
                        }
                    }

                    userAdapter = new UserAdapter(getContext(), mUsers, false);
                    recyclerView.setAdapter(userAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void readUsers() {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();    //βρισκουμε των τρέχοντα εγγεγραμμένο χρήστη
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users"); //συνδεόμαστε στην βάση χρηστών

        reference.addValueEventListener(new ValueEventListener() {          //'ακούμε' για αλλαγές στις τιμές της βάσης χρηστών
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {      //αν υπάρξουν αλλαγές επιστρέφεται ένα αμετάβλητο snapshot του ανωτέρω reference
                mUsers.clear(); //καθαρίζουμε την 'κενή' λιστα από σκουπίδια
                for(DataSnapshot dataSnapshot : snapshot.getChildren() ){   //γεμίζουμε την 'κενή' λίστα με τα 'παιδιά' του snapshot
                    User user = dataSnapshot.getValue(User.class);

                    /*if(user == null && firebaseUser == null){
                        Log.i("Debuggar", "Both local and remote users are null");
                    }
                    else if(user != null && firebaseUser == null){
                        Log.i("Debuggar", "remote user is "+user.getId().toString()+" while local user is null");
                    }
                    else if(user == null && firebaseUser != null){
                        Log.i("Debuggar", "Local user is "+firebaseUser.getUid().toString()+" while remote is null");
                    }
                    else{
                        Log.i("Debuggar", "Local user is "+firebaseUser.getUid().toString()+" and ");
                        Log.i("Debuggar", "remote user is "+ user.getId().toString());
                    }*/

                    assert user != null;
                    assert firebaseUser != null;

                    if(!user.getId().equals(firebaseUser.getUid())){        //από την λίστα εξερούμε τον τρέχοντα χρήστη
                       mUsers.add(user);
                    }
                }

                userAdapter = new UserAdapter(getContext(), mUsers, false);        //προσαρμόζουμε την λίστα χρηστών
                recyclerView.setAdapter(userAdapter);                       //στο πεδίο recyclerView της παρούσας κλάσσης
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}

/* ***ΠΑΡΑΤΗΡΗΣΗ***
* Τόσο η μέθοδος onDataChange(@NonNull DataSnapshot snapshot) όσο και η μέθοδος
* onCancelled(@NonNull DatabaseError error) δημιουργούντε αυτόματα, σαν ορίσματα, εγκλεισμένες σε
* άγκριστρα, με το που δίνουμε όρισμα new ValueEventListener() στην μέθοδο addValueEventListener()
* το reference αντικειμένου*/