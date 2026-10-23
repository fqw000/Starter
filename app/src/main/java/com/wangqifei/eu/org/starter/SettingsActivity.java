package com.wangqifei.eu.org.starter;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * 简洁设置页（偏 Apple 风格：留白、层级清晰、字号适中）
 */
public class SettingsActivity extends Activity {

    private Prefs prefs;
    private TextView tvLauncher;
    private TextView tvAutoApps;
    private List<AppUtils.AppItem> launchers;
    private List<AppUtils.AppItem> apps;

    private static final int COLOR_BG = Color.parseColor("#F2F2F7");
    private static final int COLOR_CARD = Color.WHITE;
    private static final int COLOR_TITLE = Color.parseColor("#1C1C1E");
    private static final int COLOR_SUB = Color.parseColor("#8E8E93");
    private static final int COLOR_ACCENT = Color.parseColor("#007AFF");
    private static final int COLOR_LINE = Color.parseColor("#E5E5EA");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = new Prefs(this);
        launchers = AppUtils.getInstalledLaunchers(this);
        apps = AppUtils.getInstalledApps(this);

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(COLOR_BG);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(28);
        root.setPadding(pad, dp(36), pad, dp(40));
        scroll.addView(root);

        // 标题
        TextView title = text("Starter", 26, COLOR_TITLE, true);
        title.setPadding(0, 0, 0, dp(4));
        root.addView(title);

        TextView subtitle = text("开机启动助手", 14, COLOR_SUB, false);
        subtitle.setPadding(0, 0, 0, dp(28));
        root.addView(subtitle);

        // —— 卡片 1：真正的桌面 ——
        root.addView(sectionLabel("真正的桌面"));
        LinearLayout card1 = card();
        tvLauncher = text(launcherSummary(), 15, COLOR_TITLE, false);
        tvLauncher.setPadding(0, 0, 0, dp(12));
        card1.addView(tvLauncher);
        card1.addView(primaryButton("选择桌面", v -> showLauncherPicker()));
        root.addView(card1);

        // —— 卡片 2：自启动应用 ——
        root.addView(sectionLabel("开机自启动应用"));
        LinearLayout card2 = card();
        tvAutoApps = text(autoAppsSummary(), 15, COLOR_TITLE, false);
        tvAutoApps.setPadding(0, 0, 0, dp(12));
        card2.addView(tvAutoApps);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(wrap(primaryButton("添加", v -> showAppPicker()), 1f));
        View space = new View(this);
        space.setLayoutParams(new LinearLayout.LayoutParams(dp(10), 1));
        row.addView(space);
        row.addView(wrap(secondaryButton("清空", v -> {
            prefs.setAutoApps(new ArrayList<>());
            refresh();
            toast("已清空");
        }), 1f));
        card2.addView(row);
        root.addView(card2);

        // —— 卡片 3：操作 ——
        root.addView(sectionLabel("操作"));
        LinearLayout card3 = card();
        card3.addView(primaryButton("保存设置", v -> {
            if (prefs.getRealLauncher() == null) {
                toast("请先选择真正的桌面");
                return;
            }
            prefs.setConfigured(true);
            toast("已保存。请将 Starter 设为默认桌面后重启测试");
        }));
        View sp1 = new View(this);
        sp1.setLayoutParams(new LinearLayout.LayoutParams(1, dp(10)));
        card3.addView(sp1);
        card3.addView(secondaryButton("立即启动自启动应用", v -> {
            AppUtils.startAutoApps(this, prefs.getAutoApps());
            toast("已尝试启动");
        }));
        View sp2 = new View(this);
        sp2.setLayoutParams(new LinearLayout.LayoutParams(1, dp(10)));
        card3.addView(sp2);
        card3.addView(secondaryButton("模拟开机（下次 Home 会拉自启动）", v -> {
            prefs.setNeedAutoStart(true);
            toast("已标记，按 Home 测试");
        }));
        root.addView(card3);

        // 说明
        TextView tip = text(
                "说明\n" +
                "· 自启动应用仅在开机时自动拉起一次\n" +
                "· 平时按 Home 只回到你选择的真正桌面\n" +
                "· 清空后台后可用上方「立即启动」手动恢复\n" +
                "· 请将 Starter 设为系统默认桌面",
                13, COLOR_SUB, false);
        tip.setPadding(dp(4), dp(20), dp(4), 0);
        tip.setLineSpacing(dp(3), 1f);
        root.addView(tip);

        setContentView(scroll);
    }

    private void refresh() {
        tvLauncher.setText(launcherSummary());
        tvAutoApps.setText(autoAppsSummary());
    }

    private String launcherSummary() {
        String real = prefs.getRealLauncher();
        if (real == null) return "未选择";
        String pkg = real.contains("/") ? real.split("/")[0] : real;
        String label = AppUtils.getAppLabel(this, pkg);
        return label + "\n" + real;
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
        // 每项：主标题应用名，副标题包名
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
                })
                .show();
    }

    private void showAppPicker() {
        List<String> candidates = new ArrayList<>();
        candidates.add("com.softmedia.receiver");
        for (AppUtils.AppItem item : apps) {
            if (!candidates.contains(item.packageName)) {
                candidates.add(item.packageName);
            }
        }
        CharSequence[] items = new CharSequence[candidates.size()];
        for (int i = 0; i < candidates.size(); i++) {
            String pkg = candidates.get(i);
            String label = AppUtils.getAppLabel(this, pkg);
            items[i] = label + "\n" + pkg;
        }
        new AlertDialog.Builder(this)
                .setTitle("添加自启动应用")
                .setItems(items, (d, which) -> {
                    String pkg = candidates.get(which);
                    prefs.addAutoApp(pkg);
                    refresh();
                    toast("已添加 " + AppUtils.getAppLabel(this, pkg));
                })
                .show();
    }

    // —— UI helpers ——

    private TextView text(String s, float sp, int color, boolean bold) {
        TextView tv = new TextView(this);
        tv.setText(s);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        else tv.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        return tv;
    }

    private TextView sectionLabel(String s) {
        TextView tv = text(s.toUpperCase(), 12, COLOR_SUB, false);
        tv.setPadding(dp(4), dp(18), 0, dp(8));
        return tv;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        int p = dp(16);
        card.setPadding(p, p, p, p);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(COLOR_CARD);
        bg.setCornerRadius(dp(12));
        card.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(4);
        card.setLayoutParams(lp);
        return card;
    }

    private Button primaryButton(String label, View.OnClickListener click) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(COLOR_ACCENT);
        bg.setCornerRadius(dp(10));
        b.setBackground(bg);
        b.setPadding(dp(12), dp(12), dp(12), dp(12));
        b.setOnClickListener(click);
        return b;
    }

    private Button secondaryButton(String label, View.OnClickListener click) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        b.setTextColor(COLOR_ACCENT);
        b.setAllCaps(false);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#E8F0FE"));
        bg.setCornerRadius(dp(10));
        b.setBackground(bg);
        b.setPadding(dp(12), dp(12), dp(12), dp(12));
        b.setOnClickListener(click);
        return b;
    }

    private LinearLayout wrap(View v, float weight) {
        LinearLayout box = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, weight);
        box.setLayoutParams(lp);
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
