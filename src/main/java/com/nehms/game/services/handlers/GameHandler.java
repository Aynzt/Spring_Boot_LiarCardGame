package com.nehms.game.services.handlers;

import com.nehms.game.model.GameMessage;
import com.nehms.game.model.Room;
import com.nehms.game.util.SocketMultiCaster;
import com.nehms.game.valueobjets.GameStep;
import com.nehms.game.valueobjets.MessageType;
import com.nehms.game.valueobjets.PlayerStep;

public abstract class GameHandler {

    private GameHandler next;

    public static GameHandler link(GameHandler first, GameHandler... chain) {

        GameHandler head = first;

        for (GameHandler nextHandler : chain) {
            head.next = nextHandler;
            head = nextHandler;
        }

        return first;
    }

    public abstract void handle(String sessionId, Room room);

    protected void handleNext(String sessionId, Room room) {
        if (next == null)
            return;

        next.handleNext(sessionId, room);
    }

    boolean checkRoomPlayers(Room room, PlayerStep step) {
        return room.getPlayers().values().stream().allMatch(player -> step.equals(player.getStep()));
    }

    void checkWinner(Room room) {
        room.getPlayers()
                .values()
                .stream()
                .filter(player -> player.getHand().isEmpty())
                .findFirst()
                .ifPresent(player -> {

                    SocketMultiCaster.getInstance()
                            .multicast(room.getRoomKey(), GameMessage.builder()
                                    .type(MessageType.INFO)
                                    .body(String.format("Le gagnant du jeu est %s", player.getName()))
                                    .build()
                                    .toJson());

                    room.setStep(GameStep.END);
                });
    }

}
