package com.example.etoemessenger.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.etoemessenger.Adapter.UserAdapter;
import com.example.etoemessenger.Model.Chat;
import com.example.etoemessenger.Model.User;
import com.example.etoemessenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

//1-6-2021
//fragment που περιέχει τις συνομιλίες που έχω κάνει ανα χρήστη
public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;

    FirebaseUser fuser;     //τοπικός χρήστης
    DatabaseReference reference;    //Firebase βάση δεδομένων

    private  List<String> ContactsList;    //λίστα απομακρισμένων χρηστών με τους οποίου έχω επικοινωνία
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //Authentication
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        ContactsList = new ArrayList<>();

        //Παίρνουμε  ένα snapshot της βάσης "Chats", από το οποίο τραβάμε τα μηνύματα που μας αφορούν
        //και συμπληρώνουμε την String λίστα  usersList των επαφών μας
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ContactsList.clear();

                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);

                        //Log.i("Info", "logged in user is "+fuser.getUid());
                        //Log.i("Info", "chat object is "+chat.getReceiver()+ " "+chat.getSender()+" "+chat.getMessage());


                    if (chat.getSender().equals(fuser.getUid())){       //μηνύματα που έχω στείλει
                        ContactsList.add(chat.getReceiver());              //που τα έχω στείλει
                    }
                    if (chat.getReceiver().equals(fuser.getUid())){     //μηνύματα που έχω λάβει
                        ContactsList.add(chat.getSender());                //απο πού τα έχω λάβει
                    }
                }

                readChats(); //αναγνωση μηνυμάτων
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        return view;
    }

    private void readChats(){
        mUsers = new ArrayList<>();     //προσωρινή LOCAL  User λιστα επαφών που προορίζεται για την
        //δυναμική προβολή recyclerView

        //Παίρνουμε  ένα snapshot της βάσης "Users"

        reference =FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();

                for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                    User user = dataSnapshot.getValue(User.class);

                    //προσθέτουμε ένα User αντικείμενο στην λιστα mUsers
                    for(String id : ContactsList){      //οποιοδήποτε στοιχείο της String λίστας επαφών "ταιριάζει"
                        if (user.getId().equals(id)){   //με το id οποιουδήποτε χρήστη της User λίστας όλων των χρηστών
                            if (mUsers.size() != 0){    //και η User λίστα δεν είναι κενή
                                for (User userl : mUsers){
                                    if(!user.getId().equals(userl.getId())){ //και δεν βρίσκεται ήδη στην mUsers λίστα επαφών
                                        mUsers.add(user);
                                    }
                                }
                            }
                            else {
                                mUsers.add(user);
                            }
                        }
                    }
                }

                userAdapter =new UserAdapter(getContext(), mUsers, true);
                recyclerView.setAdapter(userAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}