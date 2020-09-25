package com.example.whatsapplite.notifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.whatsapplite.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull final RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        SharedPreferences sp = getSharedPreferences("SP_USER", MODE_PRIVATE);
        final String savedCurrentUser = sp.getString("Current_USERID", "None");

        final String sent = remoteMessage.getData().get("sent");
        final String user = remoteMessage.getData().get("user");
        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        final String[] profilePicture = {null};
        final String[] name = {null};

        DatabaseReference userReference = FirebaseDatabase.getInstance()
                .getReference()
                .child("Users")
                .child(user);
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.hasChild("image")) {
                        profilePicture[0] = snapshot.child("image").getValue().toString();
                    }
                    name[0] = snapshot.child("name").getValue().toString();

                    if (fUser != null && sent.equals(fUser.getUid())) {
                        if (!savedCurrentUser.equals(user)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                sendOAndAboveNotification(remoteMessage, name[0], profilePicture[0]);
                            } else {
                                sendNormalNotification(remoteMessage, name[0], profilePicture[0]);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNormalNotification(RemoteMessage remoteMessage, String name, String profilePicture) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        RemoteMessage.Notification notification = remoteMessage.getNotification();
        assert user != null;
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatActivity.class);

        intent.putExtra("visit_user_id", user);
        intent.putExtra("visit_user_name", name);
        intent.putExtra("visit_image", profilePicture);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent PIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
                .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defSoundUri)
                .setContentIntent(PIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if (i > 0) {
            j = i;
        }
        notificationManager.notify(j, builder.build());
    }

    private void sendOAndAboveNotification(RemoteMessage remoteMessage, String name, String profilePicture) {
        String user = remoteMessage.getData().get("user");
        String icon = remoteMessage.getData().get("icon");
        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        assert user != null;
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = new Intent(this, ChatActivity.class);

        intent.putExtra("visit_user_id", user);
        intent.putExtra("visit_user_name", name);
        intent.putExtra("visit_image", profilePicture);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent PIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);

        Uri defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        Notification.Builder builder = notification1.getNotification(title, body, PIntent, defSoundUri, icon);
        int j = 0;
        if (i > 0) {
            j = i;
        }
        notification1.getManager().notify(j, builder.build());

    }
}
