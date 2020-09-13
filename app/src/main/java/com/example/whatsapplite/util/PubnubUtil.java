package com.example.whatsapplite.util;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;

public class PubnubUtil {

    private static PubNub mPubnubInstance;

    public static PubNub getPubnubInstance() {
        if (mPubnubInstance == null) {
            PNConfiguration pnConfiguration = new PNConfiguration();
            pnConfiguration.setSubscribeKey(Constants.PUBNUB_SUBSCRIBE_KEY);
            pnConfiguration.setPublishKey(Constants.PUBNUB_PUBLISH_KEY);
            pnConfiguration.setSecure(true);
            mPubnubInstance = new PubNub(pnConfiguration);
        }

        return mPubnubInstance;
    }


}
