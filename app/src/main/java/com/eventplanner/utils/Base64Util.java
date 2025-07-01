package com.eventplanner.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;

public class Base64Util {
    private static final String TAG = "Base64Util";
    public static String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    // TODO: Fix base64 profile photo decoding. Bitmap is always null.
    public static Bitmap decodeBase64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            Log.d(TAG, "Base64 string is null or empty");
            return null;
        }

        try {
            // Trim any whitespace or newlines
            base64String = "data:image/jpeg;base64," + base64String.trim();

            Log.d(TAG, "Decoding base64 string, length: " + base64String.length());

            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            Log.d(TAG, "Decoded byte array length: " + (decodedBytes != null ? decodedBytes.length : "null"));

            if (decodedBytes == null || decodedBytes.length == 0) {
                Log.e(TAG, "Decoded bytes are null or empty");
                return null;
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            if (bitmap == null) {
                Log.e(TAG, "BitmapFactory failed to decode byte array");
                // Try with different options
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length, options);
            }

            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error decoding base64 to bitmap", e);
            return null;
        }
    }
}
