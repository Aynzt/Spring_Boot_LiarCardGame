package com.nehms.game.services;

import com.nehms.game.model.GameSession;
import com.nehms.game.model.SocketManager;
import com.nehms.game.services.visitors.ConfigVisitor;
import com.nehms.game.util.Broadcaster;
import com.nehms.game.util.Converter;
import com.nehms.game.valueobjets.GameStep;
import com.nehms.game.valueobjets.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;

@Component
public class ProcessAskOkToPlay implements Orchestrator {

    private final Broadcaster broadcaster;
    private final Converter<Object, String> jsonConverter;
    private final ConfigVisitor configVisitor;

    public ProcessAskOkToPlay(Broadcaster broadcaster,
                              Converter<Object, String> jsonConverter,
                              ConfigVisitor configVisitor) {
        this.broadcaster = broadcaster;
        this.jsonConverter = jsonConverter;
        this.configVisitor = configVisitor;
    }

    @Override
    public void processTheCurrentState(SocketManager socketManager) throws IOException {

        String[] message = socketManager.getCurrentMessage().split("-");

        if (message.length != 2)
            return;

        int i = findTheIndexOfRoom(message[0], socketManager.getSocketRooms());

        if (!GameStep.ACCEPT_TO_PLAY.equals(socketManager.getSocketRooms().get(i).getGameSession().getGameStep()))
            return;

        socketManager.getSocketRooms().get(i).getGameSession().setCurrentMessage(message[1].trim());
        execute(socketManager.getSocketRooms().get(i).getGameSession());
    }

    private void execute(GameSession gameSession) throws IOException {

        Message message = new Message();

        if (gameSession.getCurrentMessage().isEmpty()) {
            return;
        }

        if (!gameSession.getGameStep().equals(GameStep.ACCEPT_TO_PLAY)) {
            return;
        }

        if (!"oui".equalsIgnoreCase(gameSession.getCurrentMessage()))
            return;

        gameSession.setNumberOfValidation(gameSession.getNumberOfValidation() + 1);

        if (gameSession.getNumberOfValidation() != gameSession.getMaxPlayer())
            return;

        gameSession.setGameStep(GameStep.PLAY_CARD);

        gameSession.configure(configVisitor);

        message.setBody("Votre main ♠️");
        message.setType("CARD");

        for (int i = 0; i < gameSession.getSocketSessions().size(); i++) {
            message.setCards(gameSession.getPlayers().get(i).getHand());
            gameSession.getSocketSessions().get(i)
                    .sendMessage(new TextMessage(jsonConverter.convert(message)));
        }

        message.setBody("Bonne change a tous  !! 🍀🍀");
        message.setType("bonne chance");
        message.setCards(null);

        broadcaster.broadcastMessage(jsonConverter.convert(message), gameSession.getSocketSessions());

        message.setBody("Joueur 1 a toi de jouer !! 🚩");
        message.setType("a toi");

        gameSession.getSocketSessions().getFirst()
                .sendMessage(new TextMessage(jsonConverter.convert(message)));
    }
}
