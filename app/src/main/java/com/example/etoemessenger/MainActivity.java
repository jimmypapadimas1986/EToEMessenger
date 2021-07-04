package com.example.etoemessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.etoemessenger.Fragments.ChatsFragment;
import com.example.etoemessenger.Fragments.ProfileFragment;
import com.example.etoemessenger.Fragments.UsersFragment;
import com.google.android.material.appbar.AppBarLayout;
import com.example.etoemessenger.Model.User;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // δηλώνοντας τα παρακάτω αντικείμενα σαν properties της κλάσσης MainActivity
    // τα κάνουμε ορατά σε όλη την κλάσση
    CircleImageView profile_image;      // αντικείμενο με το οποίο χειριζόμαστε την "εικονα προφιλ"
    TextView username;                  // "Στατικό" πλαίσιο κειμένου που εμφανιζε τον τρέχοντα χρήστη

    FirebaseUser firebaseUser;          // αντικείμενο "Firebase χρήστη"
    DatabaseReference reference;        // αναφορά στη Firebase βάση δεδομένων

    @Override
    protected void onCreate(Bundle savedInstanceState) {        // η onCreate() μεθοδος καλείται παντα με το που "τρέχει"
        super.onCreate(savedInstanceState);                     // το αντίστοιχο activity
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
                                                                // με την findViewById() συνδέουμε τα στοιχεια interface με τον κωδικα
        profile_image = findViewById(R.id.profile_image);       // εικόνα profil
        username = findViewById(R.id.username);                 // ονομα χρήστη

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();                                           // ο τρέχον χρηστησ
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());  // η αναφορά στη βαση δεδομένων της εφαρμογής μας για τον τρέχοντα χρήστη

        reference.addValueEventListener(new ValueEventListener() {      //ο συγκεκριμένος Listener "ακούει" για αλλαγές
            @Override                                                   //τιμών στη βάση μας
            public void onDataChange(@NonNull DataSnapshot snapshot) {  //αν γίνει κάποια αλλαγή παίρνει το στιγμιότυπο και
                User user = snapshot.getValue(User.class);              //το αποθηκεύει σε αντικείμενο της κλάσσης User που έχουμε ορίσει
                Log.i("checkMsg", "user is "+user.getUsername()+" with Image "+user.getImageUrl() +" and UID "+ user.getId());
                //if(user == null){
                //    Log.i("INFO", "user is null");
                //}
                username.setText(user.getUsername());
                if (user.getImageUrl()==null) {
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(MainActivity.this).load(user.getImageUrl()).into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);

        ViewPagerAdapter viewPagerAdapter =new ViewPagerAdapter(getSupportFragmentManager());
        /*Ορίζουμε τον κύριο χώρο της σελιδας με την ViewPagerAdapter και την χωρίζουμε
         σε δύο "καρτέλες "Chats" και "Users" που ονομάζονται 'Fragments'.
         Το Chat Fragments μας δείχνει τις συνομηλίες και το Users Fragment μας δείχνει του χρήστες*/
        viewPagerAdapter.addFragments(new ChatsFragment(), "Chats");
        viewPagerAdapter.addFragments(new UsersFragment(), "Users");
        viewPagerAdapter.addFragments(new ProfileFragment(), "Profile");


        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case  R.id.logout:                          //Logout κουμπί
                FirebaseAuth.getInstance().signOut();   //
                startActivity(new Intent(MainActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                return true;
        }

        return false;
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {   //οριζουμε τη κλάσση ViewPagerAdapter
                                                            //που επεκτείνει την FragmentPagerAdapter
                                                            //και διαχειρίζεται τα Fragments
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm){
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();

        }
        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        public void addFragments(Fragment fragment, String title) {
            fragments.add(fragment);
            titles.add(title);
        }

        //

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    /*μέθοδος που 'γράφει' στη βάση και συγκεκριμένα στον πίνακα ΄Users΄ και στην εγγραφή του
    τοπικού χρήστη την τέχουσα κατάστασή του */
    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }


    //4-7-2021
    //με τις επόμενες μεθόδου αλλάζουμε την κατάσταση του τοπικού χρήστη
    @Override
    protected void onResume() {     //οταν κάνουμε resume θέτουμε το status online
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {     //όταν βγένουμε από την εφαρμογή θετουμε το status offline
        super.onPause();
        status("offline");
    }
}