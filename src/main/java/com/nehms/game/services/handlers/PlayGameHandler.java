package com.nehms.game.services.handlers;

import com.nehms.game.model.Card;
import com.nehms.game.model.GameMessage;
import com.nehms.game.model.Player;
import com.nehms.game.model.Room;
import com.nehms.game.util.Processor;
import com.nehms.game.util.SocketMultiCaster;
import com.nehms.game.valueobjets.GameStep;
import com.nehms.game.valueobjets.MessageProcessed;
import com.nehms.game.valueobjets.MessageType;
import com.nehms.game.valueobjets.PlayerStep;
import org.springframework.stereotype.Component;

@Component
public class PlayGameHandler extends GameHandler {

    private final Processor processor;

    public PlayGameHandler(Processor processor) {
        this.processor = processor;
    }

    @Override
    public void handle(String sessionId, Room room) {

        if (!GameStep.PLAY_CARD.equals(room.getStep())) {
            handleNext(sessionId, room);
            return;
        }

        checkWinner(room);

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

        // vérifions si c'est le tour de cet utilisateur de jouer
        if (!room.getCurrentPlayer().equals(sessionId)) {
            SocketMultiCaster.getInstance()
                    .multicast(room.getRoomKey(), GameMessage.builder()
                            .type(MessageType.ALERT)
                            .body("Ce n'est pas encore votre tour")
                            .build()
                            .toJson());
            return;
        }

        MessageProcessed messageProcessed = processor.processingMessage(room.getMessageReceived());

        if (messageProcessed == null) {
            SocketMultiCaster.getInstance()
                    .broadcast(room.getRoomKey(), sessionId, GameMessage.builder()
                            .type(MessageType.ALERT)
                            .body("Mauvais format de message")
                            .build()
                            .toJson());
            return;
        }

        room.setPatternPlayed(messageProcessed.getPattern());

        // Je récupère le joueur en cours
        Player player = room.getPlayers().get(sessionId);
        // Je crée la carte qu'il vient de jouer
        Card cardPlayed = new Card(messageProcessed.getPattern(), messageProcessed.getNumber());
        // je retire la carte de sa main
        boolean isRemoved = player.getHand()
                .removeIf(card -> card.getNumber().equals(messageProcessed.getNumber()) && card.getPattern().equals(messageProcessed.getPattern()));

        if (isRemoved) {
            room.getCardOnPlay().add(cardPlayed);
            room.setCardPlayed(cardPlayed);
        }

        // Je notifie les joueurs de la nouvelle carte.
        room.getPlayers().forEach((s, player1) -> SocketMultiCaster.getInstance()
                .broadcast(room.getRoomKey(), s, GameMessage.builder()
                        .type(MessageType.CARD)
                        .body("Votre main ♠️")
                        .currentCard(cardPlayed)
                        .currentPattern(messageProcessed.getPatternPlay())
                        .cards(player1.getHand())
                        .build()
                        .toJson()));

        SocketMultiCaster.getInstance()
                .broadcast(room.getRoomKey(), sessionId, GameMessage.builder()
                        .type(MessageType.INFO)
                        .body(String.format("Vous venez de jouer la carte %s %s et le pattern déclaré : %s", cardPlayed.getNumber(), cardPlayed.getPattern().name(), messageProcessed.getPatternPlay()))
                        .currentCard(cardPlayed)
                        .currentPattern(cardPlayed.getPattern())
                        .build()
                        .toJson());

        room.setCurrentPlayer(room.getPlayerSorted().get(player.getPosition() + 1));

        room.setLastPlayer(player.getSessionId());

        room.setLapPlayed(room.getLapPlayed() + 1);

        if (room.getLapPlayed() >= 4) {
            room.setStep(GameStep.CONTESTATION);

            SocketMultiCaster.getInstance()
                    .multicast(room.getRoomKey(), GameMessage.builder()
                            .type(MessageType.INFO)
                            .body("Voulez-vous contester le motif déposé ?")
                            .build()
                            .toJson());
        }
    }


}
