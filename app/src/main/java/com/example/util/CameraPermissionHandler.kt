package com.example.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Camera Permission Status representation
 */
sealed class CameraPermissionStatus {
    object Granted : CameraPermissionStatus()
    object Denied : CameraPermissionStatus()
    object ShowRationale : CameraPermissionStatus()
    object PermanentlyDenied : CameraPermissionStatus()
}

/**
 * Service class to handle and check camera permissions across the application.
 */
class CameraPermissionService(
    private val context: Context
) {
    companion object {
        const val CAMERA_PERMISSION = Manifest.permission.CAMERA
    }

    /**
     * Checks if camera permission is currently granted.
     */
    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            CAMERA_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Determines whether rationale should be shown for camera permission.
     */
    fun shouldShowRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            CAMERA_PERMISSION
        )
    }

    /**
     * Opens the app settings page so the user can grant permission manually if permanently denied.
     */
    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

/**
 * Composable helper state holder for managing camera permission requests seamlessly in Jetpack Compose UI.
 */
@Composable
fun rememberCameraPermissionState(
    onPermissionResult: ((Boolean) -> Unit)? = null
): CameraPermissionState {
    val context = LocalContext.current
    val permissionService = remember(context) { CameraPermissionService(context) }
    var hasPermission by remember { mutableStateOf(permissionService.hasCameraPermission()) }
    var showRationaleDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        onPermissionResult?.invoke(isGranted)
        if (!isGranted) {
            val activity = context as? Activity
            if (activity != null && permissionService.shouldShowRationale(activity)) {
                showRationaleDialog = true
            }
        }
    }

    return remember(hasPermission, showRationaleDialog) {
        CameraPermissionState(
            hasPermission = hasPermission,
            showRationaleDialog = showRationaleDialog,
            permissionService = permissionService,
            launcher = launcher,
            dismissRationale = { showRationaleDialog = false }
        )
    }
}

class CameraPermissionState(
    val hasPermission: Boolean,
    val showRationaleDialog: Boolean,
    private val permissionService: CameraPermissionService,
    private val launcher: ManagedActivityResultLauncher<String, Boolean>,
    val dismissRationale: () -> Unit
) {
    fun requestPermission() {
        if (!hasPermission) {
            launcher.launch(CameraPermissionService.CAMERA_PERMISSION)
        }
    }

    fun openSettings() {
        permissionService.openAppSettings()
    }
}
