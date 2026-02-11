/**
 * File: KP2ATileService.java
 * Description: Orchestrates KeePass2Android launch.
 * Forces ACTIVE state in the system registry to prevent visual gray-out.
 * Copyright 2026, John Navas, All Rights Reserved
 */

package com.navasgroup.kp2atile;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.service.quicksettings.TileService;
import android.widget.Toast;

public class KP2ATileService extends TileService {

    private static final String PKG_OFFLINE = "keepass2android.keepass2android_nonet";
    private static final String PKG_STANDARD = "keepass2android.keepass2android";

    @Override
    public void onStartListening() {
        super.onStartListening();
        // No tile state manipulation — let SystemUI keep its last state
    }

    @Override
    public void onClick() {
        PackageManager pm = getPackageManager();

        if (checkAndLaunch(pm, PKG_OFFLINE)) return;
        if (checkAndLaunch(pm, PKG_STANDARD)) return;

        launchOwnActivity();
    }

    private boolean checkAndLaunch(PackageManager pm, String pkgName) {
        try {
            pm.getPackageInfo(pkgName, 0);
            Intent intent = pm.getLaunchIntentForPackage(pkgName);
            if (intent != null) {
                executeLaunch(intent);
            } else {
                showErrorToast(pkgName.equals(PKG_OFFLINE)
                        ? R.string.error_archived_offline
                        : R.string.error_archived_standard);
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void launchOwnActivity() {
        Intent intent = new Intent(this, LauncherActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        executeLaunch(intent);
    }

    private void executeLaunch(Intent intent) {
        unlockAndRun(() -> {
            try {
                PendingIntent pi = PendingIntent.getActivity(
                        this, 0, intent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                startActivityAndCollapse(pi);
            } catch (Exception e) {
                showErrorToast(R.string.error_system_busy);
            }
        });
    }

    private void showErrorToast(int stringResId) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(getApplicationContext(), getString(stringResId), Toast.LENGTH_LONG).show()
        );
    }
}