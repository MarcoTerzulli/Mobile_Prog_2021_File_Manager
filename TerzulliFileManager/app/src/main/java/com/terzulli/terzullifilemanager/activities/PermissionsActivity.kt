package com.terzulli.terzullifilemanager.activities

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.terzulli.terzullifilemanager.R

abstract class PermissionsActivity : AppCompatActivity(), OnRequestPermissionsResultCallback {
    private val permissionListLenght = 3
    private val permissionCodeStorage = 0
    private val permissionCodeInstallApk = 1
    private val permissionCodeAllFiles = 2
    private val onPermissionGrantedCallbacks =
        arrayOfNulls<OnPermissionGranted>(permissionListLenght)

    fun checkStoragePermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission(
        permission: String,
        permissionCode: Int,
        rationaleDialog: AlertDialog.Builder,
        isFirstStart: Boolean
    ) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            rationaleDialog
                .setPositiveButton(R.string.grant) { thisDialog: DialogInterface, _: Int ->
                    ActivityCompat.requestPermissions(this, arrayOf(permission), permissionCode)
                    thisDialog.dismiss()
                }
                .create().show()
        } else if (isFirstStart) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), permissionCode)
        } else {
            val snackBarBackgroundColor: Int
            val snackBarTextColor: Int

            if (isNightModeEnabled) {
                snackBarBackgroundColor = ContextCompat.getColor(this, R.color.primary_500_night)
                snackBarTextColor = ContextCompat.getColor(this, R.color.white)
            } else {
                snackBarBackgroundColor = ContextCompat.getColor(this, R.color.primary_500_light)
                snackBarTextColor = ContextCompat.getColor(this, R.color.black)
            }

            Snackbar.make(
                findViewById(R.id.content_main),
                R.string.grant_failed,
                BaseTransientBottomBar.LENGTH_INDEFINITE
            )
                .setAction(
                    R.string.grant
                ) {
                    startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse(String.format("package:%s", packageName))
                        )
                    )
                }
                .setBackgroundTint(snackBarBackgroundColor)
                .setTextColor(snackBarTextColor)
                .show()
        }
    }

    fun requestStoragePermission(isFirstStart: Boolean, onPermissionGranted: OnPermissionGranted) {
        val rationaleDialog = AlertDialog.Builder(this)
            .setTitle(R.string.grant_permission)
            .setMessage(R.string.grant_permission_msg_storage)
            .setNegativeButton(R.string.button_cancel) { _: DialogInterface?, _: Int ->
                finish()
            }
            .setCancelable(false)

        onPermissionGrantedCallbacks[permissionCodeStorage] = onPermissionGranted

        requestPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            permissionCodeStorage,
            rationaleDialog,
            isFirstStart
        )
    }

    fun requestAllFilesAccess(onPermissionGranted: OnPermissionGranted) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            AlertDialog.Builder(this)
                .setTitle(R.string.grant_permission)
                .setMessage(R.string.grant_permission_msg_full_storage)
                .setPositiveButton(R.string.grant) { _: DialogInterface?, _: Int ->
                    onPermissionGrantedCallbacks[permissionCodeAllFiles] = onPermissionGranted

                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                            .setData(Uri.parse("package:$packageName"))
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@PermissionsActivity,
                            R.string.grant_failed,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .setNegativeButton(R.string.button_cancel) { dialog: DialogInterface, _: Int ->
                    dialog.dismiss()
                    Toast.makeText(
                        this@PermissionsActivity,
                        R.string.grant_failed,
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .setCancelable(false)
                .create().show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            permissionCodeStorage -> if (permissionIsGranted(grantResults)) {
                onPermissionGrantedCallbacks[permissionCodeStorage]!!.onPermissionGranted()
                onPermissionGrantedCallbacks[permissionCodeStorage] = null
            } else {
                requestStoragePermission(
                    false,
                    onPermissionGrantedCallbacks[permissionCodeStorage]!!
                )
            }

            permissionCodeInstallApk -> if (permissionIsGranted(grantResults)) {
                onPermissionGrantedCallbacks[permissionCodeInstallApk]!!.onPermissionGranted()
                onPermissionGrantedCallbacks[permissionCodeInstallApk] = null
            }
        }
    }

    private fun permissionIsGranted(grantResults: IntArray): Boolean {
        return grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
    }

    private val isNightModeEnabled: Boolean
        get() {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES, Configuration.UI_MODE_NIGHT_UNDEFINED -> return true
                Configuration.UI_MODE_NIGHT_NO -> return false
            }
            return false
        }

    interface OnPermissionGranted {
        fun onPermissionGranted()
    }
}