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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * 16:9 设置页 — 精修版
 * 焦点：白底 + 2px 主色蓝描边 + 轻阴影 + scale 1.03
 * 右侧田字格更扁；图标使用资源图
 */
public class SettingsActivity extends Activity {

    private Prefs prefs;
    private LinearLayout autoAppsContainer;
    private TextView tvLauncherName;
    private TextView tvLauncherPkg;
    private TextView tvLauncherAvatar;
    private LinearLayout statusBadge;
    private List<AppUtils.AppItem> launchers;
    private List<AppUtils.AppItem> apps;

    private static final int C_BG = Color.parseColor("#F4F5F7");
    private static final int C_CARD = Color.WHITE;
    private static final int C_TITLE = Color.parseColor("#1C1C1E");
    private static final int C_SUB = Color.parseColor("#8E8E93");
    private static final int C_MUTED = Color.parseColor("#AEAEB2");
    private static final int C_ACCENT = Color.parseColor("#007AFF");
    private static final int C_GREEN = Color.parseColor("#34C759");
    private static final int C_RED = Color.parseColor("#FF3B30");
    private static final int C_ROW = Color.parseColor("#F7F8FA");
    private static final int C_TILE = Color.parseColor("#FAFBFC");
    private static final int C_SAVE_BG = Color.parseColor("#E8F1FF");
    private static final int C_FOCUS = Color.parseColor("#007AFF");

    private final int[] AVATAR_COLORS = {
            Color.parseColor("#FF2D55"),
            Color.parseColor("#FFCC00"),
            Color.parseColor("#5AC8FA"),
            Color.parseColor("#AF52DE"),
            Color.parseColor("#34C759"),
            Color.parseColor("#007AFF"),
            Color.parseColor("#FF9500"),
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
        page.setPadding(dp(36), dp(20), dp(36), dp(14));

        // 顶栏
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        titleBox.addView(text("Starter", 22, C_TITLE, true));
        TextView sub = text("开机启动助手", 12, C_SUB, false);
        sub.setPadding(0, dp(2), 0, 0);
        titleBox.addView(sub);
        header.addView(titleBox);

        // 状态徽章：白/浅灰底 + 绿点 + 深灰字
        statusBadge = new LinearLayout(this);
        statusBadge.setOrientation(LinearLayout.HORIZONTAL);
        statusBadge.setGravity(Gravity.CENTER_VERTICAL);
        statusBadge.setPadding(dp(12), dp(7), dp(14), dp(7));
        GradientDrawable badgeBg = round(Color.parseColor("#F0F1F3"), 20);
        statusBadge.setBackground(badgeBg);
        View dot = new View(this);
        LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(7), dp(7));
        dotLp.rightMargin = dp(8);
        dot.setLayoutParams(dotLp);
        GradientDrawable dotBg = round(C_GREEN, 4);
        dot.setBackground(dotBg);
        statusBadge.addView(dot);
        TextView badgeText = text("配置已实时生效", 12, Color.parseColor("#3A3A3C"), false);
        badgeText.setTag("badge_text");
        statusBadge.addView(badgeText);
        header.addView(statusBadge);
        page.addView(header);
        page.addView(spacer(14));

        // 主内容
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.HORIZONTAL);
        body.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 1f));

        LinearLayout left = card();
        left.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1.2f));
        left.setPadding(dp(18), dp(16), dp(18), dp(16));
        buildLeft(left);
        body.addView(left);
        body.addView(hSpace(12));

        LinearLayout right = card();
        right.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 0.8f));
        right.setPadding(dp(14), dp(14), dp(14), dp(14));
        buildRight(right);
        body.addView(right);
        page.addView(body);
        page.addView(spacer(10));

        // 底部说明：绿点 + 更紧凑字距
        LinearLayout footer = new LinearLayout(this);
        footer.setOrientation(LinearLayout.HORIZONTAL);
        footer.setGravity(Gravity.CENTER);
        footer.addView(footerItem("自启动应用在开机时拉起；清后台后按 Home 也会自动补拉。"));
        footer.addView(hSpace(16));
        footer.addView(footerItem("平时按 Home 回到真正桌面；点本图标打开本设置。"));
        footer.addView(hSpace(16));
        footer.addView(footerItem("请将 Starter 设为系统默认桌面。"));
        page.addView(footer);

        return page;
    }

    private LinearLayout footerItem(String msg) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        View d = new View(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(5), dp(5));
        lp.rightMargin = dp(6);
        d.setLayoutParams(lp);
        d.setBackground(round(C_GREEN, 3));
        row.addView(d);
        TextView t = text(msg, 11, C_MUTED, false);
        t.setLetterSpacing(-0.02f);
        row.addView(t);
        return row;
    }

    private void buildLeft(LinearLayout left) {
        left.addView(section("当前默认桌面"));

        LinearLayout launcherRow = rowBg();
        launcherRow.setPadding(dp(12), dp(10), dp(12), dp(10));
        launcherRow.setGravity(Gravity.CENTER_VERTICAL);
        tvLauncherAvatar = avatarView("?", 0);
        launcherRow.addView(tvLauncherAvatar);
        launcherRow.addView(hSpace(10));
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        tvLauncherName = text("未选择", 14, C_TITLE, true);
        tvLauncherPkg = text("", 11, C_SUB, false);
        tvLauncherPkg.setPadding(0, dp(2), 0, 0);
        info.addView(tvLauncherName);
        info.addView(tvLauncherPkg);
        launcherRow.addView(info);
        left.addView(launcherRow);
        left.addView(spacer(14));

        LinearLayout autoHeader = new LinearLayout(this);
        autoHeader.setOrientation(LinearLayout.HORIZONTAL);
        autoHeader.setGravity(Gravity.CENTER_VERTICAL);
        TextView autoTitle = section("开机自启动应用");
        autoTitle.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        autoTitle.setPadding(0, 0, 0, 0);
        autoHeader.addView(autoTitle);

        TextView clearBtn = text("清空列表", 12, C_RED, false);
        clearBtn.setPadding(dp(10), dp(5), dp(10), dp(5));
        clearBtn.setBackground(round(Color.TRANSPARENT, 8));
        clearBtn.setTag(0x1001, Color.TRANSPARENT);
        makeFocusable(clearBtn, true, () -> {
            prefs.setAutoApps(new ArrayList<>());
            refresh();
            toast("已清空");
        });
        autoHeader.addView(clearBtn);
        left.addView(autoHeader);
        left.addView(spacer(6));

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
        right.addView(spacer(4));

        // 更扁的田字格：用固定较小高度比例
        LinearLayout row1 = new LinearLayout(this);
        row1.setOrientation(LinearLayout.HORIZONTAL);
        // 高度权重略小，整体更扁
        row1.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 0.9f));
        row1.addView(actionTile(R.drawable.ic_tile_launcher, "选择桌面", false, this::showLauncherPicker));
        row1.addView(hSpace(8));
        row1.addView(actionTile(R.drawable.ic_tile_autostart, "选择自启应用", false, this::showAppPicker));
        right.addView(row1);
        right.addView(spacer(8));

        LinearLayout row2 = new LinearLayout(this);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        row2.setLayoutParams(new LinearLayout.LayoutParams(-1, 0, 0.9f));
        row2.addView(actionTile(R.drawable.ic_tile_reboot, "模拟开机测试", false, () -> {
            prefs.setNeedAutoStart(true);
            toast("已标记，按 Home 测试");
        }));
        row2.addView(hSpace(8));
        row2.addView(actionTile(R.drawable.ic_tile_save, "保存设置", true, () -> {
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

    private LinearLayout actionTile(int iconRes, String label, boolean primary, Runnable action) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setLayoutParams(new LinearLayout.LayoutParams(0, -1, 1f));
        int bgColor = primary ? C_SAVE_BG : C_TILE;
        GradientDrawable d = round(bgColor, 12);
        d.setStroke(dp(1), Color.parseColor("#EBEBEF"));
        tile.setBackground(d);
        // 内边距减小，按钮更扁
        tile.setPadding(dp(6), dp(8), dp(6), dp(8));
        tile.setTag(0x1001, bgColor);

        ImageView ic = new ImageView(this);
        int is = dp(28);
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(is, is);
        ic.setLayoutParams(ilp);
        ic.setImageResource(iconRes);
        ic.setScaleType(ImageView.ScaleType.FIT_CENTER);
        ic.setFocusable(false);
        tile.addView(ic);

        TextView lb = text(label, 12, primary ? C_ACCENT : C_TITLE, true);
        lb.setGravity(Gravity.CENTER);
        lb.setPadding(0, dp(6), 0, 0);
        tile.addView(lb);

        makeFocusable(tile, true, action);
        return tile;
    }

    private void refresh() {
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

        // 状态徽章文字
        TextView badgeText = null;
        for (int i = 0; i < statusBadge.getChildCount(); i++) {
            View c = statusBadge.getChildAt(i);
            if ("badge_text".equals(c.getTag())) badgeText = (TextView) c;
        }
        if (prefs.isConfigured() && prefs.getRealLauncher() != null) {
            if (badgeText != null) badgeText.setText("配置已实时生效");
        } else {
            if (badgeText != null) badgeText.setText("尚未保存配置");
        }

        autoAppsContainer.removeAllViews();
        List<String> list = prefs.getAutoApps();
        if (list.isEmpty()) {
            TextView empty = text("暂无自启动应用，点击右侧添加", 12, C_MUTED, false);
            empty.setPadding(dp(4), dp(10), 0, 0);
            autoAppsContainer.addView(empty);
        } else {
            for (int i = 0; i < list.size(); i++) {
                autoAppsContainer.addView(autoAppRow(list.get(i)));
                if (i < list.size() - 1) autoAppsContainer.addView(spacer(6));
            }
        }
    }

    private LinearLayout autoAppRow(String pkg) {
        LinearLayout row = rowBg();
        row.setPadding(dp(10), dp(9), dp(8), dp(9));
        row.setGravity(Gravity.CENTER_VERTICAL);

        String label = AppUtils.getAppLabel(this, pkg);
        row.addView(avatarView(label, Math.abs(pkg.hashCode())));
        row.addView(hSpace(10));

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        info.addView(text(label, 13, C_TITLE, true));
        TextView p = text(pkg, 11, C_SUB, false);
        p.setPadding(0, dp(2), 0, 0);
        info.addView(p);
        row.addView(info);

        // 移除：默认隐藏，行获焦时显示
        TextView remove = text("移除", 12, C_RED, false);
        remove.setPadding(dp(10), dp(5), dp(10), dp(5));
        remove.setVisibility(View.INVISIBLE);
        remove.setTag("remove_btn");
        final String pkgFinal = pkg;
        final String labelFinal = label;
        makeFocusable(remove, false, () -> {
            prefs.removeAutoApp(pkgFinal);
            refresh();
            toast("已移除 " + labelFinal);
        });
        row.addView(remove);

        // 行本身可获焦：获焦时显示移除
        row.setFocusable(true);
        row.setFocusableInTouchMode(true);
        row.setOnFocusChangeListener((v, hasFocus) -> {
            remove.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            applyFocusStyle(v, hasFocus, C_ROW);
            if (hasFocus) {
                // 让移除也可被右键聚焦
                remove.setFocusable(true);
            }
        });
        // 点击行不做事；方向键右可到移除
        return row;
    }

    private void showLauncherPicker() {
        if (launchers.isEmpty()) {
            toast("未找到其他桌面");
            return;
        }
        String current = prefs.getRealLauncher();
        ArrayList<PickerDialog.Item> items = new ArrayList<>();
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
        ArrayList<PickerDialog.Item> items = new ArrayList<>();
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

    // ——— helpers ———

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
        TextView tv = text(s, 11, C_SUB, false);
        tv.setPadding(0, 0, 0, dp(6));
        return tv;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(round(C_CARD, 14));
        c.setFocusable(false);
        return c;
    }

    private LinearLayout rowBg() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackground(round(C_ROW, 10));
        row.setTag(0x1001, C_ROW);
        return row;
    }

    private TextView avatarView(String label, int seed) {
        String ch = (label != null && label.length() > 0) ? label.substring(0, 1) : "?";
        TextView av = text(ch, 13, Color.WHITE, true);
        av.setGravity(Gravity.CENTER);
        int size = dp(32);
        av.setLayoutParams(new LinearLayout.LayoutParams(size, size));
        int color = AVATAR_COLORS[Math.abs(seed) % AVATAR_COLORS.length];
        av.setBackground(round(color, 9));
        return av;
    }

    private void setAvatar(TextView av, String label, int seed) {
        av.setText((label != null && label.length() > 0) ? label.substring(0, 1) : "?");
        av.setBackground(round(AVATAR_COLORS[Math.abs(seed) % AVATAR_COLORS.length], 9));
    }

    private GradientDrawable round(int color, float radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp((int) radiusDp));
        return d;
    }

    /**
     * 焦点：白底 + 2px 蓝描边 + 轻微放大
     * （阴影用加粗描边近似，避免 elevation 在老设备不一致）
     */
    private void makeFocusable(View v, boolean scale, Runnable action) {
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
        final int normalColor;
        if (v.getBackground() instanceof GradientDrawable) {
            // 无法可靠读回颜色，按类型粗分
            normalColor = C_TILE;
        } else {
            normalColor = Color.TRANSPARENT;
        }
        v.setOnFocusChangeListener((view, hasFocus) -> applyFocusStyle(view, hasFocus, normalColor));
    }

    private void applyFocusStyle(View view, boolean hasFocus, int normalFill) {
        if (!(view.getBackground() instanceof GradientDrawable)) {
            if (hasFocus) {
                GradientDrawable d = round(Color.WHITE, 10);
                d.setStroke(dp(2), C_FOCUS);
                view.setBackground(d);
            }
            return;
        }
        GradientDrawable d = (GradientDrawable) view.getBackground().mutate();
        if (hasFocus) {
            d.setColor(Color.WHITE);
            d.setStroke(dp(2), C_FOCUS);
            // 外圈弥散感：再叠一层略大描边色（半透明蓝）
            view.setScaleX(1.03f);
            view.setScaleY(1.03f);
            view.setElevation(dp(8));
        } else {
            // 恢复：需要知道原始填充色 —— 用 tag 存
            Object tag = view.getTag(0x1001);
            int fill = tag instanceof Integer ? (Integer) tag : normalFill;
            d.setColor(fill);
            d.setStroke(dp(1), Color.parseColor("#EBEBEF"));
            view.setScaleX(1f);
            view.setScaleY(1f);
            view.setElevation(0);
        }
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
