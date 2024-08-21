package com.nehms.game.services;

import com.nehms.game.model.Card;
import com.nehms.game.model.GameSession;
import com.nehms.game.model.Player;
import com.nehms.game.model.SocketManager;
import com.nehms.game.exceptions.GameSessionNullException;
import com.nehms.game.util.Broadcaster;
import com.nehms.game.util.Converter;
import com.nehms.game.valueobjets.GameStep;
import com.nehms.game.valueobjets.Message;
import com.nehms.game.valueobjets.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ProcessContestation implements Orchestrator {

    private final Broadcaster broadcaster;
    private final Converter<Object, String> jsonConverter;

    public ProcessContestation(Broadcaster broadcaster,
                               Converter<Object, String> jsonConverter) {
        this.broadcaster = broadcaster;
        this.jsonConverter = jsonConverter;
    }

    @Override
    public void processTheCurrentState(SocketManager socketManager) {
        try {
            if (socketManager == null)
                throw new GameSessionNullException();

            String[] message = socketManager.getCurrentMessage().split("=");

            if (message.length != 2)
                return;

            int i = findTheIndexOfRoom(message[0], socketManager.getSocketRooms());

            if (!GameStep.CONTESTATION.equals(socketManager.getSocketRooms().get(i).getGameSession().getGameStep()))
                return;

            socketManager.getSocketRooms().get(i).getGameSession().setCurrentMessage(message[1]);
            socketManager.getSocketRooms().get(i).getGameSession().setCurrentSession(socketManager.getCurrentSession());

            execute(socketManager.getSocketRooms().get(i).getGameSession());

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void execute(GameSession gameSession) throws IOException {

        Message message = new Message();

        if (!gameSession.getGameStep().equals(GameStep.CONTESTATION))
            return;

        startCountdown(gameSession);

        if (gameSession.getCurrentMessage().isEmpty())
            return;

        if (!"moi".equals(gameSession.getCurrentMessage())) {
            gameSession.getCurrentSession()
                    .sendMessage(new TextMessage("Pour contester dites <<moi>> !! 🚨🚨"));
            return;
        }

        if (!isContestantPlayer(gameSession)) {
            message.setType("vous ne pouvez pas contester");
            message.setBody("Vous ne pouvez pas vous contester !!✖️✖️");

            gameSession.getCurrentSession()
                    .sendMessage(new TextMessage(jsonConverter.convert(message)));

            return;
        }

        gameSession.setCurrentMessage("");

        contestPlayer(gameSession.getPlayers().get(getIndex(gameSession)),
                gameSession.getPlayers().get(gameSession.getPlayersIndexes()), gameSession.getCurrentcard(),
                gameSession.getCurrentPattern(), gameSession.getGameTable(),
                gameSession.getSocketSessions());

        message.setBody("Votre main ♠️");
        message.setType("CARD");

        for (int i = 0; i < gameSession.getSocketSessions().size(); i++) {
            message.setCards(gameSession.getPlayers().get(i).getHand());
            gameSession.getSocketSessions().get(i)
                    .sendMessage(new TextMessage(jsonConverter.convert(message)));
        }

        gameSession.setGameStep(GameStep.PLAY_CARD);


        message.setBody("Votre main ♠️");
        message.setType("CARD");

        for (int i = 0; i < gameSession.getSocketSessions().size(); i++) {
            message.setCards(gameSession.getPlayers().get(i).getHand());
            gameSession.getSocketSessions().get(i).sendMessage(new TextMessage(jsonConverter.convert(message)));
        }
    }

    private void startCountdown(GameSession gameSession) {
        try(ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor()) {
            Message message = new Message();

            scheduler.schedule(() -> {
                boolean shouldProceed = gameSession.getGameStep().equals(GameStep.CONTESTATION) && gameSession.getCurrentMessage().isEmpty();

                if (shouldProceed) {
                    try {
                        message.setType("Personne ne conteste");
                        message.setBody("personne n'a contesté 😒😒😒");

                        for (WebSocketSession session : gameSession.getSocketSessions()) {
                            session.sendMessage(new TextMessage(jsonConverter.convert(message)));
                        }

                        gameSession.setGameStep(GameStep.PLAY_CARD);

                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        shutdownScheduler(scheduler);
                    }
                } else {
                    shutdownScheduler(scheduler);
                }
            }, 10, TimeUnit.SECONDS);
        }
    }

    private void shutdownScheduler(ScheduledExecutorService scheduler) {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduler.shutdownNow();
        }
    }

    private int getIndex(GameSession gameSession) {
        for (int j = 0; j < gameSession.getSocketSessions().size(); j++) {
            if (gameSession.getSocketSessions().get(j).equals(gameSession.getCurrentSession())) {
                return j;
            }
        }
        return 0;
    }

    private boolean isContestantPlayer(GameSession gameSession) {
        return (gameSession.getCurrentIndexOfSessionCard() - 1) != getIndex(gameSession);
    }

    private void contestPlayer(Player contestPlayer, Player player, Card currentCard, Pattern pattern,
                              List<Card> cardsPlayed, List<WebSocketSession> list) {

        Message message = new Message();

        if (currentCard.getPattern().equals(pattern)) {
            contestPlayer.getHand().addAll(cardsPlayed);
            cardsPlayed.clear();

            message.setBody("Le joueur " + contestPlayer.getName() + " prend les cartes 😂😂😂");
            message.setType("prend les cartes");

            broadcaster.broadcastMessage(jsonConverter.convert(message), list);
        } else {
            player.getHand().addAll(cardsPlayed);
            cardsPlayed.clear();

            message.setBody("Le joueur " + player.getName() + " prend les cartes 😂😂😂");
            message.setType("prend les cartes");

            broadcaster.broadcastMessage(jsonConverter.convert(message), list);
        }
    }

}
