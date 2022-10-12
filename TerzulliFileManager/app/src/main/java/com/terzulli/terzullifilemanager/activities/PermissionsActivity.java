package com.terzulli.terzullifilemanager.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.utils.Utils;

public abstract class PermissionsActivity extends AppCompatActivity
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

        //Utils.disableScreenRotation(this);

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            rationaleDialog
                    .setPositiveButton(R.string.grant, (thisDialog, which) -> {
                        ActivityCompat.requestPermissions(this, new String[]{permission}, permissionCode);
                        thisDialog.dismiss();
                    })
                    .create().show();
        } else if (isFirstStart) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, permissionCode);
        } else {

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

        switch (requestCode) {
            case PERMISSION_CODE_STORAGE:
                if (permissionIsGranted(grantResults)) {
                    //Utils.enableScreenRotation(this);

                    onPermissionGrantedCallbacks[PERMISSION_CODE_STORAGE].onPermissionGranted();
                    onPermissionGrantedCallbacks[PERMISSION_CODE_STORAGE] = null;
                } else {
                    requestStoragePermission(false, onPermissionGrantedCallbacks[PERMISSION_CODE_STORAGE]);
                }
                break;

            case PERMISSION_CODE_INSTALL_APK:
                if (permissionIsGranted(grantResults)) {
                    onPermissionGrantedCallbacks[PERMISSION_CODE_INSTALL_APK].onPermissionGranted();
                    onPermissionGrantedCallbacks[PERMISSION_CODE_INSTALL_APK] = null;
                }
                break;
        }
    }

    private boolean permissionIsGranted(int[] grantResults) {
        return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
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

    public interface OnPermissionGranted {
        void onPermissionGranted();
    }

}