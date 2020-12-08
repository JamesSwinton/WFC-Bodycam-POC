package com.zebra.jamesswinton.wfcbodycampoc.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.zebra.jamesswinton.wfcbodycampoc.Application;
import com.zebra.jamesswinton.wfcbodycampoc.R;
import com.zebra.jamesswinton.wfcbodycampoc.RecordVideoService;
import com.zebra.jamesswinton.wfcbodycampoc.utilities.FileHelper;
import com.zebra.jamesswinton.wfcbodycampoc.utilities.PermissionsHelper;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements PermissionsHelper.OnPermissionsResultListener {

    // Permissions
    private PermissionsHelper mPermissionsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPermissionsHelper = new PermissionsHelper(this, this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPermissionsHelper.onActivityResult();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionsHelper.onRequestPermissionsResult();
    }

    @Override
    public void onPermissionsGranted() {
        // Load Config
        try {
            Application.mConfig = FileHelper.loadConfigToMemoryFromFile(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Launch Service
        Intent recordVideoService = new Intent(this, RecordVideoService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(recordVideoService);
        } else {
            startService(recordVideoService);
        }

        // Exit App
        finish();
    }

}