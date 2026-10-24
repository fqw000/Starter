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
 * - 开机：后台拉自启动（尽量不抢界面）→ 立刻进真正桌面
 * - 平时 Home：只进真正桌面
 * - 清后台后自启动进程没了：后台补服务，仍进真正桌面（不展示自启动 UI）
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
        Log.i(TAG, "HOME isBoot=" + isBoot + " autoMissing=" + autoMissing);

        final String real = prefs.getRealLauncher();

        // 开机 或 自启动进程已死：后台补拉（不抢前台），然后立刻进真正桌面
        if (isBoot || autoMissing) {
            // bringUi=false：AirReceiver 只起服务；其它应用即使亮一下也会马上被桌面盖住
            AppUtils.startAutoApps(this, prefs.getAutoApps(), false);
            prefs.setNeedAutoStart(false);
        }

        // 始终优先回到真正桌面（关键修复：不要停在 AirReceiver 界面）
        AppUtils.launchRealLauncher(this, real);
        // 再补一次延迟转发，防止被自启动 Activity 盖住
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            AppUtils.launchRealLauncher(this, real);
            finish();
        }, isBoot ? 900 : 300);
    }

    private boolean isAutoAppsRunning(List<String> packages) {
        if (packages == null || packages.isEmpty()) return true;
        try {
            ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> procs = am.getRunningAppProcesses();
            if (procs == null) return false;
            for (String pkg : packages) {
                for (ActivityManager.RunningAppProcessInfo p : procs) {
                    if (p.processName != null && p.processName.startsWith(pkg)) return true;
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
