package com.nehms.game.socket;

import com.nehms.game.model.*;
import com.nehms.game.util.SocketMultiCaster;
import com.nehms.game.valueobjets.GameStep;
import com.nehms.game.valueobjets.MessageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

@Component
@Slf4j
public class RoomHandler implements WebSocketHandler {

    private final SocketMultiCaster multiCaster;

    public RoomHandler() {
        this.multiCaster = SocketMultiCaster.getInstance();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // on rajoute les utilisateurs à la room
        String roomKey = Objects.requireNonNull(session.getUri()).getPath().replace("/rooms/", "");

        // On rajoute la personne à la partie.

        RoomManager roomManager = RoomManager.getInstance();

        roomManager.getRooms().computeIfAbsent(roomKey, Room::new);

        roomManager.getRooms().computeIfPresent(roomKey, (s, room) -> {
            try {
                if (room.getPlayers().size() >= 3) {
                    session.sendMessage(new TextMessage(GameMessage.builder()
                            .type(MessageType.ALERT)
                            .body("La limite des joueurs est atteinte pour cette salle")
                            .build()
                            .toJson()));
                    session.close(CloseStatus.NOT_ACCEPTABLE);
                    return room;
                }

                room.getPlayers().put(session.getId(), new Player("", session.getId()));

                return room;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        multiCaster.getSessions().computeIfAbsent(roomKey, s -> new ArrayList<>());

        multiCaster.getSessions().computeIfPresent(roomKey, (s, webSocketSessions) -> {
            webSocketSessions.add(session);
            return webSocketSessions;
        });

        multiCaster.broadcast(roomKey, session.getId(), GameMessage.builder()
                .type(MessageType.INFO)
                .body("Bienvenue dans le jeu du mensonge. Comment devons-nous vous appelez ?")
                .build()
                .toJson());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        log.info("Message - {} - {}", session.getId(), message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Transport Error - {} - {}", session.getId(), exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {

        String roomKey = Objects.requireNonNull(session.getUri()).getPath().replace("/rooms/", "");

        RoomManager roomManager = RoomManager.getInstance();

        if (!roomManager.getRooms().containsKey(roomKey))
            return;

        // Je supprime le joueur de la partie
        roomManager.getRooms()
                .computeIfPresent(roomKey, (s, room) -> {

                    Player player = room.getPlayers().get(session.getId());

                    room.getPlayers().remove(session.getId());

                    multiCaster.multicast(s, GameMessage.builder()
                            .body(String.format("Le joueur %s a quitté la partie à cause de %s. En attente d'un autre joueur", player.getName(), closeStatus.getReason()))
                            .type(MessageType.ALERT)
                            .build()
                            .toJson());

                    room.setStep(GameStep.CREATE_PLAYER);

                    return room;
                });
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
