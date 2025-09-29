package com.eventplanner.utils;

import android.util.Base64;

import com.eventplanner.model.auth.JwtPayload;
import com.google.gson.Gson;

import java.nio.charset.StandardCharsets;

public class JwtUtils {
    public static JwtPayload decodeJwt(String token) {
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
