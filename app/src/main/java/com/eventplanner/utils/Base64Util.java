package com.eventplanner.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class Base64Util {
    private static final String TAG = "Base64Util";

    public static String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();

        return Base64.getEncoder().encodeToString(imageBytes);
    }

    // TODO: Fix base64 profile photo decoding. Bitmap is always null.
    public static Bitmap decodeBase64ToBitmap(String base64String) {
        if (base64String == null) {
            Log.d(TAG, "Base64 string is null");
            return null;
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(base64String);
            if (decodedBytes == null || decodedBytes.length == 0) {
                Log.e(TAG, "Decoded bytes are null or empty");
                return null;
            }

            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Error decoding base64 to bitmap", e);
            return null;
        }
    }
}
