package com.fbsharebx.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawer;
    private EditText inputCookie, inputLink, inputLimit;
    private TextView responseView;
    private ProgressBar progress;
    private Button btnStart;

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
        responseView = findViewById(R.id.responseView);
        progress = findViewById(R.id.progressShare);
        btnStart = findViewById(R.id.btnStart);

        toolbar.setNavigationOnClickListener(v -> drawer.openDrawer(GravityCompat.START));
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_theme) {
                boolean dark = ThemeManager.toggle(this);
                Toast.makeText(this, dark ? "Dark mode" : "Light mode", Toast.LENGTH_SHORT).show();
                recreate();
                return true;
            }
            return false;
        });

        nav.setNavigationItemSelectedListener(this::onNavItem);
        btnStart.setOnClickListener(v -> startShare());
        swipe.setOnRefreshListener(() -> {
            swipe.setRefreshing(false);
            UpdateChecker.check(this, false);
        });

        // Auto check for updates on launch
        new Handler(Looper.getMainLooper()).postDelayed(
            () -> UpdateChecker.check(this, false), 1500);
    }

    private boolean onNavItem(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            // already on home
        } else if (id == R.id.nav_features) {
            new AlertDialog.Builder(this)
                .setTitle("Features")
                .setMessage("• Auto Facebook share via vern-rest-api\n" +
                            "• Customizable share limit\n" +
                            "• Light & Dark mode\n" +
                            "• Auto update notifications\n" +
                            "• Clean material UI\n" +
                            "• Pull-to-refresh update check\n" +
                            "• Persistent theme preference")
                .setPositiveButton("OK", null).show();
        } else if (id == R.id.nav_developer) {
            new AlertDialog.Builder(this)
                .setTitle("Developer")
                .setMessage("Developed by: notfound500\n\nVisit Facebook profile?")
                .setPositiveButton("Open", (d, w) ->
                    startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://www.facebook.com/notfound500"))))
                .setNegativeButton("Close", null).show();
        } else if (id == R.id.nav_theme) {
            boolean dark = ThemeManager.toggle(this);
            Toast.makeText(this, dark ? "Dark mode" : "Light mode", Toast.LENGTH_SHORT).show();
            recreate();
        } else if (id == R.id.nav_update) {
            UpdateChecker.check(this, true);
        } else if (id == R.id.nav_about) {
            new AlertDialog.Builder(this)
                .setTitle("About FB SHARE BX")
                .setMessage("Version " + BuildConfig.VERSION_NAME + "\n\n" +
                            "An auto-share helper powered by the vern-rest-api.\n\n" +
                            "Use responsibly. Educational purposes only.")
                .setPositiveButton("OK", null).show();
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
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
        responseView.setText("Sending request...");

        new Thread(() -> {
            String result;
            try {
                result = ApiClient.share(cookie, link, limit);
            } catch (Exception e) {
                result = "Error: " + e.getMessage();
            }
            final String out = result;
            new Handler(Looper.getMainLooper()).post(() -> {
                progress.setVisibility(View.GONE);
                btnStart.setEnabled(true);
                responseView.setText(out);
            });
        }).start();
    }
}
