package com.zebra.jamesswinton.wfcbodycampoc.managers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.databinding.DataBindingUtil;

import com.zebra.jamesswinton.wfcbodycampoc.activities.CaptureActivity;
import com.zebra.jamesswinton.wfcbodycampoc.R;
import com.zebra.jamesswinton.wfcbodycampoc.activities.SendAlertActivity;
import com.zebra.jamesswinton.wfcbodycampoc.databinding.OverlayFloatingWidgetBinding;
import com.zebra.jamesswinton.wfcbodycampoc.utilities.AnimationHelper;
import com.zebra.jamesswinton.wfcbodycampoc.utilities.Type;

import static android.content.Context.WINDOW_SERVICE;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.ACTION_HIDE_OVERLAY;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.EXTRA_CAPTURE_TYPE;

public class FloatingViewManager implements View.OnClickListener, View.OnTouchListener {

    // Context
    private Context mCx;

    // Views
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowManagerLayoutParams;
    private OverlayFloatingWidgetBinding mFloatingViewBinding;

    // Movement holders
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;
    private static final int MAX_CLICK_DURATION = 200;
    private long startClickTime;

    public FloatingViewManager(Context cx) {
        // Init Floating View
        initFloatingView(cx);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initFloatingView(Context cx) {
        // Init CX
        this.mCx = cx;
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(mCx, R.style.OverlayTheme);

        // Inflate the floating view layout we created
        mFloatingViewBinding = DataBindingUtil.inflate(LayoutInflater.from(contextThemeWrapper),
                R.layout.overlay_floating_widget, null, false);

        // Create View Layout Params
        mWindowManagerLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        mWindowManagerLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowManagerLayoutParams.x = 0;
        mWindowManagerLayoutParams.y = 100;

        // Add the view to the window
        mWindowManager = (WindowManager) cx.getSystemService(WINDOW_SERVICE);
        addFloatingView();

        // Init Sub View Click & Touch Listeners
        mFloatingViewBinding.sendAlert.setOnClickListener(this);
        mFloatingViewBinding.sendVideo.setOnClickListener(this);
        mFloatingViewBinding.sendImage.setOnClickListener(this);
        mFloatingViewBinding.rootContainer.setOnTouchListener(this);

        // Init Animation Settings
        AnimationHelper.init(mFloatingViewBinding.sendAlertContainer);
        AnimationHelper.init(mFloatingViewBinding.sendImageContainer);
        AnimationHelper.init(mFloatingViewBinding.sendVideoContainer);
    }

    public void addFloatingView() {
        if (mWindowManager != null && mFloatingViewBinding.getRoot() != null) {
            mWindowManager.addView(mFloatingViewBinding.getRoot(), mWindowManagerLayoutParams);
        }
    }

    public void removeFloatingView() {
        if (mFloatingViewBinding.getRoot() != null) {
            mWindowManager.removeView(mFloatingViewBinding.getRoot());
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        // Hide Container
        mRotated = AnimationHelper.rotateFab(mFloatingViewBinding.floatingButton, !mRotated);
        AnimationHelper.init(mFloatingViewBinding.sendAlertContainer);
        AnimationHelper.init(mFloatingViewBinding.sendImageContainer);
        AnimationHelper.init(mFloatingViewBinding.sendVideoContainer);

        // Request Overlay be Hidden by Service
        mCx.sendBroadcast(new Intent(ACTION_HIDE_OVERLAY));

        // Handle Click
        switch (v.getId()) {
            case R.id.send_alert:
                Intent sendTextAlert = new Intent(mCx, SendAlertActivity.class);
                sendTextAlert.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendTextAlert.putExtra(EXTRA_CAPTURE_TYPE, Type.MESSAGE);
                mCx.startActivity(sendTextAlert);
                break;
            case R.id.send_video:
                Intent captureVideo = new Intent(mCx, CaptureActivity.class);
                captureVideo.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                captureVideo.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                captureVideo.putExtra(EXTRA_CAPTURE_TYPE, Type.VIDEO.name());
                mCx.startActivity(captureVideo);
                break;
            case R.id.send_image:
                Intent captureImage = new Intent(mCx, CaptureActivity.class);
                captureImage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                captureImage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                captureImage.putExtra(EXTRA_CAPTURE_TYPE, Type.IMAGE.name());
                mCx.startActivity(captureImage);
                break;
        }
    }

    private boolean mRotated = false;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                startClickTime = System.currentTimeMillis();
                initialX = mWindowManagerLayoutParams.x;
                initialY = mWindowManagerLayoutParams.y;
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE:
                mWindowManagerLayoutParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                mWindowManagerLayoutParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                mWindowManager.updateViewLayout(mFloatingViewBinding.getRoot(), mWindowManagerLayoutParams);
                break;
            case MotionEvent.ACTION_UP: {
                long clickDuration = System.currentTimeMillis() - startClickTime;
                if (clickDuration < MAX_CLICK_DURATION) {
                    // Animate
                    mRotated = AnimationHelper.rotateFab(mFloatingViewBinding.floatingButton, !mRotated);

                    // display Sub Views
                    if(mRotated) {
                        AnimationHelper.showIn(mFloatingViewBinding.sendAlertContainer);
                        AnimationHelper.showIn(mFloatingViewBinding.sendImageContainer);
                        AnimationHelper.showIn(mFloatingViewBinding.sendVideoContainer);
                    } else {
                        AnimationHelper.showOut(mFloatingViewBinding.sendAlertContainer);
                        AnimationHelper.showOut(mFloatingViewBinding.sendImageContainer);
                        AnimationHelper.showOut(mFloatingViewBinding.sendVideoContainer);
                    }

                }
            }
        }
        return true;
    }
}
