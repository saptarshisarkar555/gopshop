package com.example.whatsapplite;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdaptor;


    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private String currentUserID;

    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();

        RootRef= FirebaseDatabase.getInstance().getReference();

        Toolbar mToolbar= findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("GopShop");

        myViewPager =(ViewPager) findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdaptor=new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdaptor);

        myTabLayout =(TabLayout) findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser=mAuth.getCurrentUser();

        if(currentUser==null)
        {
            SendUserToLoginActivity();
        }
        else{
            updateUserStatus("online");
            VerifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser=mAuth.getCurrentUser();

        if(currentUser!= null){
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser!= null){
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance() {
        String currentUserID=mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("name").exists()){
                   // Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else{
                    SendUserToSettingsActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId()==R.id.main_settings_option){
            SendUserToSettingsActivity();
        }
        if(item.getItemId()==R.id.main_create_group_option){
            RequestNewGroup();
        }
        if(item.getItemId()==R.id.main_find_friend_option){
            SendUserToFindFriendsActivity();
        }
        if(item.getItemId()==R.id.main_logout_option){
            updateUserStatus("offline");
            mAuth.signOut();
            SendUserToLoginActivity();
        }

        return true;

    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder =new AlertDialog.Builder(MainActivity.this,R.style.AlertDiolog);
        builder.setTitle("Enter Group Name :");

        final EditText groupNameField= new EditText(MainActivity.this);
        groupNameField.setHint("Saptarshi's Cafe");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName= groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please write Group Name..", Toast.LENGTH_SHORT).show();
                }
                else{
                    CreateNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void CreateNewGroup(final String groupName) {
        RootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, groupName +" group is Created Successfully...", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent =new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);

    }

    private void SendUserToSettingsActivity() {
        Intent settingIntent =new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingIntent);
    }

    private void SendUserToFindFriendsActivity() {
        Intent findFriendsIntent =new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findFriendsIntent);

    }

    private void updateUserStatus(String state)
    {
        String saveCurrentTime , saveCurrentDate;

        Calendar calendar=Calendar.getInstance();

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calendar.getTime());

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate=currentDate.format(calendar.getTime());

        HashMap<String, Object>onlineStatusMap =new HashMap<>();
        onlineStatusMap.put("time", saveCurrentTime);
        onlineStatusMap.put("date",saveCurrentDate);
        onlineStatusMap.put("state", state);

        currentUserID=mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserID).child("userState")
                .updateChildren(onlineStatusMap);
    }
}
