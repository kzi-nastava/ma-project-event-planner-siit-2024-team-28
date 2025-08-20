package com.eventplanner.utils;

import android.util.Log;

import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

/**
 *  WebSocketService is just a singleton that maintains the connection.
 *  It doesn't handle any specific messages itself – it only keeps the StompClient active.
 */
public class WebSocketService {
    private static WebSocketService instance;
    private StompClient stompClient;
    private Disposable lifecycleDisposable;

    private static final String WS_URL = "ws://10.0.2.2:8080/ws-native";

    private WebSocketService() {
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL);
    }

    // Lazy singleton - first call creates connection, every other gets the same instance
    public static synchronized WebSocketService getInstance() {
        if (instance == null) {
            instance = new WebSocketService();
        }
        return instance;
    }

    public void connect(Runnable onConnected) {
        if (stompClient != null && stompClient.isConnected()) {
            Log.d("WebSocketService", "Already connected");
            if (onConnected != null) onConnected.run();
            return;
        }

        stompClient.connect();

        lifecycleDisposable = stompClient.lifecycle().subscribe(lifecycleEvent -> {
            switch (lifecycleEvent.getType()) {
                case OPENED:
                    Log.d("WebSocketService", "Connected to STOMP");
                    if (onConnected != null) onConnected.run();
                    break;
                case ERROR:
                    Log.e("WebSocketService", "Connection error", lifecycleEvent.getException());
                    break;
                case CLOSED:
                    Log.d("WebSocketService", "Connection closed");
                    break;
            }
        });
    }

    public boolean isConnected() {
        return stompClient != null && stompClient.isConnected();
    }

    public StompClient getClient() {
        return stompClient;
    }

    /**
     * Disconnect method is optional here since the singleton connection is intended
     * to stay alive for the entire lifetime of the application.
    */
    public void disconnect() {
        if (lifecycleDisposable != null && !lifecycleDisposable.isDisposed()) {
            lifecycleDisposable.dispose();
        }
        if (stompClient != null) {
            stompClient.disconnect();
        }
        instance = null;
    }

}
