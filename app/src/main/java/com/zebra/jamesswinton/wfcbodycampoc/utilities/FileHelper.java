package com.zebra.jamesswinton.wfcbodycampoc.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.zebra.jamesswinton.wfcbodycampoc.pojos.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.FOLDER_NAME;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.IMAGE_FILE_NAME_PREFIX;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Constants.VIDEO_FILE_NAME_PREFIX;
import static com.zebra.jamesswinton.wfcbodycampoc.utilities.Type.IMAGE;

public class FileHelper {

    /** Create a file Uri for saving an image or video */
    public static Uri getOutputUri(Context cx, Type type){
        return FileProvider.getUriForFile(cx,
                cx.getApplicationContext().getPackageName() + ".provider",
                getOutputFile(type));
    }

    /** Create a File for saving an image or video */
    @SuppressLint("SimpleDateFormat")
    private static File getOutputFile(Type type){
        // Init Directory
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + File.separator + FOLDER_NAME);

        // Validate Directory
        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.e("FileHelper", "failed to create directory");
                return null;
            }
        }

        // Create Media File
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        if (type == IMAGE){
            return new File(mediaStorageDir.getPath() + File.separator +
                    IMAGE_FILE_NAME_PREFIX + timeStamp + ".jpg");
        } else {
            return new File(mediaStorageDir.getPath() + File.separator +
                    VIDEO_FILE_NAME_PREFIX + timeStamp + ".mp4");
        }
    }

    // config
    public static Config loadConfigToMemoryFromFile(Context context) throws IOException {
        // Init Directory
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + File.separator + FOLDER_NAME);

        // Validate Directory
        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.e("FileHelper", "failed to create directory");
                return null;
            }
        }

        // Create Config File
        File configFile = new File(mediaStorageDir.getAbsolutePath()
                + File.separator + "Config.json");

        // Create Default File
        if (!configFile.exists()) {
            InputStream is = context.getResources().getAssets().open("Config.json");
            OutputStream os = new FileOutputStream(configFile);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
        }

        // Read JSON File
        return new Gson().fromJson(new BufferedReader(new FileReader(configFile)), Config.class);
    }

    public static File getFileFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        String fileName = getFileName(context, uri);
        String[] splitName = splitFileName(fileName);
        File tempFile = File.createTempFile(splitName[0], splitName[1]);
        tempFile = rename(tempFile, fileName);
        tempFile.deleteOnExit();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tempFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (inputStream != null) {
            copy(inputStream, out);
            inputStream.close();
        }

        if (out != null) {
            out.close();
        }
        return tempFile;
    }

    private static String[] splitFileName(String fileName) {
        String name = fileName;
        String extension = "";
        int i = fileName.lastIndexOf(".");
        if (i != -1) {
            name = fileName.substring(0, i);
            extension = fileName.substring(i);
        }

        return new String[]{name, extension};
    }

    private static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf(File.separator);
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private static File rename(File file, String newName) {
        File newFile = new File(file.getParent(), newName);
        if (!newFile.equals(file)) {
            if (newFile.exists() && newFile.delete()) {
                Log.d("FileUtil", "Delete old " + newName + " file");
            }
            if (file.renameTo(newFile)) {
                Log.d("FileUtil", "Rename file to " + newName);
            }
        }
        return newFile;
    }

    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static long copy(InputStream input, OutputStream output) throws IOException {
        long count = 0;
        int n;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
