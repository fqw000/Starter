package com.wangqifei.eu.org.starter;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * 16:9 电视友好设置页，参考用户提供的设计稿：
 * 左：当前桌面 + 自启动列表
 * 右：2x2 操作卡片
 */
public class SettingsActivity extends Activity {

    private Prefs prefs;
    private LinearLayout autoAppsContainer;
    private TextView tvLauncherName;
    private TextView tvLauncherPkg;
    private TextView tvLauncherAvatar;
    private TextView statusBadge;
    private List<AppUtils.AppItem> launchers;
    private List<AppUtils.AppItem> apps;

    // 设计色
    private static final int C_BG = Color.parseColor("#F4F5F7");
    private static final int C_CARD = Color.WHITE;
    private static final int C_TITLE = Color.parseColor("#1C1C1E");
    private static final int C_SUB = Color.parseColor("#8E8E93");
    private static final int C_MUTED = Color.parseColor("#AEAEB2");
    private static final int C_ACCENT = Color.parseColor("#007AFF");
    private static final int C_GREEN = Color.parseColor("#34C759");
    private static final int C_GREEN_BG = Color.parseColor("#E8F8EC");
    private static final int C_RED = Color.parseColor("#FF3B30");
    private static final int C_ROW = Color.parseColor("#F7F8FA");
    private static final int C_FOCUS = Color.parseColor("#34C759");
    private static final int C_TILE_BG = Color.parseColor("#FAFBFC");
    private static final int C_SAVE_BG = Color.parseColor("#E8F1FF");

    private final int[] AVATAR_COLORS = {
            Color.parseColor("#5AC8FA"),
            Color.parseColor("#FF9500"),
            Color.parseColor("#FF2D55"),
            Color.parseColor("#AF52DE"),
            Color.parseColor("#34C759"),
            Color.parseColor("#007AFF"),
            Color.parseColor("#FFCC00"),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = new Prefs(this);
        launchers = AppUtils.getInstalledLaunchers(this);
        apps = AppUtils.getInstalledApps(this);
        setContentView(buildRoot());
        refresh();
    }

    private View buildRoot() {
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(C_BG);
        int padH = dp(36);
        int padV = dp(22);
        page.setPadding(padH, padV, padH, dp(16));

        // —— 顶栏 ——
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        TextView title = text("Starter", 24, C_TITLE, true);
        TextView sub = text("开机启动助手", 13, C_SUB, false);
        sub.setPadding(0, dp(2), 0, 0);
        titleBox.addView(title);
        titleBox.addView(sub);
        header.addView(titleBox);

        statusBadge = text("●  配置已实时生效", 12, C_GREEN, false);
        statusBadge.setPadding(dp(14), dp(8), dp(14), dp(8));
        GradientDrawable badgeBg = round(C_GREEN_BG, 20);
        statusBadge.setBackground(badgeBg);
        header.addView(statusBadge);

        page.addView(header);
        page.addView(spacer(16));

        // —— 主内容：左右两栏 ——
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.HORIZONTAL);
        body.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1f));

        // 左栏
        LinearLayout left = card();
        left.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1.15f));
        left.setPadding(dp(20), dp(18), dp(20), dp(18));
        buildLeft(left);
        body.addView(left);

        body.addView(hSpace(14));

        // 右栏
        LinearLayout right = card();
        right.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 0.85f));
        right.setPadding(dp(18), dp(18), dp(18), dp(18));
        buildRight(right);
        body.addView(right);

        page.addView(body);
        page.addView(spacer(12));

        // 底栏说明
        TextView tip = text(
                "· 自启动应用在开机时拉起；清后台后按 Home 也会自动补拉。" +
                        "    · 平时按 Home 回到真正桌面；点本图标打开本设置。" +
                        "    · 请将 Starter 设为系统默认桌面。",
                11, C_MUTED, false);
        tip.setGravity(Gravity.CENTER);
        page.addView(tip);

        return page;
    }

    private void buildLeft(LinearLayout left) {
        left.addView(section("当前默认桌面"));

        // 桌面行
        LinearLayout launcherRow = rowBg();
        launcherRow.setPadding(dp(12), dp(12), dp(12), dp(12));
        launcherRow.setGravity(Gravity.CENTER_VERTICAL);

        tvLauncherAvatar = avatarView("?", 0);
        launcherRow.addView(tvLauncherAvatar);
        launcherRow.addView(hSpace(12));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        tvLauncherName = text("未选择", 15, C_TITLE, true);
        tvLauncherPkg = text("", 11, C_SUB, false);
        tvLauncherPkg.setPadding(0, dp(2), 0, 0);
        info.addView(tvLauncherName);
        info.addView(tvLauncherPkg);
        launcherRow.addView(info);
        left.addView(launcherRow);

        left.addView(spacer(18));

        // 自启动标题行
        LinearLayout autoHeader = new LinearLayout(this);
        autoHeader.setOrientation(LinearLayout.HORIZONTAL);
        autoHeader.setGravity(Gravity.CENTER_VERTICAL);
        TextView autoTitle = section("开机自启动应用");
        autoTitle.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        autoTitle.setPadding(0, 0, 0, 0);
        autoHeader.addView(autoTitle);

        TextView clearBtn = text("清空列表", 12, C_RED, false);
        clearBtn.setPadding(dp(12), dp(6), dp(12), dp(6));
        GradientDrawable clearBg = round(Color.TRANSPARENT, 8);
        clearBg.setStroke(dp(1), Color.parseColor("#FFCDD2"));
        clearBtn.setBackground(clearBg);
        makeFocusable(clearBtn, () -> {
            prefs.setAutoApps(new ArrayList<>());
            refresh();
            toast("已清空");
        });
        autoHeader.addView(clearBtn);
        left.addView(autoHeader);
        left.addView(spacer(8));

        ScrollView sv = new ScrollView(this);
        sv.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1f));
        sv.setDescendantFocusability(android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS);
        autoAppsContainer = new LinearLayout(this);
        autoAppsContainer.setOrientation(LinearLayout.VERTICAL);
        sv.addView(autoAppsContainer);
        left.addView(sv);
    }

    private void buildRight(LinearLayout right) {
        right.addView(section("系统操作"));
        right.addView(spacer(8));

        // 2x2 网格
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        row1.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1f));
        row1.addView(actionTile("⌂", "选择桌面", false, this::showLauncherPicker));
        row1.addView(hSpace(10));
        row1.addView(actionTile("+", "选择自启应用", false, this::showAppPicker));
        right.addView(row1);

        right.addView(spacer(10));

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1f));
        row2.addView(actionTile("▷", "模拟开机测试", false, () -> {
            prefs.setNeedAutoStart(true);
            toast("已标记，按 Home 测试");
        }));
        row2.addView(hSpace(10));
        row2.addView(actionTile("✓", "保存设置", true, () -> {
            if (prefs.getRealLauncher() == null) {
                toast("请先选择真正的桌面");
                return;
            }
            prefs.setConfigured(true);
            refresh();
            toast("已保存");
        }));
        right.addView(row2);
    }

    private LinearLayout actionTile(String icon, String label, boolean primary, Runnable action) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1f));
        int bg = primary ? C_SAVE_BG : C_TILE_BG;
        GradientDrawable d = round(bg, 14);
        d.setStroke(dp(1), Color.parseColor("#EBEBEF"));
        tile.setBackground(d);
        tile.setPadding(dp(8), dp(12), dp(8), dp(12));

        TextView ic = text(icon, 26, primary ? C_ACCENT : C_TITLE, false);
        ic.setGravity(Gravity.CENTER);
        TextView lb = text(label, 13, primary ? C_ACCENT : C_TITLE, true);
        lb.setGravity(Gravity.CENTER);
        lb.setPadding(0, dp(8), 0, 0);
        tile.addView(ic);
        tile.addView(lb);

        makeFocusable(tile, action);
        return tile;
    }

    private void refresh() {
        // 桌面
        String real = prefs.getRealLauncher();
        if (real == null) {
            tvLauncherName.setText("未选择");
            tvLauncherPkg.setText("请点击右侧「选择桌面」");
            setAvatar(tvLauncherAvatar, "?", 0);
        } else {
            String pkg = real.contains("/") ? real.split("/")[0] : real;
            String label = AppUtils.getAppLabel(this, pkg);
            tvLauncherName.setText(label);
            tvLauncherPkg.setText(real);
            setAvatar(tvLauncherAvatar, label, Math.abs(pkg.hashCode()));
        }

        // 状态
        if (prefs.isConfigured() && prefs.getRealLauncher() != null) {
            statusBadge.setText("●  配置已实时生效");
            statusBadge.setTextColor(C_GREEN);
            statusBadge.setBackground(round(C_GREEN_BG, 20));
        } else {
            statusBadge.setText("●  尚未保存配置");
            statusBadge.setTextColor(Color.parseColor("#FF9500"));
            statusBadge.setBackground(round(Color.parseColor("#FFF3E0"), 20));
        }

        // 自启动列表
        autoAppsContainer.removeAllViews();
        List<String> list = prefs.getAutoApps();
        if (list.isEmpty()) {
            TextView empty = text("暂无自启动应用，点击右侧添加", 13, C_MUTED, false);
            empty.setPadding(dp(4), dp(12), 0, 0);
            autoAppsContainer.addView(empty);
        } else {
            for (int i = 0; i < list.size(); i++) {
                autoAppsContainer.addView(autoAppRow(list.get(i), i));
                if (i < list.size() - 1) autoAppsContainer.addView(spacer(8));
            }
        }
    }

    private LinearLayout autoAppRow(String pkg, int index) {
        LinearLayout row = rowBg();
        row.setPadding(dp(10), dp(10), dp(10), dp(10));
        row.setGravity(Gravity.CENTER_VERTICAL);

        String label = AppUtils.getAppLabel(this, pkg);
        row.addView(avatarView(label, Math.abs(pkg.hashCode())));
        row.addView(hSpace(10));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        TextView name = text(label, 14, C_TITLE, true);
        TextView p = text(pkg, 11, C_SUB, false);
        p.setPadding(0, dp(2), 0, 0);
        info.addView(name);
        info.addView(p);
        row.addView(info);

        TextView remove = text("移除", 12, C_MUTED, false);
        remove.setPadding(dp(10), dp(6), dp(10), dp(6));
        makeFocusable(remove, () -> {
            prefs.removeAutoApp(pkg);
            refresh();
            toast("已移除 " + label);
        });
        row.addView(remove);
        return row;
    }

    private void showLauncherPicker() {
        if (launchers.isEmpty()) {
            toast("未找到其他桌面");
            return;
        }
        String current = prefs.getRealLauncher();
        java.util.ArrayList<PickerDialog.Item> items = new java.util.ArrayList<>();
        for (AppUtils.AppItem it : launchers) {
            String comp = it.packageName + "/" + it.activityName;
            boolean sel = current != null && current.equals(comp);
            items.add(new PickerDialog.Item(it.label, it.packageName, sel));
        }
        new PickerDialog(this, "选择真正的桌面", items, "按 OK 选择", index -> {
            AppUtils.AppItem it = launchers.get(index);
            prefs.setRealLauncher(it.packageName + "/" + it.activityName);
            refresh();
            toast("已选择 " + it.label);
        }).show();
    }

    private void showAppPicker() {
        List<String> candidates = new ArrayList<>();
        String[] prefer = {
                "com.softmedia.receiver",
                "com.xiaodianshi.tv.yst",
                "com.netease.cloudmusic.tv",
                "tv.danmaku.bili",
                "com.gitvdemo.video",
                "com.togic.launcher",
                "com.ktcp.video"
        };
        for (String p : prefer) candidates.add(p);
        for (AppUtils.AppItem item : apps) {
            if (!candidates.contains(item.packageName)) candidates.add(item.packageName);
        }

        List<String> already = prefs.getAutoApps();
        final List<String> show = new ArrayList<>();
        for (String c : candidates) {
            if (!already.contains(c)) {
                try {
                    getPackageManager().getApplicationInfo(c, 0);
                    show.add(c);
                } catch (Exception ignored) {}
            }
        }
        if (show.isEmpty()) {
            toast("没有更多可添加的应用");
            return;
        }

        java.util.ArrayList<PickerDialog.Item> items = new java.util.ArrayList<>();
        for (String pkg : show) {
            items.add(new PickerDialog.Item(AppUtils.getAppLabel(this, pkg), pkg, false));
        }
        new PickerDialog(this, "添加自启动应用", items, "+ 添加", index -> {
            String pkg = show.get(index);
            prefs.addAutoApp(pkg);
            refresh();
            toast("已添加 " + AppUtils.getAppLabel(this, pkg));
        }).show();
    }

    // ——— UI helpers ———

    private TextView text(String s, float sp, int color, boolean medium) {
        TextView tv = new TextView(this);
        tv.setText(s);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        tv.setTextColor(color);
        tv.setTypeface(Typeface.create(medium ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
        tv.setFocusable(false);
        return tv;
    }

    private TextView section(String s) {
        TextView tv = text(s, 12, C_SUB, false);
        tv.setPadding(0, 0, 0, dp(8));
        return tv;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable bg = round(C_CARD, 16);
        c.setBackground(bg);
        c.setFocusable(false);
        return c;
    }

    private LinearLayout rowBg() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable bg = round(C_ROW, 12);
        row.setBackground(bg);
        return row;
    }

    private TextView avatarView(String label, int seed) {
        String ch = "?";
        if (label != null && label.length() > 0) {
            ch = label.substring(0, 1);
        }
        TextView av = text(ch, 14, Color.WHITE, true);
        av.setGravity(Gravity.CENTER);
        int size = dp(36);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        av.setLayoutParams(lp);
        int color = AVATAR_COLORS[Math.abs(seed) % AVATAR_COLORS.length];
        av.setBackground(round(color, 10));
        return av;
    }

    private void setAvatar(TextView av, String label, int seed) {
        String ch = (label != null && label.length() > 0) ? label.substring(0, 1) : "?";
        av.setText(ch);
        int color = AVATAR_COLORS[Math.abs(seed) % AVATAR_COLORS.length];
        av.setBackground(round(color, 10));
    }

    private GradientDrawable round(int color, float radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp((int) radiusDp));
        return d;
    }

    private void makeFocusable(View v, Runnable action) {
        v.setFocusable(true);
        v.setFocusableInTouchMode(true);
        v.setClickable(true);
        v.setOnClickListener(view -> action.run());
        v.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP
                    && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                    || keyCode == KeyEvent.KEYCODE_ENTER)) {
                action.run();
                return true;
            }
            return false;
        });
        v.setOnFocusChangeListener((view, hasFocus) -> {
            if (view.getBackground() instanceof GradientDrawable) {
                GradientDrawable d = (GradientDrawable) view.getBackground().mutate();
                if (hasFocus) {
                    d.setStroke(dp(3), C_FOCUS);
                    view.setScaleX(1.03f);
                    view.setScaleY(1.03f);
                } else {
                    d.setStroke(0, Color.TRANSPARENT);
                    // 恢复默认描边（若原本有）
                    view.setScaleX(1f);
                    view.setScaleY(1f);
                }
            } else {
                if (hasFocus) {
                    view.setBackgroundColor(Color.parseColor("#33FF9F0A"));
                } else {
                    view.setBackgroundColor(Color.TRANSPARENT);
                }
            }
        });
    }

    private View spacer(int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(h)));
        v.setFocusable(false);
        return v;
    }

    private View hSpace(int w) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(w), 1));
        v.setFocusable(false);
        return v;
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
