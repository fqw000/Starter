package com.wangqifei.eu.org.starter;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置页：偏 Apple 简洁风 + 遥控焦点高亮
 */
public class SettingsActivity extends Activity {

    private Prefs prefs;
    private TextView tvLauncher;
    private TextView tvAutoApps;
    private List<AppUtils.AppItem> launchers;
    private List<AppUtils.AppItem> apps;

    private static final int C_BG = Color.parseColor("#F2F2F7");
    private static final int C_CARD = Color.WHITE;
    private static final int C_TITLE = Color.parseColor("#1C1C1E");
    private static final int C_SUB = Color.parseColor("#8E8E93");
    private static final int C_ACCENT = Color.parseColor("#007AFF");
    private static final int C_ACCENT_DARK = Color.parseColor("#0056CC");
    private static final int C_BTN_SEC = Color.parseColor("#E8F0FE");
    private static final int C_FOCUS_RING = Color.parseColor("#FF9500"); // 焦点描边：橙色易辨认

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = new Prefs(this);
        launchers = AppUtils.getInstalledLaunchers(this);
        apps = AppUtils.getInstalledApps(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(C_BG);
        scroll.setFillViewport(true);
        scroll.setDescendantFocusability(android.view.ViewGroup.FOCUS_AFTER_DESCENDANTS);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(32);
        root.setPadding(pad, dp(28), pad, dp(36));
        scroll.addView(root);

        TextView title = text("Starter", 22, C_TITLE, true);
        root.addView(title);
        TextView sub = text("开机启动助手", 13, C_SUB, false);
        sub.setPadding(0, dp(2), 0, dp(20));
        root.addView(sub);

        // 桌面
        root.addView(section("真正的桌面"));
        LinearLayout c1 = card();
        tvLauncher = text(launcherSummary(), 14, C_TITLE, false);
        tvLauncher.setPadding(0, 0, 0, dp(10));
        c1.addView(tvLauncher);
        c1.addView(btn("选择桌面", true, v -> showLauncherPicker()));
        root.addView(c1);

        // 自启动
        root.addView(section("开机自启动应用"));
        LinearLayout c2 = card();
        tvAutoApps = text(autoAppsSummary(), 14, C_TITLE, false);
        tvAutoApps.setPadding(0, 0, 0, dp(10));
        c2.addView(tvAutoApps);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(weight(btn("添加", true, v -> showAppPicker()), 1f));
        View gap = new View(this);
        gap.setLayoutParams(new LinearLayout.LayoutParams(dp(8), 1));
        row.addView(gap);
        row.addView(weight(btn("清空", false, v -> {
            prefs.setAutoApps(new ArrayList<>());
            refresh();
            toast("已清空");
        }), 1f));
        c2.addView(row);
        root.addView(c2);

        // 操作
        root.addView(section("操作"));
        LinearLayout c3 = card();
        c3.addView(btn("保存设置", true, v -> {
            if (prefs.getRealLauncher() == null) {
                toast("请先选择真正的桌面");
                return;
            }
            prefs.setConfigured(true);
            toast("已保存");
        }));
        c3.addView(spacer(8));
        c3.addView(btn("立即启动自启动应用", false, v -> {
            AppUtils.startAutoApps(this, prefs.getAutoApps());
            toast("已尝试启动");
        }));
        c3.addView(spacer(8));
        c3.addView(btn("模拟开机（下次 Home 会拉自启动）", false, v -> {
            prefs.setNeedAutoStart(true);
            toast("已标记，按 Home 测试");
        }));
        root.addView(c3);

        TextView tip = text(
                "说明\n" +
                "· 自启动应用在开机时拉起；清后台后按 Home 也会自动补拉\n" +
                "· 平时按 Home 回到真正桌面；点本图标打开本设置\n" +
                "· 请将 Starter 设为系统默认桌面",
                12, C_SUB, false);
        tip.setPadding(dp(2), dp(16), dp(2), 0);
        tip.setLineSpacing(dp(2), 1f);
        root.addView(tip);

        setContentView(scroll);

        // 默认焦点到第一个按钮，方便遥控
        root.post(() -> {
            View first = findFirstButton(root);
            if (first != null) first.requestFocus();
        });
    }

    private View findFirstButton(LinearLayout root) {
        for (int i = 0; i < root.getChildCount(); i++) {
            View c = root.getChildAt(i);
            if (c instanceof Button && c.isFocusable()) return c;
            if (c instanceof LinearLayout) {
                View inner = findFirstButton((LinearLayout) c);
                if (inner != null) return inner;
            }
        }
        return null;
    }

    private void refresh() {
        tvLauncher.setText(launcherSummary());
        tvAutoApps.setText(autoAppsSummary());
    }

    private String launcherSummary() {
        String real = prefs.getRealLauncher();
        if (real == null) return "未选择";
        String pkg = real.contains("/") ? real.split("/")[0] : real;
        return AppUtils.getAppLabel(this, pkg) + "\n" + real;
    }

    private String autoAppsSummary() {
        List<String> list = prefs.getAutoApps();
        if (list.isEmpty()) return "无";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append("\n");
            String pkg = list.get(i);
            sb.append(AppUtils.getAppLabel(this, pkg)).append("  ·  ").append(pkg);
        }
        return sb.toString();
    }

    private void showLauncherPicker() {
        if (launchers.isEmpty()) {
            toast("未找到其他桌面");
            return;
        }
        CharSequence[] items = new CharSequence[launchers.size()];
        for (int i = 0; i < launchers.size(); i++) {
            AppUtils.AppItem it = launchers.get(i);
            items[i] = it.label + "\n" + it.packageName;
        }
        new AlertDialog.Builder(this)
                .setTitle("选择真正的桌面")
                .setItems(items, (d, which) -> {
                    AppUtils.AppItem it = launchers.get(which);
                    prefs.setRealLauncher(it.packageName + "/" + it.activityName);
                    refresh();
                    toast("已选择 " + it.label);
                }).show();
    }

    private void showAppPicker() {
        List<String> candidates = new ArrayList<>();
        candidates.add("com.softmedia.receiver");
        for (AppUtils.AppItem item : apps) {
            if (!candidates.contains(item.packageName)) candidates.add(item.packageName);
        }
        CharSequence[] items = new CharSequence[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            String pkg = candidates.get(i);
            items[i] = AppUtils.getAppLabel(this, pkg) + "\n" + pkg;
        }
        new AlertDialog.Builder(this)
                .setTitle("添加自启动应用")
                .setItems(items, (d, which) -> {
                    String pkg = candidates.get(which);
                    prefs.addAutoApp(pkg);
                    refresh();
                    toast("已添加 " + AppUtils.getAppLabel(this, pkg));
                }).show();
    }

    // —— UI ——

    private TextView text(String s, float sp, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(s);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        tv.setTextColor(color);
        tv.setTypeface(Typeface.create(bold ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
        tv.setFocusable(false);
        return tv;
    }

    private TextView section(String s) {
        TextView tv = text(s, 12, C_SUB, false);
        tv.setPadding(dp(2), dp(14), 0, dp(6));
        return tv;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        int p = dp(14);
        card.setPadding(p, p, p, p);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(C_CARD);
        bg.setCornerRadius(dp(10));
        card.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(2);
        card.setLayoutParams(lp);
        card.setFocusable(false);
        return card;
    }

    /** 主按钮 / 次按钮，带遥控焦点高亮（描边 + 略放大） */
    private Button btn(String label, boolean primary, View.OnClickListener click) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        b.setAllCaps(false);
        b.setFocusable(true);
        b.setFocusableInTouchMode(true);
        int vPad = dp(8);
        int hPad = dp(14);
        b.setPadding(hPad, vPad, hPad, vPad);
        b.setMinHeight(dp(36));
        b.setOnClickListener(click);

        final int normalBg = primary ? C_ACCENT : C_BTN_SEC;
        final int normalFg = primary ? Color.WHITE : C_ACCENT;
        b.setTextColor(normalFg);
        applyBtnBg(b, normalBg, 0);

        b.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // 焦点：橙色描边 + 略深底色，遥控器上非常明显
                int fill = primary ? C_ACCENT_DARK : Color.parseColor("#D0E3FC");
                applyBtnBg(b, fill, dp(3));
                b.setScaleX(1.03f);
                b.setScaleY(1.03f);
            } else {
                applyBtnBg(b, normalBg, 0);
                b.setScaleX(1f);
                b.setScaleY(1f);
            }
        });
        return b;
    }

    private void applyBtnBg(Button b, int fill, int strokeWidth) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill);
        d.setCornerRadius(dp(8));
        if (strokeWidth > 0) {
            d.setStroke(strokeWidth, C_FOCUS_RING);
        }
        b.setBackground(d);
    }

    private View spacer(int dpH) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(dpH)));
        v.setFocusable(false);
        return v;
    }

    private LinearLayout weight(View v, float w) {
        LinearLayout box = new LinearLayout(this);
        box.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, w));
        box.addView(v, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return box;
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
}
