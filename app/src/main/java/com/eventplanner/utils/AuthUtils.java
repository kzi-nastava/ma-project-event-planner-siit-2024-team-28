package com.eventplanner.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.eventplanner.model.auth.JwtPayload;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AuthUtils {
    private static final String PREF_NAME = "authPrefs";
    private static final String TOKEN_KEY = "authToken";

    public static void saveToken(Context context, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(TOKEN_KEY, token).apply();
    }

    public static void clearToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(TOKEN_KEY).apply();
    }

    public static String getToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(TOKEN_KEY, null);
    }

    public static List<String> getUserRoles(Context context) {
        String token = getToken(context);
        if (token != null) {
            JwtPayload payload = decodeJwt(token);
            if (payload != null) {
                return payload.getRolesList();
            }
        }
        return Collections.emptyList();
    }

//    public static String getUserRole(Context context) {
//        String token = getToken(context);
//        if (token != null) {
//            JwtPayload payload = decodeJwt(token);
//            if (payload != null) {
//                return payload.getRole();
//            }
//        }
//        return null;
//    }

    public static Long getUserId(Context context) {
        String token = getToken(context);
        if (token != null) {
            JwtPayload payload = decodeJwt(token);
            if (payload != null) {
                return payload.getUserId();
            }
        }
        return null;
    }

    private static JwtPayload decodeJwt(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                String payloadJson = new String(Base64.decode(parts[1], Base64.URL_SAFE), StandardCharsets.UTF_8);
                return new Gson().fromJson(payloadJson, JwtPayload.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
