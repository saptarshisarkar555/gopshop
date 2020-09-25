package com.example.whatsapplite.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:Key=AAAAlpjRfTg:APA91bFL1JWyaxZwtj6f5tvyLTJljZfFGG8eLIOkcwdi9UcPBtIwzFcTNWnjap4RBu7ZtPNxpIt4UTCWWwb3sNfiE6UFB9UBFCUF3IYNVVZ_GoGeB4WotXDE_7mXHenqdNx-N1Bsleba"
    })
    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
