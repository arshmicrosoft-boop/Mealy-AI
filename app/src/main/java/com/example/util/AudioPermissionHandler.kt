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
 * Service class to handle and check microphone audio permissions.
 */
class AudioPermissionService(
    private val context: Context
) {
    companion object {
        const val AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    }

    /**
     * Checks if audio record permission is granted.
     */
    fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            AUDIO_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Determines whether rationale should be shown for audio permission.
     */
    fun shouldShowRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            AUDIO_PERMISSION
        )
    }

    /**
     * Opens application details settings if permission is permanently denied.
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
 * Composable helper state holder for managing audio recording permission.
 */
@Composable
fun rememberAudioPermissionState(
    onPermissionResult: ((Boolean) -> Unit)? = null
): AudioPermissionState {
    val context = LocalContext.current
    val permissionService = remember(context) { AudioPermissionService(context) }
    var hasPermission by remember { mutableStateOf(permissionService.hasAudioPermission()) }
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
        AudioPermissionState(
            hasPermission = hasPermission,
            showRationaleDialog = showRationaleDialog,
            permissionService = permissionService,
            launcher = launcher,
            dismissRationale = { showRationaleDialog = false }
        )
    }
}

class AudioPermissionState(
    val hasPermission: Boolean,
    val showRationaleDialog: Boolean,
    private val permissionService: AudioPermissionService,
    private val launcher: ManagedActivityResultLauncher<String, Boolean>,
    val dismissRationale: () -> Unit
) {
    fun requestPermission() {
        if (!hasPermission) {
            launcher.launch(AudioPermissionService.AUDIO_PERMISSION)
        }
    }

    fun openSettings() {
        permissionService.openAppSettings()
    }
}
