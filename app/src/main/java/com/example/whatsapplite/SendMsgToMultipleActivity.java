package com.example.whatsapplite;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.whatsapplite.notifications.APIService;
import com.example.whatsapplite.notifications.Client;
import com.example.whatsapplite.notifications.Data;
import com.example.whatsapplite.notifications.Response;
import com.example.whatsapplite.notifications.Sender;
import com.example.whatsapplite.notifications.Token;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.pchmn.materialchips.ChipsInput;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;

public class SendMsgToMultipleActivity extends AppCompatActivity {

    private List<Contact> contactList;
    private String messageSenderID;
    private DatabaseReference rootRef;
    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_msg_to_multiple);
//        getActionBar().setTitle("Send Message");

        contactList = (List<Contact>) getIntent().getSerializableExtra("contactList");
        messageSenderID = getIntent().getStringExtra("senderID");

        rootRef = FirebaseDatabase.getInstance().getReference();
        apiService = Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        for (Contact contact : contactList) {
            Log.d("SEND", contact.getUid());
        }
        final ChipsInput chipsInput = findViewById(R.id.chips_input);
        chipsInput.setFilterableList(contactList);

        final EditText messageEditor = findViewById(R.id.messageEditText);
        findViewById(R.id.sendBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageEditor.getText().toString();
                List<Contact> selectedContacts = (List<Contact>) chipsInput.getSelectedChipList();
                for (Contact contact : selectedContacts) {
                    sendMessage(contact.getUid(), message);
                }

                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }

    private void sendMessage(final String messageReceiverID, final String message) {
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "first write your message...", Toast.LENGTH_SHORT).show();
        } else {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessaageKeyRef = rootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiverID).push();

            String messagePushID = userMessaageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", message);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("isseen", false);
         /*   messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);*/

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);

            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Message Send Successfully...", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                    }

                }
            });

            String msg = message;
            DatabaseReference database = FirebaseDatabase.getInstance().getReference("Users").child(messageSenderID);
            database.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Messages user = snapshot.getValue(Messages.class);
                    sendNotification(messageReceiverID, user.getName(), message);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        }
    }

    private void sendNotification(final String messageReceiverID, final String name, final String message) {
        DatabaseReference allTokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = allTokens.orderByKey().equalTo(messageReceiverID);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Token token = ds.getValue(Token.class);
                    Data data = new Data(messageSenderID, name + ": " + message, "New Message", messageReceiverID, R.drawable.applogo);
                    Sender sender = new Sender(data, token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
//                                    Toast.makeText(ChatActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}