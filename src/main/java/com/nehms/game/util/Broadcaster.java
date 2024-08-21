package com.nehms.game.util;

import org.springframework.web.socket.WebSocketSession;

import java.util.List;

public interface Broadcaster {
    void broadcastMessage(String message, List<WebSocketSession> sessions);
}
