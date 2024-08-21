package com.nehms.game.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Slf4j
public class SocketMultiCaster {

    private static SocketMultiCaster instance;
    private final Map<String, List<WebSocketSession>> sessions;

    private SocketMultiCaster(Map<String, List<WebSocketSession>> sessions) {
        this.sessions = sessions;
    }

    public static SocketMultiCaster getInstance() {
        if (instance == null) {
            instance = new SocketMultiCaster(new ConcurrentHashMap<>());
        }
        return instance;
    }

    public void multicast(String roomKey, String message) {
        sessions.get(roomKey)
                .forEach(session -> {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(new TextMessage(message));
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                });
    }

    public void broadcast(String roomKey, String sessionId, String message) {
        sessions.get(roomKey)
                .stream()
                .filter(webSocketSession -> webSocketSession.getId().equals(sessionId))
                .findFirst()
                .ifPresent(webSocketSession -> {
                    if (webSocketSession.isOpen()) {
                        try {
                            webSocketSession.sendMessage(new TextMessage(message));
                        } catch (IOException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                });
    }
}
