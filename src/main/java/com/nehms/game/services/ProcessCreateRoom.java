package com.nehms.game.services;

import com.nehms.game.model.Room;
import com.nehms.game.model.SocketManager;
import com.nehms.game.util.Converter;
import com.nehms.game.valueobjets.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;

@Component
public class ProcessCreateRoom implements Orchestrator {

    private final Converter<Object, String> jsonConverter;

    public ProcessCreateRoom(Converter<Object, String> jsonConverter) {
        this.jsonConverter = jsonConverter;
    }

    @Override
    public void processTheCurrentState(SocketManager socketManager) throws IOException {

        if (socketManager.getCurrentMessage() == null)
            return;

        if (!"createRoom".equals(socketManager.getCurrentMessage()))
            return;

        Message message = new Message();
        Room room = new Room();
        message.setBody(room.getRoomKey());
        message.setType("roomKey");

        if (!socketManager.getSocketRooms().contains(room)) {
            socketManager.getSocketRooms().add(room);
            socketManager.getCurrentSession().sendMessage(new TextMessage(jsonConverter.convert(message)));
            message.setType("roomCreated");
            message.setBody("Bienvenue Veillez rejoindre une partie ou créer un salon de jeu");
            socketManager.getCurrentSession().sendMessage(new TextMessage(jsonConverter.convert(message)));
        }

    }

}
