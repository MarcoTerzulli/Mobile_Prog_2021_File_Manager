package com.terzulli.terzullifilemanager.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.utils.Utils;

public class PermissionsActivity extends AppCompatActivity {

    public static final int PERMISSION_CODE_STORAGE = 0, PERMISSION_CODE_INSTALL_APK = 1,
            PERMISSION_CODE_ALL_FILES = 2;

    public void checkForSystemPermissions() {
        if (!checkStoragePermission()) {
            requestStoragePermission();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requestAllFilesAccess();
        }
    }

    public boolean checkStoragePermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission(final String permission,
                                   final int permissionCode,
                                   @NonNull final AlertDialog rationaleDialog,
                                   boolean isFirstStart) {
        
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle(R.string.grant_permission)
                    .setMessage(R.string.grant_permission_msg_storage)
                    .setPositiveButton(R.string.grant, (dialog, which) -> requestStoragePermissionNoRationale())
                    .setNegativeButton(R.string.button_cancel, (dialog, which) -> {
                        //dialog.dismiss();

                        //Toast.makeText(PermissionsActivity.this, "qui dovremmo uscire dallapp", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setCancelable(false)
                    .create().show();
        } else {
            requestStoragePermissionNoRationale();
        }
    }

    private void requestStoragePermissionNoRationale() {
        Utils.disableScreenRotation(PermissionsActivity.this);
        ActivityCompat.requestPermissions(PermissionsActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        Utils.enableScreenRotation(PermissionsActivity.this);
    }

    private void requestAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {

            new AlertDialog.Builder(this)
            .setTitle(R.string.grant_permission)
            .setMessage(R.string.grant_permission_msg_full_storage)
            .setPositiveButton(R.string.grant, (dialog, which) -> {

                try {
                    Intent intent =
                            new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                                    .setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(PermissionsActivity.this, R.string.grant_failed_exit, Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.button_cancel, (dialog, which) -> {
                dialog.dismiss();

                Toast.makeText(PermissionsActivity.this, "qui dovremmo uscire dallapp", Toast.LENGTH_SHORT).show();
                finish();
            })
            .setCancelable(false)
            .create().show();
        }
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

                    Toast.makeText(this, R.string.grant_successed, Toast.LENGTH_SHORT).show();

                    //TODO gestione di eventuale callback?
                } else {
                    //Log.e("DEBUG", "Non ho ottenuto i permessi per lo storage", null);
                    //Toast.makeText(this, R.string.grant_failed_exit, Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "Non ho ottenuto i permessi per lo storage. Chiedo di nuobo", Toast.LENGTH_SHORT).show();
                    requestStoragePermission();
                    //finishAffinity();
                }
            }

            case PERMISSION_CODE_INSTALL_APK:
                Utils.enableScreenRotation(this);
                //TODO gestione di eventuale callback?
                return;
            default:
                return;
        }
    }

    private boolean permissionIsGranted(int[] grantResults) {
        return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    /*@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }*/
}