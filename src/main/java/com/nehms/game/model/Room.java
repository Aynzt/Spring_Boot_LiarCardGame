package com.nehms.game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nehms.game.valueobjets.GameStep;
import com.nehms.game.valueobjets.Pattern;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@ToString
@EqualsAndHashCode
@Getter
@Setter
public class Room {
    private String roomKey;
    private int playerLimit = 3;
    private int lapPlayed;
    private GameStep step;
    private String currentPlayer;
    private String lastPlayer;
    private String currentSession;
    private String messageReceived;
    private Card cardPlayed;
    private Pattern patternPlayed;
    private Map<String, Player> players;
    private List<String> playerSorted;
    private List<Card> cardOnPlay;
    private List<Card> cards;
    private GameSession gameSession;

    public Room() {
        this.currentPlayer = null;
        this.messageReceived = null;
        this.cardPlayed = null;
        this.patternPlayed = null;
        this.lapPlayed = 0;
        this.players = new ConcurrentHashMap<>();
        this.cardOnPlay = new ArrayList<>();
    }

    public Room(String roomKey) {
        this.currentPlayer = null;
        this.messageReceived = null;
        this.cardPlayed = null;
        this.patternPlayed = null;
        this.lapPlayed = 0;
        this.players = new ConcurrentHashMap<>();
        this.cardOnPlay = new ArrayList<>();
        this.roomKey = roomKey;
    }
}
