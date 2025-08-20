package com.eventplanner.services;

import android.util.Log;

import com.eventplanner.utils.WebSocketService;

import java.util.function.Consumer;

import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.StompClient;

public class ChatWebSocketService {
    private final StompClient stompClient;

    /**
     * For chat-based channel we only need one channel at the time
     *  since this subscription is only responsible for real-time messaging in chat
     *  i.e. we don't need multiple chat subscriptions active at the same time (we don't need List<Disposable> chatSubscription)
     */
    private Disposable chatSubscription;

    public ChatWebSocketService() {
        // Directly takes singleton stomp-client
        this.stompClient = WebSocketService.getInstance().getClient();
    }

    public void subscribeToChat(Long chatId, Consumer<String> onMessage) {
        if (stompClient != null && stompClient.isConnected()) {
            chatSubscription = stompClient.topic("/topic/chat/" + chatId)
                    .subscribe(topicMessage -> onMessage.accept(topicMessage.getPayload()),
                            throwable -> Log.e("ChatWebSocketService", "STOMP topic subscription error", throwable));
        }
    }

    public void sendMessage(String message, Runnable onComplete) {
        String destination = "/app/send/message";
        if (stompClient != null && stompClient.isConnected()) {
            stompClient.send(destination, message)
                    .subscribe(() -> { if (onComplete != null) onComplete.run(); },
                            throwable -> Log.e("ChatWebSocketService", "Failed to send STOMP message", throwable));
        }
    }

    public void unsubscribeFromChat() {
        if (chatSubscription != null && !chatSubscription.isDisposed()) {
            chatSubscription.dispose();
        }
    }
}
