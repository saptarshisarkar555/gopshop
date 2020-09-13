package com.example.whatsapplite;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.whatsapplite.util.JsonUtil;
import com.example.whatsapplite.util.PubnubUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private SupportMapFragment mMapFragment;
    private GoogleMap mGoogleMap;
    private Marker driverMarker;
    private static PubNub pubNub;

    private String messageReceiverID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        checkPermission();

        messageReceiverID = getIntent().getExtras().get("receiver_id").toString();
        pubNub = PubnubUtil.getPubnubInstance();

        mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Can add more as per requirement
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapFragment.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapFragment.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapFragment.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapFragment.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            mGoogleMap = googleMap;
            mGoogleMap.setMyLocationEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        Log.d("Passenger Activity", "Map ready");

        // This code adds the listener and subscribes passenger to channel with driver's location.
        pubNub.addListener(new SubscribeCallback() {
            @Override
            public void status(PubNub pub, PNStatus status) {
                Log.d("Passenger Activity", status.toString());
            }

            @Override
            public void message(PubNub pub, final PNMessageResult message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Map<String, String> newLocation = JsonUtil.fromJson(
                                    message.getMessage().toString(),
                                    LinkedHashMap.class
                            );

                            updateUI(newLocation);
                        } catch (Exception e) {
                            Toast.makeText(TrackingActivity.this,
                                    "You can't track this person now",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                });
            }

            @Override
            public void presence(PubNub pub, PNPresenceEventResult presence) {
                Log.d("Passenger Activity", presence.toString());
            }
        });
        pubNub.subscribe()
                .channels(Arrays.asList(messageReceiverID)) // subscribe to channels
                .execute();
        Log.d("Passenger Activity", "registered");
    }

    /*
        This method gets the new location of driver and calls method animateCar
        to move the marker slowly along linear path to this location.
        Also moves camera, if marker is outside of map bounds.
     */
    private void updateUI(Map<String, String> newLoc) {
        Log.d("Passenger Activity", "found user's location");
        LatLng newLocation = new LatLng(Double.valueOf(newLoc.get("lat")), Double.valueOf(newLoc.get("lng")));
        Log.d("Found location", newLoc.get("lat") + " , " + newLoc.get("lng"));
        if (driverMarker != null) {
            animateCar(newLocation);
            boolean contains = mGoogleMap.getProjection()
                    .getVisibleRegion()
                    .latLngBounds
                    .contains(newLocation);
            if (!contains) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));
            }
        } else {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    newLocation, 15.5f));
            driverMarker = mGoogleMap.addMarker(new MarkerOptions().position(newLocation).
                    icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
        }
    }

    /*
        Animates car by moving it by fractions of the full path and finally moving it to its
        destination in a duration of 5 seconds.
     */
    private void animateCar(final LatLng destination) {
        final LatLng startPosition = driverMarker.getPosition();
        final LatLng endPosition = new LatLng(destination.latitude, destination.longitude);
        final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(5000); // duration 5 seconds
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                try {
                    float v = animation.getAnimatedFraction();
                    LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                    driverMarker.setPosition(newPosition);
                } catch (Exception ex) {
                }
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
            }
        });
        valueAnimator.start();
    }

    /*
        This interface defines the interpolate method that allows us to get LatLng coordinates for
        a location a fraction of the way between two points. It also utilizes a Linear method, so
        that paths are linear, as they should be in most streets.
     */
    private interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }
}