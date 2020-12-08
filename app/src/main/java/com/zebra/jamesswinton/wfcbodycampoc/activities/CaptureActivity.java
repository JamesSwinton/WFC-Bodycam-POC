package com.zebra.jamesswinton.wfcbodycampoc.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.zebra.jamesswinton.wfcbodycampoc.R;
import com.zebra.jamesswinton.wfcbodycampoc.utilities.FileHelper;
import com.zebra.jamesswinton.wfcbodycampoc.utilities.Type;

import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.ACTION_CAPTURE_FAILURE;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.ACTION_HIDE_OVERLAY;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.EXTRA_CAPTURE_TYPE;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.EXTRA_CAPTURE_URI;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Type.IMAGE;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Type.VIDEO;

public class CaptureActivity extends AppCompatActivity {

    // Intent Ids
    private static final int CAPTURE_VIDEO_REQUEST = 100;
    private static final int CAPTURE_IMAGE_REQUEST = 200;

    // Capture Type && Uri
    private Type mCaptureType;
    private Uri mOutputUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        // Get Capture Type from launch Intent
        mCaptureType = getCaptureType();

        // Get Output URI
        mOutputUri = FileHelper.getOutputUri(this, mCaptureType);

        // Capture Video
        if (mCaptureType == VIDEO) {
            Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mOutputUri);
                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); // Quality Low
                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5); // 5 Secs
                takeVideoIntent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 1098304L); // 1MB
                startActivityForResult(takeVideoIntent, CAPTURE_VIDEO_REQUEST);
            }
        } else if (mCaptureType == IMAGE) {
            Intent captureImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (captureImageIntent.resolveActivity(getPackageManager()) != null) {
                captureImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, mOutputUri);
                startActivityForResult(captureImageIntent, CAPTURE_IMAGE_REQUEST);
            }
        }
    }

    private Type getCaptureType() {
        Intent launchIntent = getIntent();
        if (launchIntent != null) {
            String captureType = launchIntent.getStringExtra(EXTRA_CAPTURE_TYPE);
            return captureType.equals(IMAGE.name()) ? IMAGE : VIDEO;
        } else {
            return VIDEO;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAPTURE_IMAGE_REQUEST:
                    Log.i(this.getClass().getName(), "Image Captured");
                    Intent sendImageCapture = new Intent(this, SendAlertActivity.class);
                    sendImageCapture.putExtra(EXTRA_CAPTURE_TYPE, IMAGE);
                    sendImageCapture.putExtra(EXTRA_CAPTURE_URI, mOutputUri.toString());
                    startActivity(sendImageCapture);
                    break;
                case CAPTURE_VIDEO_REQUEST:
                    Log.i(this.getClass().getName(), "Video Captured");
                    Intent sendVideoCapture = new Intent(this, SendAlertActivity.class);
                    sendVideoCapture.putExtra(EXTRA_CAPTURE_TYPE, VIDEO);
                    sendVideoCapture.putExtra(EXTRA_CAPTURE_URI, mOutputUri.toString());
                    startActivity(sendVideoCapture);
                    break;
                default:
                    Log.d(this.getClass().getName(), "Unknown Result");
            }
        } else {
            Log.e(this.getClass().getName(), "Capture Failed");
            sendBroadcast(new Intent(ACTION_CAPTURE_FAILURE));
        }

        finish();
    }
}