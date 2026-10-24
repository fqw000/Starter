package com.wangqifei.eu.org.starter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class AppUtils {
    private static final String TAG = "Starter";

    public static class AppItem {
        public String packageName;
        public String activityName;
        public String label;
        public boolean isLauncher;
    }

    public static List<AppItem> getInstalledLaunchers(Context context) {
        List<AppItem> result = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> homes = pm.queryIntentActivities(home, 0);

        Intent leanback = new Intent(Intent.ACTION_MAIN);
        leanback.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        List<ResolveInfo> tvs = pm.queryIntentActivities(leanback, 0);

        List<ResolveInfo> all = new ArrayList<>(homes);
        for (ResolveInfo ri : tvs) {
            boolean exists = false;
            for (ResolveInfo h : homes) {
                if (h.activityInfo.packageName.equals(ri.activityInfo.packageName)
                        && h.activityInfo.name.equals(ri.activityInfo.name)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) all.add(ri);
        }

        String self = context.getPackageName();
        for (ResolveInfo ri : all) {
            if (ri.activityInfo.packageName.equals(self)) continue;
            AppItem item = new AppItem();
            item.packageName = ri.activityInfo.packageName;
            item.activityName = ri.activityInfo.name;
            item.label = ri.loadLabel(pm).toString();
            item.isLauncher = true;
            result.add(item);
        }
        return result;
    }

    public static List<AppItem> getInstalledApps(Context context) {
        List<AppItem> result = new ArrayList<>();
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        String self = context.getPackageName();

        for (ApplicationInfo ai : apps) {
            if (ai.packageName.equals(self)) continue;
            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0
                    && !ai.packageName.contains("softmedia")
                    && !ai.packageName.contains("receiver")
                    && !ai.packageName.contains("airplay")) {
                continue;
            }
            AppItem item = new AppItem();
            item.packageName = ai.packageName;
            item.activityName = null;
            item.label = ai.loadLabel(pm).toString();
            item.isLauncher = false;
            result.add(item);
        }
        return result;
    }

    public static void launchRealLauncher(Context context, String component) {
        if (TextUtils.isEmpty(component)) return;
        try {
            String[] parts = component.split("/");
            if (parts.length != 2) return;
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setComponent(new ComponentName(parts[0], parts[1]));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
            Log.i(TAG, "forward to launcher: " + component);
        } catch (Exception e) {
            Log.e(TAG, "launch real launcher failed", e);
            try {
                Intent i = context.getPackageManager()
                        .getLaunchIntentForPackage(component.split("/")[0]);
                if (i != null) {
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(i);
                }
            } catch (Exception ignored) {}
        }
    }

    /**
     * 后台拉起自启动应用：优先只启服务，避免把界面推到前台抢桌面。
     * @param bringUi 仅在明确需要时（目前默认 false）才尝试启动界面
     */
    public static void startAutoApps(Context context, List<String> packages) {
        startAutoApps(context, packages, false);
    }

    public static void startAutoApps(Context context, List<String> packages, boolean bringUi) {
        if (packages == null) return;
        PackageManager pm = context.getPackageManager();
        for (String pkg : packages) {
            if (TextUtils.isEmpty(pkg)) continue;
            try {
                // AirReceiver：只启服务，不打开主界面
                if ("com.softmedia.receiver".equals(pkg)) {
                    Intent service = new Intent();
                    service.setClassName(pkg, "com.softmedia.receiver.app.AirReceiverService");
                    context.startService(service);
                    Log.i(TAG, "started service only: " + pkg);
                    if (!bringUi) continue;
                }

                // 其他应用：尽量只触发进程，不抢前台
                // 使用不带 CLEAR_TOP 的方式；若 bringUi=false，仍尝试 startService 式拉活
                if (!bringUi) {
                    // 尝试隐式启动已导出的服务（多数第三方没有，忽略失败）
                    try {
                        Intent bounce = pm.getLaunchIntentForPackage(pkg);
                        if (bounce != null) {
                            // 用 NEW_TASK 但不把用户视线长时间留在该页：由调用方立刻切回桌面
                            bounce.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            // 不使用 CLEAR_TOP，减少抢焦点
                            context.startActivity(bounce);
                            Log.i(TAG, "warmed up: " + pkg);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "warm up failed: " + pkg, e);
                    }
                } else {
                    Intent launch = pm.getLaunchIntentForPackage(pkg);
                    if (launch != null) {
                        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(launch);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "start failed: " + pkg, e);
            }
        }
    }

    public static String getAppLabel(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return ai.loadLabel(pm).toString();
        } catch (Exception e) {
            return packageName;
        }
    }
}
