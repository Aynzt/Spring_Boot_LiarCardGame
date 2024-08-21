package com.nehms.game.services.handlers;

import com.nehms.game.model.GameMessage;
import com.nehms.game.model.Room;
import com.nehms.game.services.visitors.GameConfigurer;
import com.nehms.game.util.SocketMultiCaster;
import com.nehms.game.valueobjets.GameStep;
import com.nehms.game.valueobjets.MessageType;
import com.nehms.game.valueobjets.PlayerStep;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class StartGameHandler extends GameHandler {

    private final GameConfigurer gameConfigurer;

    public StartGameHandler(GameConfigurer gameConfigurer) {
        this.gameConfigurer = gameConfigurer;
    }

    @Override
    public void handle(String sessionId, Room room) {

        if (!GameStep.ACCEPT_TO_PLAY.equals(room.getStep())) {
            handleNext(sessionId, room);
            return;
        }

        boolean isEverybodyApproving = checkRoomPlayers(room, PlayerStep.APPROVING);

        if (!isEverybodyApproving) {
            SocketMultiCaster.getInstance()
                    .broadcast(room.getRoomKey(), sessionId, GameMessage.builder()
                            .type(MessageType.INFO)
                            .body("Tous les joueurs ne sont pas prêts")
                            .build()
                            .toJson());
            return;
        }

        if (!StringUtils.hasText(room.getMessageReceived())) {
            SocketMultiCaster.getInstance()
                    .broadcast(room.getRoomKey(), sessionId, GameMessage.builder()
                            .type(MessageType.INFO)
                            .body("Un message vide ?")
                            .build()
                            .toJson());
            return;
        }

        if (!"oui".equals(room.getMessageReceived())) {

            SocketMultiCaster.getInstance()
                    .broadcast(room.getRoomKey(), sessionId, GameMessage.builder()
                            .type(MessageType.ALERT)
                            .body("Vous ne voulez pas jouer ? Vous pouvez vous déconnecter")
                            .build()
                            .toJson());
            return;
        }

        room.getPlayers().computeIfPresent(sessionId, (s, player) -> {
            player.setStep(PlayerStep.APPROVED);
            return player;
        });

        if (checkRoomPlayers(room, PlayerStep.APPROVED)) {
            SocketMultiCaster.getInstance()
                    .multicast(room.getRoomKey(), GameMessage.builder()
                            .type(MessageType.INFO)
                            .body("La partie peut commencer, Nous créons le paquet de cartes")
                            .build()
                            .toJson());
            gameConfigurer.createCards(room);
            SocketMultiCaster.getInstance()
                    .multicast(room.getRoomKey(), GameMessage.builder()
                            .type(MessageType.INFO)
                            .body("Nous mélangeons le paquet de cartes")
                            .build()
                            .toJson());
            gameConfigurer.mixCards(room);
            SocketMultiCaster.getInstance()
                    .multicast(room.getRoomKey(), GameMessage.builder()
                            .type(MessageType.INFO)
                            .body("Nous vous distribuons les cartes")
                            .build()
                            .toJson());
            gameConfigurer.distribute(room);
            gameConfigurer.sortPlayers(room);

            room.setStep(GameStep.PLAY_CARD);

            room.setCurrentPlayer(room.getPlayerSorted().getFirst());

            room.getPlayers().forEach((s, player) -> player.setStep(PlayerStep.PLAYING));

            for (int i = 0; i < room.getPlayerSorted().size(); i++) {
                String session = room.getPlayerSorted().get(i);
                SocketMultiCaster.getInstance()
                        .broadcast(room.getRoomKey(), session, GameMessage.builder()
                                .type(MessageType.INFO)
                                .body(String.format("Vous jouerez à la position %d", i))
                                .build()
                                .toJson());
            }
        }

    }
}
