package com.nehms.game.socket;

import com.nehms.game.controllers.GameController;
import com.nehms.game.model.Player;
import com.nehms.game.model.Room;
import com.nehms.game.util.Broadcaster;
import com.nehms.game.util.Converter;
import com.nehms.game.valueobjets.Message;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;

@Component
public class Communication extends TextWebSocketHandler {

	private final GameController gameController;
	private final Converter<Object, String> jsonConverter;
	private final Broadcaster broadcaster;

    public Communication(GameController gameController,
						 Converter<Object, String> jsonConverter,
						 Broadcaster broadcaster) {
        this.gameController = gameController;
        this.jsonConverter = jsonConverter;
        this.broadcaster = broadcaster;
    }

    @Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		gameController.play(session, message);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

		for (Room room : gameController.getSocketManager().getSocketRooms()) {

				if(room.getGameSession().getSocketSessions().contains(session)) {

					Message message = new Message();

                    int indexofTheLeavePlayer = 0;

					for (int i = 0; i < room.getGameSession().getSocketSessions().size(); i++) {

						if (room.getGameSession().getSocketSessions().get(i).equals(session)) {
							indexofTheLeavePlayer = i;
						}
					}

					message.setBody("Le joueur " + room.getGameSession().getPlayers().get(indexofTheLeavePlayer).getName()
							+ " a quitter le jeu 😭😭");

					room.getGameSession().getSocketSessions().remove(session);

					room.getGameSession().getPlayers().remove(room.getGameSession().getPlayers().get(indexofTheLeavePlayer));

                    List<WebSocketSession> sessions = new ArrayList<>(room.getGameSession().getSocketSessions());

                    List<Player> players = new ArrayList<>(room.getGameSession().getPlayers());

					room.getGameSession().reset();

					int i = 1;
					for (Player player : players) {
						player.setName("Player " + i);
						
						i++;
					}

					room.getGameSession().setSocketSessions(sessions);

					room.getGameSession().setPlayers(players);

					message.setType("Reset");

					broadcaster.broadcastMessage(jsonConverter.convert(message), room.getGameSession().getSocketSessions());

					Message message2 = new Message();

					message2.setType("changement d'identifiant");

					for (int j = 0; j < room.getGameSession().getSocketSessions().size(); j++) {
						message2.setNamePlayer(players.get(j).getName());
						message2.setBody("votre nouvelle identifiant est : " + players.get(j).getName());
						room.getGameSession().getSocketSessions().get(j)
								.sendMessage(new TextMessage(jsonConverter.convert(message2)));
					}

				}
			}

		}

}