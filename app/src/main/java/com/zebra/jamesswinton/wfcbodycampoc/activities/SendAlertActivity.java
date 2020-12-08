package com.zebra.jamesswinton.wfcbodycampoc.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.zebra.jamesswinton.wfcbodycampoc.OnAlertSentListener;
import com.zebra.jamesswinton.wfcbodycampoc.R;
import com.zebra.jamesswinton.wfcbodycampoc.RecordVideoService;
import com.zebra.jamesswinton.wfcbodycampoc.databinding.ActivitySendAlertBinding;
import com.zebra.jamesswinton.wfcbodycampoc.managers.IWGManager;
import com.zebra.jamesswinton.wfcbodycampoc.utilities.Type;

import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.ACTION_ALERT_FAILED;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.ACTION_ALERT_SENT;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.ACTION_CAPTURE_FAILURE;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.EXTRA_CAPTURE_TYPE;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.EXTRA_CAPTURE_URI;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Type.IMAGE;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Type.VIDEO;

public class SendAlertActivity extends AppCompatActivity implements View.OnClickListener, OnAlertSentListener {

    // UI
    private ActivitySendAlertBinding mDataBinding;

    // Managers
    private IWGManager mIWGManager = null;

    // Alert Variables
    private Type mAlertType = null;
    private Uri mCaptureUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
        mDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_send_alert);

        // Init Manager
        mIWGManager = new IWGManager(SendAlertActivity.this, this);

        // Init Click Listeners
        mDataBinding.cancelButton.setOnClickListener(this);
        mDataBinding.sendButton.setOnClickListener(this);

        // Init Alert Variables
        setAlertVariables();

        // Init Layout Params
        FrameLayout.LayoutParams captureContentLayoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        captureContentLayoutParams.gravity = Gravity.CENTER;

        // Validate Capture Content
        if (mAlertType == VIDEO || mAlertType == IMAGE) {
            if (mCaptureUri == null) {
                throw new NullPointerException("Capture URI Null");
            }
        }

        // Init Capture Content
        switch (mAlertType) {
            case MESSAGE:
                mDataBinding.captureContainer.setVisibility(View.GONE);
                break;
            case IMAGE:
                ImageView imageView = new ImageView(this);
                imageView.setLayoutParams(captureContentLayoutParams);
                imageView.setImageURI(mCaptureUri);
                mDataBinding.captureContainer.addView(imageView);
                break;
            case VIDEO:
                VideoView videoView = new VideoView(this);
                videoView.setLayoutParams(captureContentLayoutParams);
                videoView.setVideoURI(mCaptureUri);
                MediaController mediaController = new MediaController(this);
                mediaController.setAnchorView(videoView);
                videoView.setMediaController(mediaController);
                mDataBinding.captureContainer.addView(videoView);
                break;
        }
    }

    private void setAlertVariables() {
        Intent launchIntent = getIntent();
        if (getIntent() != null) {
            mAlertType = launchIntent.getSerializableExtra(EXTRA_CAPTURE_TYPE) == null
                    ? Type.MESSAGE : (Type) launchIntent.getSerializableExtra(EXTRA_CAPTURE_TYPE);
            mCaptureUri = launchIntent.getStringExtra(EXTRA_CAPTURE_URI) == null
                    ? null : Uri.parse(launchIntent.getStringExtra(EXTRA_CAPTURE_URI));
        } else {
            Log.e(this.getClass().getName(), "Alert Failed");
            sendBroadcast(new Intent(ACTION_ALERT_FAILED));
        }
    }


    /**
     * Click Handler
     */

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_button:
                Log.e(this.getClass().getName(), "Alert Canceled");
                sendBroadcast(new Intent(ACTION_ALERT_FAILED));
                finish();
                break;
            case R.id.send_button:
                mIWGManager.sendAlert(mAlertType, mCaptureUri,
                        mDataBinding.messageEdittext.getText().toString());
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If we've received a touch notification that the user has touched
        // outside the app, finish the activity.
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            Log.e(this.getClass().getName(), "Alert Canceled");
            sendBroadcast(new Intent(ACTION_ALERT_FAILED));
            finish();
            return true;
        }

        // Delegate everything else to Activity.
        return super.onTouchEvent(event);
    }

    /**
     * Alert Status Callbacks
     */

    @Override
    public void onAlertSent() {
        sendBroadcast(new Intent(ACTION_ALERT_SENT));
        finish();
    }

    @Override
    public void onError(String e) {
        sendBroadcast(new Intent(ACTION_ALERT_FAILED));
        finish();
    }
}