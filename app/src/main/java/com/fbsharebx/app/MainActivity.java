package com.fbsharebx.app;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private EditText inputCookie, inputLink, inputLimit;
    private ProgressBar progress;
    private Button btnStart;
    private View resultCard;
    private ImageView resultIcon;
    private TextView resultTitle, resultSubtitle, resultBadge, resultMessage;
    private LinearLayout metricsRow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applySavedTheme(this);
        setContentView(R.layout.activity_main);

        drawer = findViewById(R.id.drawerLayout);
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        NavigationView nav = findViewById(R.id.navView);
        SwipeRefreshLayout swipe = findViewById(R.id.swipe);
        inputCookie = findViewById(R.id.inputCookie);
        inputLink = findViewById(R.id.inputLink);
        inputLimit = findViewById(R.id.inputLimit);
        progress = findViewById(R.id.progressShare);
        btnStart = findViewById(R.id.btnStart);

        resultCard = findViewById(R.id.resultCard);
        resultIcon = findViewById(R.id.resultIcon);
        resultTitle = findViewById(R.id.resultTitle);
        resultSubtitle = findViewById(R.id.resultSubtitle);
        resultBadge = findViewById(R.id.resultBadge);
        resultMessage = findViewById(R.id.resultMessage);
        metricsRow = findViewById(R.id.metricsRow);

        toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));

        // Theme switch inside the drawer menu
        MenuItem themeItem = nav.getMenu().findItem(R.id.nav_theme);
        final View actionView = themeItem.getActionView();
        if (actionView instanceof MaterialSwitch) {
            final MaterialSwitch sw = (MaterialSwitch) actionView;
            // Set state WITHOUT firing the listener to avoid recreate() loop
            sw.setOnCheckedChangeListener(null);
            sw.setChecked(ThemeManager.isDark(this));
            sw.setOnCheckedChangeListener((b, checked) -> {
                if (checked == ThemeManager.isDark(this)) return;
                ThemeManager.setDark(this, checked);
                drawer.closeDrawer(GravityCompat.START);
                new Handler(Looper.getMainLooper()).postDelayed(this::recreate, 220);
            });
        }

        nav.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                // already home
            } else if (id == R.id.nav_features) {
                openInfo(InfoActivity.PAGE_FEATURES);
            } else if (id == R.id.nav_developer) {
                openInfo(InfoActivity.PAGE_DEVELOPER);
            } else if (id == R.id.nav_about) {
                openInfo(InfoActivity.PAGE_ABOUT);
            } else if (id == R.id.nav_update) {
                UpdateChecker.check(this, true);
            } else if (id == R.id.nav_theme) {
                // Allow tapping the row itself to also toggle the switch
                if (actionView instanceof MaterialSwitch) {
                    MaterialSwitch sw = (MaterialSwitch) actionView;
                    sw.setChecked(!sw.isChecked());
                }
                return true;
            }
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });

        btnStart.setOnClickListener(v -> startShare());
        swipe.setOnRefreshListener(() -> {
            swipe.setRefreshing(false);
            UpdateChecker.check(this, false);
        });

        new Handler(Looper.getMainLooper()).postDelayed(
                () -> UpdateChecker.check(this, false), 1500);
    }

    private void openInfo(int page) {
        Intent i = new Intent(this, InfoActivity.class);
        i.putExtra(InfoActivity.EXTRA_PAGE, page);
        startActivity(i);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void startShare() {
        final String cookie = inputCookie.getText().toString().trim();
        final String link = inputLink.getText().toString().trim();
        final String limit = inputLimit.getText().toString().trim();

        if (TextUtils.isEmpty(cookie) || TextUtils.isEmpty(link) || TextUtils.isEmpty(limit)) {
            Toast.makeText(this, "Please fill in cookie, link and limit", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        btnStart.setEnabled(false);
        resultCard.setVisibility(View.GONE);

        new Thread(() -> {
            String body;
            int httpCode = -1;
            String err = null;
            try {
                ApiClient.Result r = ApiClient.shareWithCode(cookie, link, limit);
                body = r.body;
                httpCode = r.code;
            } catch (Exception e) {
                body = null;
                err = e.getMessage();
            }
            final String fbody = body;
            final int fcode = httpCode;
            final String ferr = err;
            new Handler(Looper.getMainLooper()).post(() -> {
                progress.setVisibility(View.GONE);
                btnStart.setEnabled(true);
                renderResult(fcode, fbody, ferr, limit, link);
            });
        }).start();
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    private void renderResult(int httpCode, String body, String err, String limit, String link) {
        resultCard.setVisibility(View.VISIBLE);
        metricsRow.removeAllViews();

        if (err != null) {
            applyError("Network Error", "Couldn't reach the server", "ERR", err);
            return;
        }

        // Try parse JSON
        boolean success = httpCode >= 200 && httpCode < 300;
        String message = body == null ? "" : body.trim();
        Integer countShared = null;
        String statusText = null;

        try {
            Object parsed = parse(message);
            if (parsed instanceof JSONObject) {
                JSONObject o = (JSONObject) parsed;
                statusText = firstString(o, "status", "result", "state");
                if (statusText != null) {
                    String s = statusText.toLowerCase();
                    if (s.contains("fail") || s.contains("error") || s.contains("invalid")) success = false;
                    if (s.contains("success") || s.equals("ok") || s.equals("true")) success = true;
                }
                if (o.has("success")) success = o.optBoolean("success", success);
                Integer n = firstInt(o, "count", "shared", "total", "amount", "shares");
                if (n != null) countShared = n;
                String msg = firstString(o, "message", "msg", "description", "info");
                if (msg != null) message = msg;
            } else if (parsed instanceof JSONArray) {
                countShared = ((JSONArray) parsed).length();
                message = "Received " + countShared + " items.";
            }
        } catch (Exception ignored) {}

        if (success) {
            applySuccess(httpCode, countShared, limit, link, message);
        } else {
            applyError("Request Failed",
                    "The server rejected the request",
                    httpCode > 0 ? ("HTTP " + httpCode) : "ERR",
                    message);
        }
    }

    private void applySuccess(int httpCode, Integer count, String limit, String link, String detail) {
        resultIcon.setImageResource(R.drawable.ic_check_circle);
        resultTitle.setText("Sharing Started");
        resultSubtitle.setText("Your request was accepted by the server");
        resultBadge.setText("HTTP " + httpCode);
        resultBadge.setBackgroundResource(R.drawable.bg_pill_success);
        resultBadge.setTextColor(0xFF16A34A);

        addMetric("Shares", count != null ? String.valueOf(count) : (limit != null ? limit : "—"));
        addMetric("Status", "OK");
        addMetric("Mode", "Auto");

        if (TextUtils.isEmpty(detail)) detail = "Your share job is now running. Sit back and watch the count climb.";
        resultMessage.setText(detail);
        resultMessage.setTextColor(getOnSurface());
    }

    private void applyError(String title, String subtitle, String badge, String detail) {
        resultIcon.setImageResource(R.drawable.ic_error_circle);
        resultTitle.setText(title);
        resultSubtitle.setText(subtitle);
        resultBadge.setText(badge);
        resultBadge.setBackgroundResource(R.drawable.bg_pill_error);
        resultBadge.setTextColor(0xFFDC2626);
        addMetric("Result", "Failed");
        addMetric("Retry", "Yes");
        if (TextUtils.isEmpty(detail)) detail = "Please verify your cookie, the post link, and the limit value, then try again.";
        resultMessage.setText(detail);
        resultMessage.setTextColor(getOnSurface());
    }

    private int getOnSurface() {
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, tv, true);
        return tv.data;
    }

    private void addMetric(String label, String value) {
        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setBackgroundResource(R.drawable.bg_metric);
        col.setPadding(dp(12), dp(10), dp(12), dp(10));
        col.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMarginEnd(dp(8));
        col.setLayoutParams(lp);

        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(18);
        v.setTypeface(Typeface.create("sans-serif-black", Typeface.BOLD));
        v.setTextColor(getOnSurface());
        v.setGravity(Gravity.CENTER);

        TextView l = new TextView(this);
        l.setText(label);
        l.setTextSize(11);
        l.setTextColor(0xFF8A92A6);
        l.setGravity(Gravity.CENTER);

        col.addView(v);
        col.addView(l);
        metricsRow.addView(col);
    }

    private static Object parse(String s) {
        if (s == null) return null;
        s = s.trim();
        try {
            if (s.startsWith("{")) return new JSONObject(s);
            if (s.startsWith("[")) return new JSONArray(s);
        } catch (Exception ignored) {}
        return null;
    }

    private static String firstString(JSONObject o, String... keys) {
        for (String k : keys) {
            if (o.has(k)) {
                Object v = o.opt(k);
                if (v != null && !(v instanceof JSONObject) && !(v instanceof JSONArray)) return String.valueOf(v);
            }
        }
        // fallback: any string value
        Iterator<String> it = o.keys();
        while (it.hasNext()) {
            String k = it.next();
            Object v = o.opt(k);
            if (v instanceof String && ((String) v).length() < 200) return (String) v;
        }
        return null;
    }

    private static Integer firstInt(JSONObject o, String... keys) {
        for (String k : keys) {
            if (o.has(k)) {
                int v = o.optInt(k, Integer.MIN_VALUE);
                if (v != Integer.MIN_VALUE) return v;
            }
        }
        return null;
    }
}
