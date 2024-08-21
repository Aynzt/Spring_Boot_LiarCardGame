package com.nehms.game.services;

import com.nehms.game.model.Card;
import com.nehms.game.model.GameSession;
import com.nehms.game.model.Player;
import com.nehms.game.model.SocketManager;
import com.nehms.game.util.Broadcaster;
import com.nehms.game.util.Converter;
import com.nehms.game.util.Processor;
import com.nehms.game.valueobjets.GameStep;
import com.nehms.game.valueobjets.Message;
import com.nehms.game.valueobjets.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.List;

@Component
public class ProcessPlayCard implements Orchestrator {

    private final Broadcaster broadcaster;
    private final Processor processor;
    private final Converter<Object, String> jsonConverter;

    public ProcessPlayCard(Broadcaster broadcaster, Processor processor, Converter<Object, String> jsonConverter) {
        this.broadcaster = broadcaster;
        this.processor = processor;
        this.jsonConverter = jsonConverter;
    }

    @Override
    public void processTheCurrentState(SocketManager socketManager) throws IOException {

        String[] message = socketManager.getCurrentMessage().split("-");

        int indice = findTheIndexOfRoom(message[0], socketManager.getSocketRooms());

        if (message.length != 4)
            return;

        if (!GameStep.PLAY_CARD.equals(socketManager.getSocketRooms().get(indice).getGameSession().getGameStep()))
            return;

        socketManager.getSocketRooms().get(indice).getGameSession().setCurrentSession(socketManager.getCurrentSession());

        socketManager.getSocketRooms().get(indice).getGameSession()
                .setCurrentMessage(message[1] + "-" + message[2] + "-" + message[3]);

        execute(socketManager.getSocketRooms().get(indice).getGameSession());

    }

    private void execute(GameSession gameSession) throws IOException {

        Message message = new Message();

        checkWinner(broadcaster, gameSession);

        if (!GameStep.PLAY_CARD.equals(gameSession.getGameStep()))
            return;

        if (!isPlayerLap(gameSession)) {

            if ("".equals(gameSession.getCurrentMessage()))
                return;

            message.setBody("ce n'est pas a votre tour de jouer !");
            message.setType("not your turn");

            gameSession.getCurrentSession().sendMessage(new TextMessage(jsonConverter.convert(message)));

            return;
        }

        if (processor.processingMessage(gameSession.getCurrentMessage()) == null) {
            gameSession.getCurrentSession().sendMessage(new TextMessage("mauvais format du message"));
            return;
        }


        String number = processor.processingMessage(gameSession.getCurrentMessage()).getNumber();
        Pattern pattern = processor.processingMessage(gameSession.getCurrentMessage()).getPattern();
        Pattern patternPlayed = processor.processingMessage(gameSession.getCurrentMessage()).getPatternPlay();

        gameSession.setCurrentPattern(pattern);

        playOneCard(number, pattern, gameSession.getPlayers().get(getIndexOfCurrentPlayer(gameSession)),
                gameSession.getGameTable(), gameSession.getCurrentcard());

        gameSession.setCurrentPattern(patternPlayed);

        message.setBody("Votre main ♠️");
        message.setType("CARD");
        message.setCurrentCard(new Card(gameSession.getCurrentcard().getPattern(),
                gameSession.getCurrentcard().getNumber()));
        message.setCurrentPattern(patternPlayed);

        for (int i = 0; i < gameSession.getSocketSessions().size(); i++) {
            message.setCards(gameSession.getPlayers().get(i).getHand());
            gameSession.getSocketSessions().get(i)
                    .sendMessage(new TextMessage(jsonConverter.convert(message)));
        }

        message.setBody("vous venez de jouer la carte [ "
                + gameSession.getCurrentcard().getNumber() + ", " + gameSession.getCurrentcard().getPattern() + " ] et vous avez dit " + patternPlayed);

        message.setType("vous avez jouez");

        gameSession.getCurrentSession().sendMessage(new TextMessage(jsonConverter.convert(message)));

        gameSession.setCurrentPlayer(gameSession.getPlayers().get(getIndexOfCurrentPlayer(gameSession)));

        gameSession.setPlayersIndexes(getIndexOfCurrentPlayer(gameSession));

        gameSession.setCurrentIndexOfSessionCard(
                (gameSession.getCurrentIndexOfSessionCard() + 1) % gameSession.getMaxPlayer());

        gameSession.setLap((gameSession.getLap() + 1));

        if (gameSession.getLap() >= 4) {

            gameSession.setGameStep(GameStep.CONTESTATION);


            message.setType("voulez-vous contester ?");
            message.setBody("voulez-vous contester le pattern ?");

            broadcaster.broadcastMessage(jsonConverter.convert(message), gameSession.getSocketSessions());

        }


        message.setType("actuellement le tour");
        message.setBody("c'est actuellement le tour du " + gameSession.getPlayers()
                .get(gameSession.getCurrentIndexOfSessionCard()).getName());


        broadcaster.broadcastMessage(jsonConverter.convert(message), gameSession.getSocketSessions());

        gameSession.setCurrentMessage("");
    }

    private boolean isPlayerLap(GameSession gameSession) {
        return getIndexOfCurrentPlayer(gameSession) == gameSession.getCurrentIndexOfSessionCard();
    }

    private int getIndexOfCurrentPlayer(GameSession gameSession) {

        for (int j = 0; j < gameSession.getSocketSessions().size(); j++) {
            if (gameSession.getSocketSessions().get(j).equals(gameSession.getCurrentSession())) {
                return j;
            }
        }
        return 0;
    }

    private void playOneCard(String numberOfCard, Pattern patternOfCard, Player joueur, List<Card> cardsPlayed,
                            Card currentCard) {

        int indice = findCardInCards(joueur.getHand(), new Card(patternOfCard, numberOfCard));

        currentCard.setPattern(joueur.getHand().get(indice).getPattern());

        currentCard.setNumber(joueur.getHand().get(indice).getNumber());

        cardsPlayed.add(joueur.getHand().get(indice));
        joueur.getHand().remove(indice);
    }

    private int findCardInCards(List<Card> cards, Card card) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).equals(card)) {
                return i;
            }
        }
        return -1;
    }

}
