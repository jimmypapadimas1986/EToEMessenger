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
import com.example.etoemessenger.Model.Chat;
import com.example.etoemessenger.Model.User;
import com.example.etoemessenger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


//22-5-2021
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {     //Κλάσση UserAdapter επεκτείνει την υποκλάσση Adapter της κλάσσης RecyclerView
    //η παρούσα κλάσση μας εμφανίζει του χρήστες της υπηρεσίας με δυναμικό (RecyclerView) 'clickable'(Adapter) τροπο

    /*Το πεδίο myContext είναι Διεπαφή με global πληροφορίες σχετικά με ένα περιβάλλον εφαρμογής.
    Αυτή είναι μια αφηρημένη κλάσση που επιτρέπει την πρόσβαση σε πόρους και κλάσσης, συγκεκριμένες στην εφαρμογή μας εφαρμογές,
    καθώς και σε αναβαθμίσεις για λειτουργίες σε επίπεδο εφαρμογής, όπως εκκίνηση των Activitiew, μετάδοση και λήψη των intent*/
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context myContext;
    private List<Chat> myChat;     //Λίστα μηνυμάτων
    private String imageurl;

    FirebaseUser fuser;

    public MessageAdapter(Context myContext, List<Chat> myChat, String imageurl){  //μεθοδος δημιουργός
        this.myChat = myChat;
        this.myContext = myContext;
        this.imageurl = imageurl;
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
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(myContext).inflate(R.layout.chat_item_right, parent, false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(myContext).inflate(R.layout.chat_item_left, parent, false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    /*https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.Adapter#onBindViewHolder(VH,%20int,%20java.util.List%3Cjava.lang.Object%3E)
    Η onBindViewHolder είναι αφηρημένη μέθοδος που καλείτε από το RecyclerView για εμφάνιση των
    δεδομένων στην καθορισμένη θέση. Αυτή η μέθοδος ενημερώσει τα περιεχόμενα του
    RecyclerView.ViewHolder.itemView ώστε να αντικατοπτρίζει το στοιχείο στη δεδομένη θέση.
    */
    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

        Chat chat = myChat.get(position);

        holder.showMessage.setText(chat.getMessage());

        if (imageurl.equals("default")){
            holder.profile_Image.setImageResource(R.mipmap.ic_launcher);
        }
        else {
            Glide.with(myContext).load(imageurl).into(holder.profile_Image);
        }
    }

    @Override
    public int getItemCount() {
        return myChat.size();
    }

     /*https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.ViewHolder
     H RecyclerView.ViewHolder είναι αφηρημένη κλάσση που περιγράφει μια προβολή αντικειμένων και
     μεταδεδομένα σχετικά με τη θέση του στο RecyclerView.*/
    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView showMessage;
        public ImageView profile_Image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            showMessage = itemView.findViewById(R.id.show_message);
            profile_Image = itemView.findViewById(R.id.profile_image);
        }
    }

    /*η μέθοδος getItemViewType(int position) επιστρέφει το 'είδος' του μηνύματος  στην θέση
    position και χρησιμοποιείται για να στοιχίζει τα εισερχόμενα μηνύματα αριστερά και εξερχόμενα
    δεξιά */
    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        if (myChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}
