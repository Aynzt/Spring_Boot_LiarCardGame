package com.nehms.game.util;

import java.io.IOException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
@Slf4j
public final class SimpleBroadcaster implements Broadcaster {

    @Override
    public void broadcastMessage(String message, List<WebSocketSession> sessions) {
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

}
