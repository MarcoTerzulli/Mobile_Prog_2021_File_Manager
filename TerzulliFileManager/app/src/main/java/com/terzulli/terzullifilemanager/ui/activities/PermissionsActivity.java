package com.terzulli.terzullifilemanager.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.utils.Utils;

public class PermissionsActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private final int PERMISSION_LIST_LENGHT = 3;
    private final int PERMISSION_CODE_STORAGE = 0,
            PERMISSION_CODE_INSTALL_APK = 1,
            PERMISSION_CODE_ALL_FILES = 2;

    private final OnPermissionGranted[] onPermissionGrantedCallbacks =
            new OnPermissionGranted[PERMISSION_LIST_LENGHT];

    public boolean checkStoragePermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(final String permission,
                                   final int permissionCode,
                                   @NonNull final AlertDialog.Builder rationaleDialog,
                                   boolean isFirstStart) {

        Utils.disableScreenRotation(this);

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            Toast.makeText(this, "mostro il rationale", Toast.LENGTH_SHORT).show();

            rationaleDialog
                    .setPositiveButton(R.string.grant, (thisDialog, which) -> {
                        ActivityCompat.requestPermissions(this, new String[]{permission}, permissionCode);
                        thisDialog.dismiss();
                    })
                    .create().show();
        } else if (isFirstStart) {
            Toast.makeText(this, "initial start", Toast.LENGTH_SHORT).show();

            ActivityCompat.requestPermissions(this, new String[]{permission}, permissionCode);
        } else {

            Toast.makeText(this, "dovrei aprire la snackbar", Toast.LENGTH_SHORT).show();

            int snackBarBackgroundColor, snackBarTextColor;
            if (isNightModeEnabled()) {
                snackBarBackgroundColor = ContextCompat.getColor(this, R.color.primary_500_night);
                snackBarTextColor = ContextCompat.getColor(this, R.color.white);
            } else {
                snackBarBackgroundColor = ContextCompat.getColor(this, R.color.primary_500_light);
                snackBarTextColor = ContextCompat.getColor(this, R.color.black);
            }

            Snackbar.make(
                            findViewById(R.id.content_main),
                            R.string.grant_failed,
                            BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setAction(
                            R.string.grant,
                            v ->
                                    startActivity(
                                            new Intent(
                                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.parse(String.format("package:%s", getPackageName())))))
                    .setBackgroundTint(snackBarBackgroundColor)
                    .setTextColor(snackBarTextColor)
                    .show();

        }
    }

    public void requestStoragePermission(boolean isFirstStart, @NonNull final OnPermissionGranted onPermissionGranted) {
        final AlertDialog.Builder rationaleDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.grant_permission)
                .setMessage(R.string.grant_permission_msg_storage)
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> {
                    //dialog.dismiss();

                    //Toast.makeText(PermissionsActivity.this, "qui dovremmo uscire dallapp", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setCancelable(false);

        onPermissionGrantedCallbacks[PERMISSION_CODE_STORAGE] = onPermissionGranted;
        requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_CODE_STORAGE, rationaleDialog, isFirstStart);
    }

    public void requestAllFilesAccess(@NonNull final OnPermissionGranted onPermissionGranted) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {

            new AlertDialog.Builder(this)
                    .setTitle(R.string.grant_permission)
                    .setMessage(R.string.grant_permission_msg_full_storage)
                    .setPositiveButton(R.string.grant, (dialog, which) -> {

                        onPermissionGrantedCallbacks[PERMISSION_CODE_ALL_FILES] = onPermissionGranted;

                        try {
                            Intent intent =
                                    new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                            .setData(Uri.parse("package:" + getPackageName()));
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(PermissionsActivity.this, R.string.grant_failed, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.button_cancel, (dialog, which) -> {
                        dialog.dismiss();
                        Toast.makeText(PermissionsActivity.this, R.string.grant_failed, Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setCancelable(false)
                    .create().show();
        }
    }

    public void requestApkInstallPermission(boolean isFirstStart, @NonNull final OnPermissionGranted onPermissionGranted) {
        final AlertDialog.Builder rationaleDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.grant_permission)
                .setMessage(R.string.grant_permission_msg_apk)
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false);

        onPermissionGrantedCallbacks[PERMISSION_CODE_INSTALL_APK] = onPermissionGranted;
        requestPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES, PERMISSION_CODE_INSTALL_APK, rationaleDialog, isFirstStart);
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Toast.makeText(this, "dentro result: codice = " + requestCode, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "dentro result: permission granted = " + permissionIsGranted(grantResults), Toast.LENGTH_SHORT).show();

        switch (requestCode) {
            case PERMISSION_CODE_STORAGE: {
                if (permissionIsGranted(grantResults)) {
                    Utils.enableScreenRotation(this);
                    Toast.makeText(this, R.string.grant_successed, Toast.LENGTH_SHORT).show();

                    onPermissionGrantedCallbacks[PERMISSION_CODE_STORAGE].onPermissionGranted();
                    onPermissionGrantedCallbacks[PERMISSION_CODE_STORAGE] = null;
                } else {
                    //Log.e("DEBUG", "Non ho ottenuto i permessi per lo storage", null);
                    //Toast.makeText(this, R.string.grant_failed_exit, Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "Non ho ottenuto i permessi per lo storage. Chiedo di nuobo", Toast.LENGTH_SHORT).show();
                    requestStoragePermission(false, onPermissionGrantedCallbacks[PERMISSION_CODE_STORAGE]);
                    //finishAffinity();
                }
            }

            case PERMISSION_CODE_INSTALL_APK:
                if (permissionIsGranted(grantResults)) {
                    onPermissionGrantedCallbacks[PERMISSION_CODE_INSTALL_APK].onPermissionGranted();
                    onPermissionGrantedCallbacks[PERMISSION_CODE_INSTALL_APK] = null;
                }
        }
    }

    private boolean permissionIsGranted(int[] grantResults) {
        return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }


    public interface OnPermissionGranted {
        void onPermissionGranted();
    }

    private boolean isNightModeEnabled() {
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:

            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                return true;

            case Configuration.UI_MODE_NIGHT_NO:
                return false;
        }

        return false;
    }
}