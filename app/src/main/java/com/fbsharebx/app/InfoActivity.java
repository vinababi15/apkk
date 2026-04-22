package com.fbsharebx.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class InfoActivity extends AppCompatActivity {

    public static final String EXTRA_PAGE = "page";
    public static final int PAGE_FEATURES = 1;
    public static final int PAGE_DEVELOPER = 2;
    public static final int PAGE_ABOUT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applySavedTheme(this);
        setContentView(R.layout.activity_info);

        MaterialToolbar bar = findViewById(R.id.infoToolbar);
        LinearLayout container = findViewById(R.id.infoContainer);
        bar.setNavigationOnClickListener(v -> finish());

        int page = getIntent().getIntExtra(EXTRA_PAGE, PAGE_ABOUT);
        switch (page) {
            case PAGE_FEATURES:
                bar.setTitle(getString(R.string.title_features));
                buildFeatures(container);
                break;
            case PAGE_DEVELOPER:
                bar.setTitle(getString(R.string.title_developer));
                buildDeveloper(container);
                break;
            default:
                bar.setTitle(getString(R.string.title_about));
                buildAbout(container);
        }
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, getResources().getDisplayMetrics());
    }

    private TextView heading(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(22);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setTextColor(getColor(android.R.color.white));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(4));
        t.setLayoutParams(lp);
        return t;
    }

    private TextView subtitle(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(13);
        t.setTextColor(0xCCFFFFFF);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        t.setLayoutParams(lp);
        return t;
    }

    private LinearLayout heroCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.bg_header);
        card.setPadding(dp(20), dp(24), dp(20), dp(24));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(16));
        card.setLayoutParams(lp);
        return card;
    }

    private View featureRow(String emojiBadge, String title, String desc) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setBackgroundResource(R.drawable.bg_card);
        row.setPadding(dp(16), dp(16), dp(16), dp(16));
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(12));
        row.setLayoutParams(lp);
        row.setElevation(dp(2));

        TextView badge = new TextView(this);
        badge.setText(emojiBadge);
        badge.setTextSize(20);
        badge.setBackgroundResource(R.drawable.bg_metric);
        badge.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(dp(48), dp(48));
        blp.setMarginEnd(dp(14));
        badge.setLayoutParams(blp);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView t = new TextView(this);
        t.setText(title);
        t.setTextSize(15);
        t.setTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD));
        t.setTextColor(getResources().getColor(android.R.color.darker_gray, getTheme()));
        t.setTextColor(0xFF111827);
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(android.R.attr.textColorPrimary, tv, true);
        t.setTextColor(tv.data);
        TextView d = new TextView(this);
        d.setText(desc);
        d.setTextSize(12);
        d.setTextColor(0xFF8A92A6);
        col.addView(t);
        col.addView(d);

        row.addView(badge);
        row.addView(col);
        return row;
    }

    private void buildFeatures(LinearLayout c) {
        LinearLayout hero = heroCard();
        hero.addView(heading("What's inside"));
        hero.addView(subtitle("Everything you need to power up your share game."));
        c.addView(hero);

        c.addView(featureRow("\uD83D\uDE80", "Auto Share", "Send hundreds of shares with one tap via vern-rest-api."));
        c.addView(featureRow("\u2699\uFE0F",  "Custom Limit", "Pick any share count up to your account safety limit."));
        c.addView(featureRow("\uD83C\uDF19", "Dark / Light", "A switch in the menu — match your vibe day and night."));
        c.addView(featureRow("\uD83D\uDD04", "Auto Update", "App checks GitHub releases on launch and prompts to update."));
        c.addView(featureRow("\uD83D\uDCCB", "Clean Result", "Pretty success card with response stats — no raw JSON."));
        c.addView(featureRow("\uD83D\uDCBE", "Persistent", "Theme and preferences saved across restarts."));
        c.addView(featureRow("\uD83D\uDD12", "Privacy", "No data leaves the device except your API request."));
    }

    private void buildDeveloper(LinearLayout c) {
        LinearLayout hero = heroCard();
        hero.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageView avatar = new ImageView(this);
        avatar.setImageResource(R.mipmap.ic_launcher_round);
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(dp(96), dp(96));
        alp.setMargins(0, 0, 0, dp(12));
        avatar.setLayoutParams(alp);
        hero.addView(avatar);

        TextView name = new TextView(this);
        name.setText("notfound500");
        name.setTextSize(22);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setTextColor(0xFFFFFFFF);
        name.setGravity(Gravity.CENTER);
        hero.addView(name);

        TextView role = new TextView(this);
        role.setText("Developer  •  Designer  •  Maintainer");
        role.setTextSize(12);
        role.setTextColor(0xCCFFFFFF);
        role.setGravity(Gravity.CENTER);
        hero.addView(role);

        c.addView(hero);

        c.addView(featureRow("\uD83D\uDC65", "Facebook", "facebook.com/notfound500"));
        c.addView(featureRow("\uD83D\uDCE6", "Repository", "github.com/vinababi15/apkk"));
        c.addView(featureRow("\u2728", "Made with", "Java, Material 3, and a lot of coffee."));

        MaterialButton fb = new MaterialButton(this);
        fb.setText("Open Facebook Profile");
        fb.setCornerRadius(dp(14));
        LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(54));
        flp.setMargins(0, dp(8), 0, 0);
        fb.setLayoutParams(flp);
        fb.setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.facebook.com/notfound500"))));
        c.addView(fb);
    }

    private void buildAbout(LinearLayout c) {
        LinearLayout hero = heroCard();
        hero.setGravity(Gravity.CENTER_HORIZONTAL);

        ImageView ic = new ImageView(this);
        ic.setImageResource(R.mipmap.ic_launcher);
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(dp(80), dp(80));
        alp.setMargins(0, 0, 0, dp(10));
        ic.setLayoutParams(alp);
        hero.addView(ic);

        TextView name = new TextView(this);
        name.setText(getString(R.string.app_name));
        name.setTextSize(22);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        name.setTextColor(0xFFFFFFFF);
        name.setGravity(Gravity.CENTER);
        hero.addView(name);

        TextView ver = new TextView(this);
        ver.setText("Version " + BuildConfig.VERSION_NAME);
        ver.setTextSize(12);
        ver.setTextColor(0xCCFFFFFF);
        ver.setGravity(Gravity.CENTER);
        hero.addView(ver);
        c.addView(hero);

        c.addView(featureRow("\uD83D\uDCD6", "About",
                "FB SHARE BX is a clean, modern Android client for the vern-rest-api auto-share endpoint."));
        c.addView(featureRow("\u2696\uFE0F", "License",
                "Provided as-is for educational purposes. Use responsibly."));
        c.addView(featureRow("\uD83D\uDCAC", "Support",
                "Reach the developer via the Facebook profile linked in the Developer page."));
    }
}
