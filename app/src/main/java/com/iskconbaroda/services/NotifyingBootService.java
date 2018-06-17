package com.iskconbaroda.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Vaman Das on 4/12/2018.
 */

public class NotifyingBootService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @SuppressLint("LongLogTag")
    @Override
    public int onStartCommand(Intent pIntent, int flags, int startId) {
        // TODO Auto-generated method stub
        Toast.makeText(this, "NotifyingBootService", Toast.LENGTH_LONG).show();
        Log.i("com.example.bootbroadcastpoc","NotifyingBootService");

        return super.onStartCommand(pIntent, flags, startId);
    }
}
