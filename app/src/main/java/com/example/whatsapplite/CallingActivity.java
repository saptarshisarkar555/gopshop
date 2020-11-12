package com.example.whatsapplite;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity {

    private TextView nameContact;
    private ImageView profileImage;
    private ImageView cancelCallButton, acceptCallButton;
    private String receiverUserId = "", receiverUserImage = "", receiverUserName = "";
    private String senderUserId = "", senderUserImage = "", senderUserName = "", checker = "";
    private String callingId = "", ringingId = "";
    private DatabaseReference usersRef;
    private boolean isCalling;



    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        isCalling = getIntent().getExtras().getBoolean("isCalling");
        if (isCalling) {
            senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        } else {
            senderUserId = getIntent().getExtras().get("visit_user_id").toString();
            receiverUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        nameContact = findViewById(R.id.username_calling);
        profileImage = findViewById(R.id.profile_image_calling_Id);
        cancelCallButton = findViewById(R.id.cancel_call_id);
        acceptCallButton = findViewById(R.id.make_call_id);

        mediaPlayer = MediaPlayer.create(this, R.raw.ringing);

        cancelCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaPlayer.stop();
                checker = "clicked";
                cancelCallingUsers();
            }
        });

        acceptCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaPlayer.stop();

                final HashMap<String, Object> callingPickUpMap = new HashMap<>();
                callingPickUpMap.put("picked", "picked");

                usersRef.child(receiverUserId)
                        .child("Ringing")
                        .updateChildren(callingPickUpMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    Intent intent = new Intent(CallingActivity.this, VideoChatActivity.class);
                                    startActivity(intent);
                                }
                            }
                        });

            }
        });

        getAndSetUserProfileInfo();
    }

    private void getAndSetUserProfileInfo() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(receiverUserId).exists()) {
                    //  receiverUserImage = dataSnapshot.child(receiverUserId).child("image").getValue().toString();
                    Object messageReceiverImageObj = getIntent().getExtras().get("visit_image");
                    if (messageReceiverImageObj != null) {
                        receiverUserImage = messageReceiverImageObj.toString();
                    } else {
                        receiverUserImage = "default_image";
                    }

                    Log.d("test", "isCalling" + isCalling);

                    receiverUserName = dataSnapshot.child(receiverUserId).child("name").getValue().toString();

                    nameContact.setText(receiverUserName);
                    Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile_image).into(profileImage);
                }

                if (dataSnapshot.child(senderUserId).exists()) {
                    senderUserImage = dataSnapshot.child(senderUserId).child("image").getValue().toString();
                    Object messageReceiverImageObj = getIntent().getExtras().get("visit_image");
                    if (messageReceiverImageObj != null) {
                        senderUserImage = messageReceiverImageObj.toString();
                    } else {
                        senderUserImage = "default_image";
                    }

                    senderUserName = dataSnapshot.child(senderUserId).child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        mediaPlayer.start();

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!checker.equals("clicked") && !dataSnapshot.hasChild("Calling") &&
                        !dataSnapshot.hasChild("Ringing")) {
                    final HashMap<String, String> callingInfo = new HashMap<>();
                    callingInfo.put("calling", receiverUserId);



                    usersRef.child(senderUserId)
                            .child("Calling")
                            .setValue(callingInfo)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
//                                    if (task.isSuccessful()) {
                                        final HashMap<String, String> ringingInfo = new HashMap<>();
                                        ringingInfo.put("ringing", senderUserId);

                                        Log.d("test", receiverUserId);

                                        usersRef.child(receiverUserId)
                                                .child("Ringing")
                                                .setValue(ringingInfo);
//                                    }
                                }
                            });
                    usersRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Fixed accept call button visibility here
                            if (dataSnapshot.child(receiverUserId).hasChild("Ringing") && !isCalling) {
                                acceptCallButton.setVisibility(View.VISIBLE);
                            }

                            if (dataSnapshot.child(receiverUserId).child("Ringing").hasChild("picked")) {
                                mediaPlayer.stop();

                                Intent intent = new Intent(CallingActivity.this, VideoChatActivity.class);
                                startActivity(intent);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        super.onStart();
    }

    private void cancelCallingUsers() {

        //-------------------------------------------from sender side
        usersRef.child(senderUserId)
                .child("Calling")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("calling")) {
                            callingId = dataSnapshot.child("calling").getValue().toString();

                            usersRef.child(callingId)
                                    .child("Ringing")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                usersRef.child(senderUserId)
                                                        .child("Calling")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                startActivity(new Intent(CallingActivity.this, MainActivity.class));
                                                                finish();
                                                            }
                                                        });
                                            }

                                        }
                                    });

                        } else {
                            startActivity(new Intent(CallingActivity.this, MainActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        //-------------------------------------------from receiver side
        usersRef.child(senderUserId)
                .child("Ringing")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("ringing")) {
                            ringingId = dataSnapshot.child("ringing").getValue().toString();

                            usersRef.child(ringingId)
                                    .child("Calling")
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                usersRef.child(senderUserId)
                                                        .child("Ringing")
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                startActivity(new Intent(CallingActivity.this, MainActivity.class));
                                                                finish();
                                                            }
                                                        });
                                            }

                                        }
                                    });

                        } else {
                            startActivity(new Intent(CallingActivity.this, MainActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }
}