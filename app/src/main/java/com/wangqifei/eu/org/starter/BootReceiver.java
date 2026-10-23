package com.wangqifei.eu.org.starter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "Starter";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent != null ? intent.getAction() : "null";
        Log.i(TAG, "boot broadcast: " + action);

        Prefs prefs = new Prefs(context);
        if (!prefs.isConfigured()) return;

        prefs.setNeedAutoStart(true);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.i(TAG, "BootReceiver start auto apps");
            AppUtils.startAutoApps(context, prefs.getAutoApps());
        }, 8000);
    }
}
