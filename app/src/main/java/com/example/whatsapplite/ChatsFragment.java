package com.example.whatsapplite;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsapplite.notifications.Token;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View privateChatView;
    private RecyclerView chatsList;
    private String currentUserID;
    private LinearLayoutManager mLayoutManager;


    private DatabaseReference chatsRef, usersRef;
    private FirebaseAuth mAuth;
    // String mUID;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatView = inflater.inflate(R.layout.fragment_chats, container, false);

        mAuth = FirebaseAuth.getInstance();
        //currentUserID= mAuth.getCurrentUser().getUid();

        FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
        if (mFirebaseUser != null) {
            currentUserID = mFirebaseUser.getUid(); //Do what you need to do with the id
        } else {
            currentUserID = "lzYPB1A11wb8amh6paquffsXKfz2";
        }

        chatsList = privateChatView.findViewById(R.id.chats_list);
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);

        chatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatsList.setLayoutManager(mLayoutManager);

        privateChatView.findViewById(R.id.addBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<Contact> contactList = new ArrayList<>();

                FirebaseDatabase.getInstance().getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Iterable<DataSnapshot> snapshotIterable = snapshot.getChildren();
                        Iterator<DataSnapshot> iterator = snapshotIterable.iterator();

                        while (iterator.hasNext()) {
                            DataSnapshot dataSnapshot = iterator.next();
                            Contact contact = new Contact();
                            String id = dataSnapshot.getKey();
                            DataSnapshot nameSnapShot = dataSnapshot.child("name");
                            if (nameSnapShot.getValue() == null) {
                                continue;
                            }

                            contact.setUid(id);
                            contact.setName(nameSnapShot.getValue().toString());
                            contactList.add(contact);
                        }

                        Intent intent = new Intent(getContext(), SendMsgToMultipleActivity.class);
                        intent.putExtra("contactList", (Serializable) contactList);
                        intent.putExtra("senderID", currentUserID);
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        return privateChatView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(chatsRef, Contact.class)
                        .build();

//        Log.d("message", "onStart: "+ options);

        FirebaseRecyclerAdapter<Contact, ChatsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contact, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position, @NonNull Contact model) {
                        final String usersIDs = getRef(position).getKey();
                        final String[] setImage = {"default_image"};

                        usersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChild("image")) {
                                        setImage[0] = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(setImage[0]).into(holder.profileImage);
                                    }

                                    final String setName = dataSnapshot.child("name").getValue().toString();
                                    final String setStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(setName);
                                    holder.userStatus.setText("Last seen: " + "\n" + "Date" + " Time");

                                    if (dataSnapshot.child("userState").hasChild("state")) {
                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                        if (state.equals("online")) {
                                            holder.userStatus.setText("online");

                                           /* //for notification
                                            mUID = currentUser.getUid();
                                            SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
                                            SharedPreferences.Editor editor = sp.edit();
                                            editor.putString("Current_USERID", mUID);
                                            editor.apply();*/
                                        } else if (state.equals("offline")) {
                                            holder.userStatus.setText("Last seen: " + date + " " + time);
                                        }
                                    } else {
                                        holder.userStatus.setText("offline");
                                    }

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", usersIDs);
                                            chatIntent.putExtra("visit_user_name", setName);
                                            chatIntent.putExtra("visit_image", setImage[0]);
                                            startActivity(chatIntent);
                                        }
                                    });

                                    //delete chat item
                                    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                        @Override
                                        public boolean onLongClick(View v) {
                                            CharSequence options[] = new CharSequence[]
                                                    {
                                                            "Delete " + setName + "'s chat?",
                                                            "Cancel",

                                                    };
                                            AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                                            builder.setTitle("Delete Chat?");

                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    if (which == 0) {
                                                        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                                                        rootRef.child("Messages").child(currentUserID).child(usersIDs).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(getContext(), setName + "'s all data deleted!!!", Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });

                                            builder.show();

                                            return true;
                                        }
                                    });

                                    //end
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }


                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);

                        return new ChatsViewHolder(view);
                    }
                };
        chatsList.setAdapter(adapter);
        adapter.startListening();

        updateToken(FirebaseInstanceId.getInstance().getToken());
    }

    public void updateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        ref.child(currentUserID).setValue(mToken);
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder {

        CircleImageView profileImage;
        TextView userName, userStatus;


        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage = itemView.findViewById(R.id.users_profile_image);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);

        }
    }
}
