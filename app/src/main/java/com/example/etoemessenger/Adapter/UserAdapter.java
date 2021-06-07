package com.example.etoemessenger.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.etoemessenger.MessageActivity;
import com.example.etoemessenger.Model.User;
import com.example.etoemessenger.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//19-5-2021

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {     //Κλάσση UserAdapter επεκτείνει την υποκλάσση Adapter της κλάσσης RecyclerView
    //η παρούσα κλάσση μας εμφανίζει του χρήστες της υπηρεσίας με δυναμικό (RecyclerView) 'clickable'(Adapter) τροπο

    /*Το πεδίο myContext είναι Διεπαφή με global πληροφορίες σχετικά με ένα περιβάλλον εφαρμογής.
    Αυτή είναι μια αφηρημένη κλάσση που επιτρέπει την πρόσβαση σε πόρους και κλάσσης, συγκεκριμένες στην εφαρμογή μας εφαρμογές,
    καθώς και σε αναβαθμίσεις για λειτουργίες σε επίπεδο εφαρμογής, όπως εκκίνηση των Activitiew, μετάδοση και λήψη των intent*/
    private Context myContext;
    private List<User> myUsers;     //Λίστα χρηστών

    public UserAdapter(Context myContext, List<User> myUsers){  //μεθοδος δημιουργός
        this.myUsers = myUsers;
        this.myContext = myContext;
    }



    /*https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#onCreateViewHolder(android.view.ViewGroup,%20int)
    Η onCreateViewHolder είναι αφηρημένη μέθοδος που καλείτε όταν το RecyclerView χρειάζεται ένα νέο
    RecyclerView.ViewHolder του δεδομένου τύπου για να αντιπροσωπεύει ένα στοιχείο.

    Επιστρέφει νέο ViewHolder αντικείμενο θα πρέπει να είναι κατασκευασμένο με μια νέα προβολή που
    μπορεί να αντιπροσωπεύει τα στοιχεία του δεδομένου τύπου. Στην περίπτωσή μας δημιουργούμε μια
    νέα προβολή κάνοντας inflate το layout αρχείο user_item.

    Το νέο ViewHolder θα χρησιμοποιηθεί για την εμφάνιση στοιχείων του προσαρμογέα χρησιμοποιώντας
    την μέθοδο onBindViewHolder (ViewHolder, int, List) που υλοποιούμε παρακάτω.*/
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(myContext).inflate(R.layout.user_item, parent, false);

        return new UserAdapter.ViewHolder(view);
    }

    /*https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#onBindViewHolder(VH,%20int,%20java.util.List%3Cjava.lang.Object%3E)
    Η onBindViewHolder είναι αφηρημένη μέθοδος που καλείτε από το RecyclerView για εμφάνιση των
    δεδομένων στην καθορισμένη θέση. Αυτή η μέθοδος ενημερώσει τα περιεχόμενα του
    RecyclerView.ViewHolder.itemView ώστε να αντικατοπτρίζει το στοιχείο στη δεδομένη θέση.
    */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = myUsers.get(position);
        holder.username.setText(user.getUsername());
        if (user.getImageUrl().equals("default")){
            holder.profile_Image.setImageResource(R.mipmap.ic_launcher);
        }
        else {
            Glide.with(myContext).load(user.getImageUrl()).into(holder.profile_Image);
        }

        //19-5-2021
        holder.itemView.setOnClickListener(new View.OnClickListener() {         //πατάμε σε εναν χρήστη-item της λίστας χρηστών
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(myContext, MessageActivity.class);   //και μεταφερόμαστε στην εναρξη συνομιλίας
                intent.putExtra("userid", user.getId());                 //περνάμε επίσης όρισμα το id του χρήστη
                myContext.startActivity(intent);                                //με τον οποίο θέλουμε να συνομιλίσουμε
            }
        });

    }

    @Override
    public int getItemCount() {
        return myUsers.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_Image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_Image = itemView.findViewById(R.id.profile_image);
        }
    }
}
