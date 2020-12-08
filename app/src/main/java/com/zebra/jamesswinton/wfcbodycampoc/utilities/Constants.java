package com.zebra.jamesswinton.wfcbodycampoc.utilities;

public class Constants {
    // Intent Actions
    public static final String ACTION_HIDE_OVERLAY = "com.zebra.bodycampoc.HIDE_OVERLAY";
    public static final String ACTION_CAPTURE_FAILURE = "com.zebra.bodycampoc.CAPTURE_FAILURE";
    public static final String ACTION_ALERT_SENT = "com.zebra.bodycampoc.ALERT_SENT";
    public static final String ACTION_ALERT_FAILED = "com.zebra.bodycampoc.ALERT_FAILED";

    // Intent Extras
    public static final String EXTRA_CAPTURE_URI = "CAPTURE_URI";
    public static final String EXTRA_CAPTURE_TYPE = "CAPTURE_TYPE";

    // Folder & File Names
    public static final String FOLDER_NAME = "WFC Body Cam POC";

    // File Names
    public static final String VIDEO_FILE_NAME_PREFIX = "Body Cam Video - ";
    public static final String IMAGE_FILE_NAME_PREFIX = "Body Cam Capture - ";
}
