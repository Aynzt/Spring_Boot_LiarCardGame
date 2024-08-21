package com.nehms.game.model;

import com.nehms.game.services.visitors.ConfigVisitor;
import com.nehms.game.valueobjets.GameStep;
import com.nehms.game.valueobjets.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Data
@Component
public class GameSession {

	private int maxPlayer = 3;
	private List<Player> players;
	private GameStep gameStep;
	private Player currentPlayer;
	private boolean isContestable;
	private List<Card> cards;
	private Player currentPlayerContest;
	private List<Card> gameTable;
	private Pattern currentPattern;
	private String currentMessage;
	private WebSocketSession currentSession;
	private List<WebSocketSession> socketSessions;
	private int numberOfValidation;
	private List<WebSocketSession> socketOfOk;
	private int currentIndexOfSessionCard;
	private Card currentcard;
	private boolean okToPlay;
	private int lap;
	private int playersIndexes;
	private String message;
	private int indexLapFinished;
	private boolean isDisconnected;

	public GameSession() {

		this.setGameStep(GameStep.CREATE_PLAYER);
		this.setCurrentPlayer(new Player(""));
		this.setContestable(false);
		this.setCards(new ArrayList<>());
		this.setCurrentPlayerContest(new Player(""));
		this.setGameTable(new ArrayList<>());
		this.setCurrentPattern(Pattern.COEUR);
		this.setCurrentMessage("");
		this.setPlayers(new ArrayList<>());
		this.setCurrentSession(null);
		this.setSocketSessions(new ArrayList<>());
		this.setSocketOfOk(new ArrayList<>());
		this.setNumberOfValidation(0);
		this.setCurrentIndexOfSessionCard(0);
		this.setCurrentcard(new Card());
		this.setOkToPlay(false);
		this.setLap(0);
		this.setPlayersIndexes(0);
		this.setMessage("");
		this.setDisconnected(false);
		this.setIndexLapFinished(0);
	}
	
	public void reset() {

		this.setGameStep(GameStep.CREATE_PLAYER);
		this.setCurrentPlayer(new Player(""));
		this.setContestable(false);
		this.setCards(new ArrayList<>());
		this.setCurrentPlayerContest(new Player(""));
		this.setGameTable(new ArrayList<>());
		this.setCurrentPattern(Pattern.COEUR);
		this.setCurrentMessage("");
		this.setPlayers(new ArrayList<>());
		this.setCurrentSession(null);
		this.setSocketSessions(new ArrayList<>());
		this.setSocketOfOk(new ArrayList<>());
		this.setNumberOfValidation(0);
		this.setCurrentIndexOfSessionCard(0);
		this.setCurrentcard(new Card());
		this.setOkToPlay(false);
		this.setLap(0);
		this.setPlayersIndexes(0);
		this.setMessage("");
		this.setDisconnected(false);
		this.setIndexLapFinished(0);

	}

	public void configure(ConfigVisitor configVisitor) {
		configVisitor.createCards(this);
		configVisitor.mixCards(this);
		configVisitor.distribute(this);
	}

}
