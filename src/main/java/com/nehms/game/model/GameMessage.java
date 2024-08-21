package com.nehms.game.model;

import com.google.gson.Gson;
import com.nehms.game.valueobjets.MessageType;
import com.nehms.game.valueobjets.Pattern;
import lombok.Builder;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;

@Builder
public class GameMessage {
    private String player;
    private MessageType type;
    private String body;
    private List<Card> cards;
    private Card currentCard;
    private Pattern currentPattern;

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
