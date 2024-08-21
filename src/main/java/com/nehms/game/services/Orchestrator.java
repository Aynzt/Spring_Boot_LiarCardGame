package com.nehms.game.services;

import java.io.IOException;
import java.util.List;

import com.nehms.game.model.GameSession;
import com.nehms.game.model.Room;
import com.nehms.game.model.SocketManager;
import com.nehms.game.exceptions.GameSessionNullException;
import com.nehms.game.util.Broadcaster;
import com.nehms.game.util.Converter;
import com.nehms.game.util.JsonConverter;
import com.nehms.game.valueobjets.GameStep;
import com.nehms.game.valueobjets.Message;

public interface Orchestrator {

	void processTheCurrentState(SocketManager socketManager) throws GameSessionNullException, IOException;

	default int findTheIndexOfRoom(String roomKey, List<Room> rooms) {
		for (int i = 0; i < rooms.size(); i++) {
			if (rooms.get(i).getRoomKey().equals(roomKey)) {
				return i;
			}
		}
		return -1;
	}

	default void checkWinner(Broadcaster broadcaster, GameSession gameSession) {
		Message message = new Message();
		Converter<Object, String> jsonConverter = new JsonConverter();

		for (int i = 0; i < gameSession.getPlayers().size(); i++) {

			message.setType("CARD");

			if (gameSession.getPlayers().get(i).getHand().isEmpty()) {
				String winnerMessage = "Le gagnant du jeu est " + gameSession.getPlayers().get(i).getName() + " 🎉🎉🎉🎉🎉";
				message.setBody(winnerMessage);
				message.setType("gagnant");
				broadcaster.broadcastMessage(jsonConverter.convert(message), gameSession.getSocketSessions());
				gameSession.setGameStep(GameStep.END);

				return;
			}
		}
	}

}
