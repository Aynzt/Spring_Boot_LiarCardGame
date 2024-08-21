package com.nehms.game.util;

import java.util.List;

import com.nehms.game.model.Card;
import com.nehms.game.model.Player;

public interface Configurable {

    void createCards(List<Card> cards);

    void mixCards(List<Card> cards);

    void distribute(List<Card> cards, List<Player> players);

    void removeCards(Card card, List<Card> cards);

}
