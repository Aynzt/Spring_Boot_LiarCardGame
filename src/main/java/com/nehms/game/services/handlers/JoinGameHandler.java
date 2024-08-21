package com.nehms.game.services.handlers;

import com.nehms.game.model.GameMessage;
import com.nehms.game.model.Room;
import com.nehms.game.util.SocketMultiCaster;
import com.nehms.game.valueobjets.GameStep;
import com.nehms.game.valueobjets.MessageType;
import com.nehms.game.valueobjets.PlayerStep;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JoinGameHandler extends GameHandler {

    @Override
    public void handle(String sessionId, Room room) {

        if (!GameStep.CREATE_PLAYER.equals(room.getStep())) {
            handleNext(sessionId, room);
            return;
        }

        // On vérifie si le joueur appartient bien à la room
        room.getPlayers().computeIfPresent(sessionId, (s, player) -> {

            if (!StringUtils.hasText(room.getMessageReceived())) {
                SocketMultiCaster.getInstance()
                        .broadcast(room.getRoomKey(), sessionId, GameMessage.builder()
                                .type(MessageType.INFO)
                                .body("Un message vide ?")
                                .build()
                                .toJson());
                return player;
            }

            if (PlayerStep.APPROVING.equals(player.getStep()))
                return player;

            player.setName(room.getMessageReceived());
            player.setStep(PlayerStep.APPROVING);

            SocketMultiCaster.getInstance()
                    .broadcast(room.getRoomKey(), sessionId, GameMessage.builder()
                            .type(MessageType.INFO)
                            .body(String.format("OK, c'est sauvegardé. Bienvenue %s, nous attendons les autres joueurs.", player.getName()))
                            .build()
                            .toJson());

            return player;
        });

       boolean isEverybodyApproving = checkRoomPlayers(room, PlayerStep.APPROVING);

        if (isEverybodyApproving) {
            room.setStep(GameStep.ACCEPT_TO_PLAY);

            SocketMultiCaster.getInstance()
                    .multicast(room.getRoomKey(), GameMessage.builder()
                            .type(MessageType.INFO)
                            .body("Voulez-vous commencez à jouer ?")
                            .build()
                            .toJson());
        }

    }
}
