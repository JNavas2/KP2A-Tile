/**
 * File: LauncherActivity.java
 * Description: Manages app launch and tile addition. Automatically redirects
 * to Play Store (Offline version) if KP2A is missing for a smoother experience.
 * Copyright 2026, John Navas, All Rights Reserved
 */

package com.navasgroup.kp2atile;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LauncherActivity extends Activity {

    private final Executor executor = Executors.newSingleThreadExecutor();
    private static final String TAG = "KP2A-Launcher";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        if (!isKP2AInstalled()) {
            Toast.makeText(this, R.string.error_not_installed_redirect, Toast.LENGTH_LONG).show();
            new Handler(Looper.getMainLooper()).postDelayed(this::openPlayStore, 3000);
        }

        Button btn = findViewById(R.id.btnAddTile);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                if (isKP2AInstalled()) {
                    requestTile();
                } else {
                    openPlayStore();
                }
            });
        }
    }

    private boolean isKP2AInstalled() {
        PackageManager pm = getPackageManager();
        String[] pkgs = {
                "keepass2android.keepass2android_nonet",
                "keepass2android.keepass2android"
        };
        for (String pkg : pkgs) {
            try {
                pm.getPackageInfo(pkg, 0);
                return true;
            } catch (PackageManager.NameNotFoundException ignored) {}
        }
        return false;
    }

    private void requestTile() {
        try {
            StatusBarManager sbm = getSystemService(StatusBarManager.class);
            ComponentName cn = new ComponentName(this, KP2ATileService.class);

            if (sbm != null) {
                sbm.requestAddTileService(
                        cn,
                        getString(R.string.tile_label),
                        Icon.createWithResource(this, R.drawable.ic_kp2a_lock),
                        executor,
                        result -> runOnUiThread(() -> handleResult(result))
                );
            }
        } catch (Exception e) {
            Log.e(TAG, "Tile request failed", e);
            Toast.makeText(getApplicationContext(),
                    R.string.error_system_busy, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleResult(int result) {
        if (result == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED) {
            Toast.makeText(getApplicationContext(),
                    R.string.toast_added_success, Toast.LENGTH_SHORT).show();
            finish();
        } else if (result == StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED) {
            Toast.makeText(getApplicationContext(),
                    R.string.toast_already_exists, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(),
                    R.string.toast_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void openPlayStore() {
        Intent intent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=keepass2android.keepass2android_nonet"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception ignored) {}
    }
}