package com.nehms.game.services.handlers;

import com.nehms.game.model.GameMessage;
import com.nehms.game.model.Player;
import com.nehms.game.model.Room;
import com.nehms.game.util.Processor;
import com.nehms.game.util.SocketMultiCaster;
import com.nehms.game.valueobjets.GameStep;
import com.nehms.game.valueobjets.MessageType;
import com.nehms.game.valueobjets.PlayerStep;
import org.springframework.stereotype.Component;

@Component
public class ContestGameHandler extends GameHandler {

    @Override
    public void handle(String sessionId, Room room) {

        if (!GameStep.CONTESTATION.equals(room.getStep())) {
            handleNext(sessionId, room);
            return;
        }

        boolean isEverybodyPlaying = checkRoomPlayers(room, PlayerStep.PLAYING);

        if (!isEverybodyPlaying) {
            SocketMultiCaster.getInstance()
                    .broadcast(room.getRoomKey(), sessionId, GameMessage.builder()
                            .type(MessageType.INFO)
                            .body("Tous les joueurs ne sont pas prêts")
                            .build()
                            .toJson());
            return;
        }

        if (!"moi".equals(room.getMessageReceived())) {
            SocketMultiCaster.getInstance()
                    .broadcast(room.getRoomKey(), sessionId, GameMessage.builder()
                            .type(MessageType.INFO)
                            .body("Envoyez \"moi\" afin de contester.")
                            .build()
                            .toJson());
            return;
        }

        if (!room.getCurrentPlayer().equals(sessionId)) {
            SocketMultiCaster.getInstance()
                    .multicast(room.getRoomKey(), GameMessage.builder()
                            .type(MessageType.ALERT)
                            .body("Ce n'est pas encore votre tour")
                            .build()
                            .toJson());
            return;
        }

        Player contestant = room.getPlayers().get(sessionId);
        Player lastPlayer = room.getPlayers().get(room.getLastPlayer());

        String message = String.format("Le joueur %s prend toutes les cartes", contestant.getName());

        if (room.getCardPlayed().getPattern().equals(room.getPatternPlayed()))
            contestant.getHand().addAll(room.getCardOnPlay());
        else {
            lastPlayer.getHand().addAll(room.getCardOnPlay());
            message = String.format("Le joueur %s prend toutes les cartes", lastPlayer.getName());
        }

        room.getCardOnPlay().clear();

        SocketMultiCaster.getInstance()
                .multicast(room.getRoomKey(), GameMessage.builder()
                        .type(MessageType.INFO)
                        .body(message)
                        .build()
                        .toJson());

        room.setStep(GameStep.PLAY_CARD);

        // Je notifie les joueurs de la nouvelle carte.
        room.getPlayers().forEach((s, player1) -> SocketMultiCaster.getInstance()
                .broadcast(room.getRoomKey(), s, GameMessage.builder()
                        .type(MessageType.CARD)
                        .body("Votre main ♠️")
                        .cards(player1.getHand())
                        .build()
                        .toJson()));

    }


}
