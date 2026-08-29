package com.example.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * CameraPermissionManager utility class for managing and checking camera permissions.
 */
class CameraPermissionManager(
    private val context: Context
) {
    companion object {
        const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }

    /**
     * Checks whether the camera permission is currently granted.
     */
    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            CAMERA_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Requests the camera permission from an Activity.
     */
    fun requestCameraPermission(activity: Activity, requestCode: Int = CAMERA_PERMISSION_REQUEST_CODE) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(CAMERA_PERMISSION),
            requestCode
        )
    }

    /**
     * Checks if the app should show a rationale UI before requesting the permission.
     */
    fun shouldShowRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            CAMERA_PERMISSION
        )
    }

    /**
     * Opens the application's details/settings screen so the user can grant permission manually.
     */
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
