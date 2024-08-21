package com.nehms.game.valueobjets;

import java.util.List;

import com.nehms.game.model.Card;
import org.springframework.web.socket.WebSocketSession;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Message {

	private String namePlayer;
	private String type; //alert, info
	private String body;
	private List<Card> cards;
	private Card currentCard;
	private Pattern currentPattern;
	private List<WebSocketSession> sessions;
	
}
