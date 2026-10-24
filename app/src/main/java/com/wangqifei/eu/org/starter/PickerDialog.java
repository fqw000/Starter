package com.wangqifei.eu.org.starter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class PickerDialog extends Dialog {

    public interface OnPickListener {
        void onPick(int index);
    }

    public static class Item {
        public String title;
        public String subtitle;
        public boolean selected;

        public Item(String title, String subtitle, boolean selected) {
            this.title = title;
            this.subtitle = subtitle;
            this.selected = selected;
        }
    }

    private final String title;
    private final List<Item> items;
    private final OnPickListener listener;
    private final String actionHint;

    private static final int C_PANEL = Color.WHITE;
    private static final int C_ITEM = Color.parseColor("#F8FAFC");
    private static final int C_TEXT = Color.parseColor("#1D1D1F");
    private static final int C_SUB = Color.parseColor("#86868B");
    private static final int C_BRAND = Color.parseColor("#007AFF");
    private static final int C_GREEN = Color.parseColor("#34C759");
    private static final int C_GREEN_BG = Color.parseColor("#E8F5E9");
    private static final int C_LINE = Color.parseColor("#F0F0F5");

    private final int[] ICON_BG = {
            Color.parseColor("#E6F0FF"), Color.parseColor("#E8F5E9"),
            Color.parseColor("#FFF0F0"), Color.parseColor("#FFF3E0"),
            Color.parseColor("#F3E5F5"), Color.parseColor("#E0F7FA"),
            Color.parseColor("#F1F3F5"),
    };
    private final int[] ICON_FG = {
            Color.parseColor("#007AFF"), Color.parseColor("#34C759"),
            Color.parseColor("#FF3B30"), Color.parseColor("#FF9800"),
            Color.parseColor("#9C27B0"), Color.parseColor("#00ACC1"),
            Color.parseColor("#555555"),
    };

    public PickerDialog(Context context, String title, List<Item> items,
                        String actionHint, OnPickListener listener) {
        // 使用半透明主题，避免系统默认黑框
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        this.title = title;
        this.items = items;
        this.actionHint = actionHint;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setDimAmount(0f); // 自己画遮罩，不要系统 dim 黑框
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
        }

        // 全屏半透明遮罩（不是包在弹窗外的黑矩形）
        LinearLayout overlay = new LinearLayout(getContext());
        overlay.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setOrientation(LinearLayout.VERTICAL);
        overlay.setGravity(Gravity.CENTER);
        overlay.setBackgroundColor(Color.parseColor("#66000000"));
        overlay.setPadding(dp(40), dp(32), dp(40), dp(32));
        overlay.setOnClickListener(v -> dismiss());

        LinearLayout box = new LinearLayout(getContext());
        box.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable boxBg = new GradientDrawable();
        boxBg.setColor(C_PANEL);
        boxBg.setCornerRadius(dp(12));
        box.setBackground(boxBg);
        LinearLayout.LayoutParams boxLp = new LinearLayout.LayoutParams(dp(440), -2);
        box.setLayoutParams(boxLp);
        box.setOnClickListener(v -> {});

        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(22), dp(18), dp(18), dp(14));

        TextView titleView = text(title, 16, C_TEXT, true);
        titleView.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        header.addView(titleView);

        TextView closeHint = text("按 [返回] 关闭", 11, C_SUB, false);
        closeHint.setPadding(dp(12), dp(5), dp(12), dp(5));
        GradientDrawable hintBg = new GradientDrawable();
        hintBg.setColor(C_ITEM);
        hintBg.setCornerRadius(dp(10));
        closeHint.setBackground(hintBg);
        header.addView(closeHint);
        box.addView(header);

        View line = new View(getContext());
        line.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(1)));
        line.setBackgroundColor(C_LINE);
        box.addView(line);

        ScrollView scroll = new ScrollView(getContext());
        scroll.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(340)));
        scroll.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        LinearLayout body = new LinearLayout(getContext());
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(16), dp(12), dp(16), dp(16));
        scroll.addView(body);

        for (int i = 0; i < items.size(); i++) {
            final int index = i;
            body.addView(buildItem(items.get(i), i, () -> {
                if (listener != null) listener.onPick(index);
                dismiss();
            }));
            if (i < items.size() - 1) {
                View sp = new View(getContext());
                sp.setLayoutParams(new LinearLayout.LayoutParams(1, dp(8)));
                body.addView(sp);
            }
        }
        box.addView(scroll);
        overlay.addView(box);
        setContentView(overlay);

        body.post(() -> {
            if (body.getChildCount() > 0 && body.getChildAt(0) != null) {
                body.getChildAt(0).requestFocus();
            }
        });
    }

    private View buildItem(Item item, int index, Runnable onSelect) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(C_ITEM);
        bg.setCornerRadius(dp(10));
        row.setBackground(bg);
        row.setFocusable(true);
        row.setFocusableInTouchMode(true);
        row.setClickable(true);

        String ch = (item.title != null && item.title.length() > 0)
                ? item.title.substring(0, 1) : "?";
        TextView icon = text(ch, 13, ICON_FG[index % ICON_FG.length], true);
        icon.setGravity(Gravity.CENTER);
        int is = dp(34);
        icon.setLayoutParams(new LinearLayout.LayoutParams(is, is));
        GradientDrawable iconBg = new GradientDrawable();
        iconBg.setColor(ICON_BG[index % ICON_BG.length]);
        iconBg.setCornerRadius(dp(9));
        icon.setBackground(iconBg);
        row.addView(icon);

        View gap = new View(getContext());
        gap.setLayoutParams(new LinearLayout.LayoutParams(dp(12), 1));
        row.addView(gap);

        LinearLayout info = new LinearLayout(getContext());
        info.setOrientation(LinearLayout.VERTICAL);
        info.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
        info.addView(text(item.title, 14, C_TEXT, true));
        TextView pkg = text(item.subtitle != null ? item.subtitle : "", 11, C_SUB, false);
        pkg.setPadding(0, dp(2), 0, 0);
        info.addView(pkg);
        row.addView(info);

        if (item.selected) {
            TextView badge = text("当前使用", 11, C_GREEN, true);
            badge.setPadding(dp(10), dp(4), dp(10), dp(4));
            GradientDrawable bbg = new GradientDrawable();
            bbg.setColor(C_GREEN_BG);
            bbg.setCornerRadius(dp(10));
            badge.setBackground(bbg);
            row.addView(badge);
        } else {
            TextView hint = text(actionHint, 12, C_BRAND, true);
            hint.setAlpha(0.55f);
            hint.setTag("action_hint");
            row.addView(hint);
        }

        row.setOnClickListener(v -> onSelect.run());
        row.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP
                    && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                    || keyCode == KeyEvent.KEYCODE_ENTER)) {
                onSelect.run();
                return true;
            }
            return false;
        });

        row.setOnFocusChangeListener((v, hasFocus) -> {
            GradientDrawable d = new GradientDrawable();
            d.setCornerRadius(dp(10));
            TextView hintView = null;
            for (int i = 0; i < row.getChildCount(); i++) {
                View c = row.getChildAt(i);
                if (c instanceof TextView && "action_hint".equals(c.getTag())) {
                    hintView = (TextView) c;
                    break;
                }
            }
            if (hasFocus) {
                d.setColor(Color.WHITE);
                d.setStroke(dp(2), C_BRAND);
                if (hintView != null) hintView.setAlpha(1f);
            } else {
                d.setColor(C_ITEM);
                d.setStroke(0, Color.TRANSPARENT);
                if (hintView != null) hintView.setAlpha(0.55f);
            }
            v.setBackground(d);
        });

        return row;
    }

    private TextView text(String s, float sp, int color, boolean medium) {
        TextView tv = new TextView(getContext());
        tv.setText(s);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        tv.setTextColor(color);
        tv.setTypeface(Typeface.create(medium ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
        tv.setFocusable(false);
        return tv;
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v,
                getContext().getResources().getDisplayMetrics());
    }
}
