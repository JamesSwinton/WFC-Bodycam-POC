package com.zebra.jamesswinton.wfcbodycampoc;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.widget.Toast;

import com.zebra.jamesswinton.wfcbodycampoc.managers.FloatingViewManager;
import com.zebra.jamesswinton.wfcbodycampoc.utilities.NotificationHelper;

import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.ACTION_ALERT_FAILED;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.ACTION_ALERT_SENT;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.ACTION_CAPTURE_FAILURE;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.ACTION_HIDE_OVERLAY;

public class RecordVideoService extends Service {

    // Managers
    private FloatingViewManager mFloatingViewManager = null;

    @Override
    public void onCreate() {
        super.onCreate();
        // Init Managers
        mFloatingViewManager = new FloatingViewManager(this);

        // Init BC Receiver
        IntentFilter mReceiverIntentFilter = new IntentFilter();
        mReceiverIntentFilter.addAction(ACTION_HIDE_OVERLAY);
        mReceiverIntentFilter.addAction(ACTION_ALERT_SENT);
        mReceiverIntentFilter.addAction(ACTION_ALERT_FAILED);
        mReceiverIntentFilter.addAction(ACTION_CAPTURE_FAILURE);
        registerReceiver(mReceiver, mReceiverIntentFilter);

        // Start Foreground
        startForeground(NotificationHelper.NOTIFICATION_ID,
                NotificationHelper.createNotification(this));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        mFloatingViewManager.removeFloatingView();
    }

    /**
     * BC Receiver
     */

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (intentAction != null) {
                switch (intentAction) {
                    case ACTION_HIDE_OVERLAY:
                        mFloatingViewManager.removeFloatingView();
                        break;
                    case ACTION_CAPTURE_FAILURE:
                        mFloatingViewManager.addFloatingView();
                        break;
                    case ACTION_ALERT_SENT:
                        mFloatingViewManager.addFloatingView();
                        Toast.makeText(RecordVideoService.this, "Alert Sent",
                                Toast.LENGTH_LONG).show();
                        break;
                    case ACTION_ALERT_FAILED:
                        mFloatingViewManager.addFloatingView();
                        Toast.makeText(RecordVideoService.this, "Alert Error",
                                Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }
    };

    /**
     * Unsupported
     */

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}