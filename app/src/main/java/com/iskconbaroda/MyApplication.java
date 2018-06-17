package com.iskconbaroda;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.iskconbaroda.db.MarkMyPlacesDBHelper;

public class MyApplication extends Application {

    public static final String TAG = "LBS";
    public static final int MAX_GEOFENCES = 100;
    private MarkMyPlacesDBHelper mDbHelper = null;

    public static MarkMyPlacesDBHelper getDatabase(Context context) {
        return ((MyApplication) context.getApplicationContext()).mDbHelper;
    }

    public static void showGenericToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDbHelper = MarkMyPlacesDBHelper.getInstance(getApplicationContext());
    }

}
