package com.wangqifei.eu.org.starter;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.List;

/**
 * 仅响应 HOME。
 * - 开机 / 自启动应用已不在运行：拉自启动，再进真正桌面
 * - 平时 Home：只进真正桌面
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

        // 若带 LAUNCHER 类别（少数系统会误投），转到设置
        Intent in = getIntent();
        if (in != null && in.getCategories() != null
                && in.getCategories().contains(Intent.CATEGORY_LAUNCHER)) {
            startActivity(new Intent(this, SettingsActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
            return;
        }

        if (!prefs.isConfigured()) {
            startActivity(new Intent(this, SettingsActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
            return;
        }

        boolean isBoot = prefs.needAutoStart();
        boolean autoMissing = !isAutoAppsRunning(prefs.getAutoApps());

        Log.i(TAG, "HOME launch isBoot=" + isBoot + " autoMissing=" + autoMissing);

        // 开机，或清后台后自启动应用已不在：补一次自启动
        if (isBoot || autoMissing) {
            AppUtils.startAutoApps(this, prefs.getAutoApps());
            prefs.setNeedAutoStart(false);
            final String real = prefs.getRealLauncher();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                AppUtils.launchRealLauncher(this, real);
                finish();
            }, isBoot ? 1200 : 600);
        } else {
            AppUtils.launchRealLauncher(this, prefs.getRealLauncher());
            finish();
        }
    }

    /** 自启动列表里是否至少有一个进程还在 */
    private boolean isAutoAppsRunning(List<String> packages) {
        if (packages == null || packages.isEmpty()) return true;
        try {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> procs = am.getRunningAppProcesses();
            if (procs == null) return false;
            for (String pkg : packages) {
                for (ActivityManager.RunningAppProcessInfo p : procs) {
                    if (p.processName != null && p.processName.startsWith(pkg)) {
                        return true;
                    }
                    if (p.pkgList != null) {
                        for (String pl : p.pkgList) {
                            if (pkg.equals(pl)) return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "check running failed", e);
        }
        return false;
    }
}
