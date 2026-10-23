package com.wangqifei.eu.org.starter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * HOME 入口。
 * 开机：拉自启动 App → 转发真正桌面
 * 平时 Home：只转发真正桌面
 */
public class MainActivity extends Activity {

    private static final String TAG = "Starter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handleLaunch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleLaunch();
    }

    private void handleLaunch() {
        Prefs prefs = new Prefs(this);

        if (!prefs.isConfigured()) {
            Intent i = new Intent(this, SettingsActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
            return;
        }

        boolean isBoot = prefs.needAutoStart();
        Log.i(TAG, "home launch, isBoot=" + isBoot);

        if (isBoot) {
            AppUtils.startAutoApps(this, prefs.getAutoApps());
            prefs.setNeedAutoStart(false);
            final String real = prefs.getRealLauncher();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                AppUtils.launchRealLauncher(this, real);
                finish();
            }, 1200);
        } else {
            AppUtils.launchRealLauncher(this, prefs.getRealLauncher());
            finish();
        }
    }
}
