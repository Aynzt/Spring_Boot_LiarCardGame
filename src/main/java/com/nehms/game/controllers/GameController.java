package com.nehms.game.controllers;

import com.nehms.game.model.SocketManager;
import com.nehms.game.exceptions.GameSessionNullException;
import com.nehms.game.services.Orchestrator;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
@Getter
public class GameController {

	private final Orchestrator createRoom;
	private final Orchestrator joinRoom;
	private final Orchestrator askToPlay;
	private final Orchestrator playCard;
	private final Orchestrator contestation;

	SocketManager socketManager = new SocketManager();

    public GameController(@Qualifier("processCreateRoom") Orchestrator createRoom,
                          @Qualifier("processJoinRoom") Orchestrator joinRoom,
                          @Qualifier("processAskOkToPlay") Orchestrator askToPlay,
                          @Qualifier("processPlayCard") Orchestrator playCard,
                          @Qualifier("processContestation") Orchestrator contestation) {
        this.createRoom = createRoom;
        this.joinRoom = joinRoom;
        this.askToPlay = askToPlay;
        this.playCard = playCard;
        this.contestation = contestation;
    }

    public void play(WebSocketSession session, TextMessage textMessage) throws GameSessionNullException, IOException {

		if (textMessage != null) {
			socketManager.setCurrentMessage(textMessage.getPayload());
		}

		socketManager.setCurrentSession(session);

		createRoom.processTheCurrentState(socketManager);
		joinRoom.processTheCurrentState(socketManager);
		askToPlay.processTheCurrentState(socketManager);
		playCard.processTheCurrentState(socketManager);
		contestation.processTheCurrentState(socketManager);

	}

}
