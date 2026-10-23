package com.wangqifei.eu.org.starter;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Prefs {
    private static final String NAME = "starter_prefs";
    private static final String KEY_REAL_LAUNCHER = "real_launcher";
    private static final String KEY_AUTO_APPS = "auto_apps";
    private static final String KEY_CONFIGURED = "configured";
    private static final String KEY_NEED_AUTO_START = "need_auto_start";

    private final SharedPreferences sp;

    public Prefs(Context context) {
        sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public boolean isConfigured() {
        return sp.getBoolean(KEY_CONFIGURED, false);
    }

    public void setConfigured(boolean v) {
        sp.edit().putBoolean(KEY_CONFIGURED, v).apply();
    }

    public String getRealLauncher() {
        return sp.getString(KEY_REAL_LAUNCHER, null);
    }

    public void setRealLauncher(String component) {
        sp.edit().putString(KEY_REAL_LAUNCHER, component).apply();
    }

    public List<String> getAutoApps() {
        String raw = sp.getString(KEY_AUTO_APPS, "com.softmedia.receiver");
        if (TextUtils.isEmpty(raw)) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(raw.split(",")));
    }

    public void setAutoApps(List<String> packages) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < packages.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(packages.get(i));
        }
        sp.edit().putString(KEY_AUTO_APPS, sb.toString()).apply();
    }

    public void addAutoApp(String pkg) {
        List<String> list = getAutoApps();
        if (!list.contains(pkg)) {
            list.add(pkg);
            setAutoApps(list);
        }
    }

    public void removeAutoApp(String pkg) {
        List<String> list = getAutoApps();
        list.remove(pkg);
        setAutoApps(list);
    }

    public boolean needAutoStart() {
        return sp.getBoolean(KEY_NEED_AUTO_START, false);
    }

    public void setNeedAutoStart(boolean v) {
        sp.edit().putBoolean(KEY_NEED_AUTO_START, v).apply();
    }
}
