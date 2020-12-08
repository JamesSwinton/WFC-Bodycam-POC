package com.zebra.jamesswinton.wfcbodycampoc.utilities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class PermissionsHelper {

    // Constants
    public static final int PERMISSIONS_REQUEST_CODE = 1000;
    public static final int OVERLAY_REQUEST_CODE = 2000;
    private static final String[] PERMISSIONS = {
            INTERNET, CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE
    };

    // Variables
    private final Activity mActivity;
    private final OnPermissionsResultListener mOnPermissionsResultListener;

    // Interfaces
    public interface OnPermissionsResultListener {
        void onPermissionsGranted();
    }

    public PermissionsHelper(@NonNull Activity activity,
                             @NonNull OnPermissionsResultListener onPermissionsResultListener) {
        this.mActivity = activity;
        this.mOnPermissionsResultListener = onPermissionsResultListener;
        forcePermissionsUntilGranted();
    }

    private void forcePermissionsUntilGranted() {
        if (checkOverlayPermission() && checkStandardPermissions()) {
            mOnPermissionsResultListener.onPermissionsGranted();
        } else if (!checkStandardPermissions()) {
            requestStandardPermission();
        } else if (!checkOverlayPermission()){
            requestOverlayPermission();
        }
    }

    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(mActivity);
        } return true;
    }

    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + mActivity.getPackageName()));
        mActivity.startActivityForResult(intent, OVERLAY_REQUEST_CODE);
    }

    private boolean checkStandardPermissions() {
        boolean permissionsGranted = true;
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(mActivity, permission) != PERMISSION_GRANTED) {
                permissionsGranted = false;
                break;
            }
        }
        return permissionsGranted;
    }

    private void requestStandardPermission() {
        ActivityCompat.requestPermissions(mActivity, PERMISSIONS, PERMISSIONS_REQUEST_CODE);
    }

    public void onRequestPermissionsResult() {
        forcePermissionsUntilGranted();
    }

    public void onActivityResult() {
        forcePermissionsUntilGranted();
    }

}
