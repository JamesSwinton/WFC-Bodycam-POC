package com.zebra.jamesswinton.wfcbodycampoc.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.zebra.jamesswinton.wfcbodycampoc.OnAlertSentListener;
import com.zebra.jamesswinton.wfcbodycampoc.networking.AnnouncerApi;
import com.zebra.jamesswinton.wfcbodycampoc.networking.RetrofitInstance;
import com.zebra.jamesswinton.wfcbodycampoc.pojos.AnnouncerResponse;
import com.zebra.jamesswinton.wfcbodycampoc.utilities.CustomDialog;
import com.zebra.jamesswinton.wfcbodycampoc.utilities.FileHelper;
import com.zebra.jamesswinton.wfcbodycampoc.utilities.Type;

import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.zebra.jamesswinton.wfcbodycampoc.Application.mConfig;

public class IWGManager implements Callback<AnnouncerResponse> {

    // Variables
    private int mEid;
    private Context mCx;
    private String mApiKey;
    private AnnouncerApi mAnnouncerApi;

    // dialog
    private AlertDialog mProgressDialog;

    // Callback
    private OnAlertSentListener mOnAlertSentListener;

    public IWGManager(Context cx, OnAlertSentListener onAlertSentListener) {
        this.mCx = cx;
        this.mEid = mConfig.getEid();
        this.mApiKey = mConfig.getApiPassword();
        this.mOnAlertSentListener = onAlertSentListener;
        this.mAnnouncerApi = RetrofitInstance.getInstance(mConfig.getApiUrl())
                .create(AnnouncerApi.class);
        this.mProgressDialog = CustomDialog.buildLoadingDialog(cx,
                "Sending Alert...", false);
    }

    public void sendAlert(@NonNull Type type, @Nullable Uri uri, @Nullable String message) {
        // Show Dialog
        mProgressDialog.show();

        // Send Alert
        switch (type) {
            case MESSAGE:
                mAnnouncerApi.sendMessage(mApiKey, message, mEid).enqueue(this);
                break;
            case IMAGE:
                try {
                    sendImage(type, uri, message);
                } catch (IOException e) {
                    e.printStackTrace();
                    mOnAlertSentListener.onError(e.getMessage());
                }
                break;
            case VIDEO:
                try {
                    sendVideo(uri, message);
                } catch (IOException e) {
                    e.printStackTrace();
                    mOnAlertSentListener.onError(e.getMessage());
                }
                break;
        }
    }

    private void sendImage(Type type, Uri fileUri, String message) throws IOException {
        // Get file from URI
        File imageFile = FileHelper.getFileFromUri(mCx, fileUri);

        // Create Reuqest Bodies
        RequestBody imageRequestBody = RequestBody.create(MediaType.parse("multipart/form-data"), imageFile);
        MultipartBody.Part fileBody = MultipartBody.Part.createFormData("file",
                imageFile.getName(), imageRequestBody);
        RequestBody messageBody = RequestBody.create(MediaType.parse("multipart/form-data"), message);
        RequestBody typeBody = RequestBody.create(MediaType.parse("multipart/form-data"), type.name().toUpperCase());

        // Send Request
        mAnnouncerApi.sendImage(mApiKey, fileBody, messageBody, typeBody, mEid)
                .enqueue(this);

    }

    private void sendVideo(Uri fileUri, String message) throws IOException {
        // Get file from URI
        File videoFile = FileHelper.getFileFromUri(mCx, fileUri);

        // Create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(
                MediaType.parse(mCx.getContentResolver().getType(fileUri)), videoFile);
        MultipartBody.Part fileBody = MultipartBody.Part.createFormData("file",
                videoFile.getName(), requestFile);

        // Execute the request
        mAnnouncerApi.sendVideo(mApiKey, fileBody, message, "VIDEO", mEid)
                .enqueue(this);
    }

    /**
     * Retrofit Callbacks
     */

    @Override
    public void onResponse(@NonNull Call<AnnouncerResponse> call,
                           @NonNull Response<AnnouncerResponse> response) {
        // Hide Dialog
        mProgressDialog.dismiss();

        // Handle Response
        if (response.isSuccessful()) {
            mOnAlertSentListener.onAlertSent();
        } else {
            mOnAlertSentListener.onError(response.message());
        }
    }

    @Override
    public void onFailure(@NonNull Call<AnnouncerResponse> call, @NonNull Throwable t) {
        // Hide Dialog
        mProgressDialog.dismiss();

        // Handle Response
        mOnAlertSentListener.onError(t.getMessage());
    }
}
