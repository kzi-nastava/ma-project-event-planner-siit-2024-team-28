package com.eventplanner.utils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class JwtInterceptor implements Interceptor {
    private final TokenProvider tokenProvider;

    public JwtInterceptor(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = tokenProvider.getToken();
        Request originalRequest = chain.request();

        if (token != null && !token.isEmpty()) {
            Request modifiedRequest = originalRequest.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(modifiedRequest);
        }

        return chain.proceed(originalRequest);
    }

    public interface TokenProvider {
        String getToken();
    }
}

