package com.iskconbaroda;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.iskconbaroda.services.NotifyingBootService;

/**
 * Created by Vaman Das on 4/12/2018.
 */

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent arg1) {
        // TODO Auto-generated method stub
        Log.w("boot_broadcast_poc", "starting service...");
        context.startService(new Intent(context, NotifyingBootService.class));

    }
}
