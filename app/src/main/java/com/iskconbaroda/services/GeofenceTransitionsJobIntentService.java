/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iskconbaroda.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.JobIntentService;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.iskconbaroda.MyApplication;
import com.iskconbaroda.R;
import com.iskconbaroda.activities.MapsActivity;
import com.iskconbaroda.db.MyPlace;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Listener for geofence transition changes.
 *
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a notification
 * as the output.
 */
public class GeofenceTransitionsJobIntentService extends JobIntentService {

    private static final int JOB_ID = 573;

    private static final String TAG = "GeofenceTransitionsIS";
    public static final String SENT_SMS_ACTION_NAME = "SMS_SENT";
    public static final String DELIVERED_SMS_ACTION_NAME = "SMS_DELIVERED";
    private static final String CHANNEL_ID = "channel_01";

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, GeofenceTransitionsJobIntentService.class, JOB_ID, intent);
    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleWork(Intent intent) {
        Log.i(TAG, "GeoFenceIntent Called");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Log.e(TAG, "Transition " + geofencingEvent.getGeofenceTransition() + "  " + ", " + geofencingEvent.getErrorCode() + "  " + geofencingEvent.getTriggeringLocation() + "" );
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
//            String geofenceTransitionDetails = getGeofenceTransitionDetails(geofenceTransition,
//                    triggeringGeofences);
            MyPlace myPlace = getGeofenceTransitionDetails(triggeringGeofences);
            if(myPlace == null) {
                Log.d(TAG, "onHandleWork: Myplace is null!");
                return;
            }
                // Send notification and log the transition details.
//            try {
//                sendNotification(state ,myPlace);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            {
                try {
                    sendNotification(geofenceTransition, myPlace);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG,"" + geofenceTransition);
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return                      The transition details formatted as String.
     */
   /* private String getGeofenceTransitionDetails(
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList<String> triggeringGeofencesIdsList = new ArrayList<>();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }*/
    private MyPlace getGeofenceTransitionDetails(List<Geofence> triggeringGeofences) {
        for (Geofence geofence : triggeringGeofences) {
            MyPlace place = getGeoFencePlace(geofence);
            if (place != null) {
                return place;
            }
        }
        return null;
    }

    private MyPlace getGeoFencePlace(Geofence geofence) {
        MyPlace place = null;
        Log.d(TAG, "getGeoFencePlace() called with: geofence request id= [" + geofence.getRequestId() + "]");
        if (geofence.getRequestId() != null) {
            long placeId = Long.parseLong(geofence.getRequestId());
            place = MyApplication.getDatabase(getBaseContext()).getPlace(placeId);
        }
        return place;
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

    private void sendNotification(int transition_state, MyPlace myPlace) throws IOException {
        String state="";
        if(transition_state==Geofence.GEOFENCE_TRANSITION_ENTER)
        {
            state="Entering to";
        }
        else if(transition_state==Geofence.GEOFENCE_TRANSITION_EXIT)
        {
            state="Exiting from";
        }
        Log.d(TAG,"send");
        Intent resultIntent = new Intent(this, MapsActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(getBaseContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT, null);

        AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        SmsManager smsManager = SmsManager.getDefault();

        NotificationCompat.Builder notification1 = new NotificationCompat.Builder(this);

        if(transition_state==Geofence.GEOFENCE_TRANSITION_ENTER) {
            switch (myPlace.getActionType()) {
                case 1:
                    // myPlace.setActionType(TYPE_SILENT);
                    //currentStatus = (byte) (currentStatus & silent_status);
                    Log.d(TAG, "silent");
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    notification1.setSmallIcon(R.drawable.ic_ringer_silent)
                            /*.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.ic_ringer_silent))*/
                            .setContentTitle("Silent Mode Activated")
                            .build();
                    break;

                case 2:
                    //myPlace.setActionType(TYPE_VIBRATE);
                    Log.d(TAG, "vibrate");
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    notification1.setSmallIcon(R.drawable.ic_ringer_vibrate)
                            /*.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.ic_ringer_vibrate))*/
                            .setContentTitle("Vibrate Mode Activated")
                            .build();
                    break;

                case 3:
                    //myPlace.setActionType(TYPE_NORMAL);
                    Log.d(TAG, "normal");
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    notification1.setSmallIcon(R.drawable.ic_ringer_normal)
                            /*.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.ic_ringer_normal))*/
                            .setContentTitle("General Mode Activated")
                            .build();
                    break;

                case 4:
                    //myPlace.setActionType(TYPE_WIFI_ON);
                    Log.d(TAG, "Wifi on");
                    mWifiManager.setWifiEnabled(true);
                    notification1.setSmallIcon(R.drawable.ic_wifi_on)
                            /*.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.ic_wifi_on))*/
                            .setContentTitle("Wifi Enabled")
                            .build();
                    break;

                case 5:
                    //myPlace.setActionType(TYPE_WIFI_OFF);
                    Log.d(TAG, "Wifi off");
                    mWifiManager.setWifiEnabled(false);
                    notification1.setSmallIcon(R.drawable.ic_wifi_off)
                            /*.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.ic_wifi_off))*/
                            .setContentTitle("Wifi Disabled")
                            .build();
                    break;

                case 6:
                    //myPlace.setActionType(TYPE_BLUETOOTH_ON);
                    if (!mBluetoothAdapter.isEnabled()) {
                        Log.d(TAG, "Bluetooth Enable");
                        mBluetoothAdapter.enable();
                    }
                    notification1.setSmallIcon(R.drawable.ic_bluetooth_on)
                           /* .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.ic_bluetooth_on))*/
                            .setContentTitle("Bluetooth Enabled")
                            .build();
                    break;

                case 7:
                    //myPlace.setActionType(TYPE_BLUETOOTH_OFF);
                    if (mBluetoothAdapter.isEnabled()) {
                        Log.d(TAG, "Bluetooth Disable");
                        mBluetoothAdapter.disable();
                    }
                    notification1.setSmallIcon(R.drawable.ic_bluetooth_off)
                           /* .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.ic_bluetooth_off))*/
                            .setContentTitle("Bluetooth Disabled")
                            .build();
                    break;

                case 8:
                    //Schedule a Message
                    String number = String.valueOf(myPlace.getContactNo());
                    String message = myPlace.getMessage();
                    ArrayList<String> parts = smsManager.divideMessage(message);
                    PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT_SMS_ACTION_NAME), 0);
                    PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED_SMS_ACTION_NAME), 0);

                    ArrayList<PendingIntent> sendList = new ArrayList<>();
                    sendList.add(sentPI);

                    ArrayList<PendingIntent> deliverList = new ArrayList<>();
                    deliverList.add(deliveredPI);

                    smsManager.sendMultipartTextMessage(number, null, parts, sendList, deliverList);
                    //smsManager.sendMultipartTextMessage(number, null, parts, null, null);
                    notification1.setSmallIcon(R.drawable.ic_message_sent)
                            /*.setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.ic_message_sent))*/
                            .setContentTitle("Message Sent")
                            .build();

                   // MyApplication.getDatabase(getApplicationContext()).deleteMyPlace(myPlace.getDbId());
                    break;

                case 9:
                    // Simple Notification ( Reminder)
                    String reminder = myPlace.getReminder();
                    notification1.setSmallIcon(R.drawable.ic_reminder)
                           /* .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.ic_reminder))*/
                            .setContentTitle("Things To Do!!!")
                            .setContentText(reminder)
                            .build();
                    break;

                case 10:
                    // Custom Volume
                    break;

            }

        }

        Log.d(TAG, "sendNotification() called with: transition_state = [" + transition_state + "], myPlace = [" + myPlace.toString() + "]");


        if (transition_state==Geofence.GEOFENCE_TRANSITION_EXIT)
        {
            switch (myPlace.getActionType()) {
                case 1:
                    // myPlace.setActionType(TYPE_SILENT);
                    Log.d(TAG, "silent");
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    notification1.setSmallIcon(R.drawable.ic_ringer_normal)
                           /* .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.ic_ringer_normal))*/
                            .setContentTitle("Back to Normal")
                            .build();
                    break;

                case 2:
                    //myPlace.setActionType(TYPE_VIBRATE);
                    Log.d(TAG, "vibrate");
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    notification1.setSmallIcon(R.drawable.ic_ringer_normal)
                           /* .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.ic_ringer_normal))*/
                            .setContentTitle("Back to Normal")
                            .build();
                    break;

                case 3:
                    //myPlace.setActionType(TYPE_NORMAL);
                    Log.d(TAG, "normal");
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    notification1.setSmallIcon(R.drawable.ic_ringer_vibrate)
                           /* .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.ic_ringer_vibrate))*/
                            .setContentTitle("Vibrate Mode Activated")
                            .build();
                    break;

                case 4:
                    //myPlace.setActionType(TYPE_WIFI_ON);
                    Log.d(TAG, "Wifi on");
                    mWifiManager.setWifiEnabled(false);
                    notification1.setSmallIcon(R.drawable.ic_wifi_off)
                           /* .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.ic_wifi_off))*/
                            .setContentTitle("Wifi Disabled")
                            .build();
                    break;

                case 5:
                    //myPlace.setActionType(TYPE_WIFI_OFF);
                    Log.d(TAG, "Wifi on");
                    mWifiManager.setWifiEnabled(true);
                    notification1.setSmallIcon(R.drawable.ic_wifi_on)
                           /* .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.ic_wifi_on))*/
                            .setContentTitle("Wifi Enabled")
                            .build();
                    break;

                case 6:
                    //myPlace.setActionType(TYPE_BLUETOOTH_ON);
                    if (mBluetoothAdapter.isEnabled()) {
                        Log.d(TAG, "Bluetooth Disable");
                        mBluetoothAdapter.disable();
                    }
                    notification1.setSmallIcon(R.drawable.ic_bluetooth_off)
                           /* .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.ic_bluetooth_off))*/
                            .setContentTitle("Bluetooth Disabled")
                            .build();
                    break;

                case 7:
                    //myPlace.setActionType(TYPE_BLUETOOTH_OFF);
                    if (!mBluetoothAdapter.isEnabled()) {
                        Log.d(TAG, "Bluetooth Enable");
                        mBluetoothAdapter.enable();
                    }
                    notification1.setSmallIcon(R.drawable.ic_bluetooth_on)
                           /* .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                                    R.drawable.ic_bluetooth_on))*/
                            .setContentTitle("Bluetooth Enabled")
                            .build();
                    break;

                case 8:
                        //Schedule a Message
                    break;

                case 9:
                        //Simple Notification ( Reminder)

                    break;

                case 10:
                    // Custom Volume
                    break;

            }
        }


        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle("Hey Vaman !!!")
                .setContentText("You are " + state + " " + myPlace.getTitle() )
                .setContentIntent(resultPendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher2)
                .setWhen(System.currentTimeMillis())
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(Picasso.with(this).load("https://maps.googleapis.com/maps/api/staticmap?size=640x400&markers=color:red|" + myPlace.getLatitude() + "," + myPlace.getLongitude()).get())
                        .setSummaryText("You are " + state + " " + myPlace.getAddress()))
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // Play default notification sound
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, notification);
        // Issue the notification
        mNotificationManager.notify(0, notification1.build());

    }
}
