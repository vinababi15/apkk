package com.fbsharebx.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import org.json.JSONObject;

public class UpdateChecker {
    private static final String RELEASES_API =
        "https://api.github.com/repos/vinababi15/apkk/releases/latest";

    public static void check(Context ctx, boolean userTriggered) {
        new Thread(() -> {
            try {
                String json = ApiClient.fetchUrl(RELEASES_API);
                JSONObject obj = new JSONObject(json);
                String tag = obj.optString("tag_name", "").replace("v", "");
                String html = obj.optString("html_url", "https://github.com/vinababi15/apkk/releases");
                String current = BuildConfig.VERSION_NAME;
                if (!tag.isEmpty() && !tag.equals(current)) {
                    new Handler(Looper.getMainLooper()).post(() ->
                        new AlertDialog.Builder(ctx)
                            .setTitle("Update available")
                            .setMessage("A new version (v" + tag + ") is available.\nYou are on v" + current + ".")
                            .setPositiveButton("Download", (d, w) -> {
                                ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(html)));
                            })
                            .setNegativeButton("Later", null)
                            .show());
                } else if (userTriggered) {
                    new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(ctx, "You are up to date (v" + current + ")", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                if (userTriggered) {
                    new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(ctx, "Update check failed", Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }
}
