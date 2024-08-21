package com.nehms.game.services;

import com.nehms.game.model.GameSession;
import com.nehms.game.model.Player;
import com.nehms.game.model.Room;
import com.nehms.game.model.SocketManager;
import com.nehms.game.util.Broadcaster;
import com.nehms.game.util.Converter;
import com.nehms.game.util.SimpleBroadcaster;
import com.nehms.game.valueobjets.GameStep;
import com.nehms.game.valueobjets.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;

@Component
public class ProcessJoinRoom implements Orchestrator {

    private final Converter<Object, String> jsonConverter;

    public ProcessJoinRoom(Converter<Object, String> jsonConverter) {
        this.jsonConverter = jsonConverter;
    }

    @Override
    public void processTheCurrentState(SocketManager socketManager) throws IOException {

        String checkRoomKey = socketManager.getCurrentMessage();
        Message message = new Message();

        if (checkRoomKey == null)
            return;

        for (Room room : socketManager.getSocketRooms()) {
            if (!room.getRoomKey().equals(checkRoomKey))
                return;

            if (room.getGameSession().getSocketSessions().size() == room.getGameSession().getMaxPlayer()) {
                message.setBody("la session de la socket est full !! ");
                message.setType("full");
                socketManager.getCurrentSession().sendMessage(new TextMessage(jsonConverter.convert(message)));
                return;
            }

            room.getGameSession().setCurrentSession(socketManager.getCurrentSession());
            room.getGameSession().setCurrentMessage(socketManager.getCurrentMessage());
            createPlayers(room.getGameSession());

            socketManager.setCurrentRoomIndex(findTheIndexOfRoom(checkRoomKey, socketManager.getSocketRooms()));

            message.setType("gameOk");
            message.setBody("");
            room.getGameSession().getCurrentSession().sendMessage(new TextMessage(jsonConverter.convert(message)));
        }
    }

    private void createPlayers(GameSession gameSession) throws IOException {

        Broadcaster broadcast = new SimpleBroadcaster();

        Message objectResponse = new Message();

        if (!GameStep.CREATE_PLAYER.equals(gameSession.getGameStep()))
            return;

        Player player = new Player("Player " + (gameSession.getPlayers().size() + 1));

        objectResponse.setNamePlayer("Player " + (gameSession.getPlayers().size() + 1));

        if (!gameSession.getSocketSessions().contains(gameSession.getCurrentSession())
                && gameSession.getPlayers().size() < gameSession.getMaxPlayer()) {

            gameSession.getPlayers().add(player);

            gameSession.getSocketSessions().add(gameSession.getCurrentSession());

            objectResponse.setBody("Bienvenue "
                    + gameSession.getPlayers().get(gameSession.getSocketSessions().size() - 1).getName());

            objectResponse.setType("creation de joueur");

            gameSession.getCurrentSession().sendMessage(new TextMessage(jsonConverter.convert(objectResponse)));

            objectResponse.setType("");

        }

        if (gameSession.getPlayers().size() == gameSession.getMaxPlayer()) {

            objectResponse.setBody("Are you ready to play ?");

            objectResponse.setType("are you ready ?");

            broadcast.broadcastMessage(jsonConverter.convert(objectResponse), gameSession.getSocketSessions());
            gameSession.setGameStep(GameStep.ACCEPT_TO_PLAY);
        }

    }

}
