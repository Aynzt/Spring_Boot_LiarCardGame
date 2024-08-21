package com.nehms.game.services.visitors;

import com.nehms.game.model.Card;
import com.nehms.game.model.GameSession;
import com.nehms.game.model.Player;
import com.nehms.game.valueobjets.Pattern;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class LiarGameConfigurer implements ConfigVisitor {

    @Override
    public void createCards(GameSession session) {
        final String[] numberCards = { "A", "7", "8", "9", "10", "J", "Q", "K" };

        for (String numberCard : numberCards) {
            session.getCards().add(new Card(Pattern.CARREAU, numberCard));
            session.getCards().add(new Card(Pattern.TREFLE, numberCard));
            session.getCards().add(new Card(Pattern.COEUR, numberCard));
            session.getCards().add(new Card(Pattern.PIQUE, numberCard));
        }
    }

    @Override
    public void mixCards(GameSession session) {
        Collections.shuffle(session.getCards());
    }

    @Override
    public void distribute(GameSession session) {
        int handSize = session.getCards().size() / session.getPlayers().size();
        for (Player joueur : session.getPlayers()) {
            for (int i = 0; i < handSize; i++) {
                int lastIndex = session.getCards().size() - 1;
                joueur.getHand().add(session.getCards().get(lastIndex));
                removeCards(joueur.getHand().get(i), session.getCards());
            }
        }
    }

    private void removeCards(Card card, List<Card> cards) {
        cards.removeIf(car -> car.getPattern().equals(card.getPattern()) && car.getNumber().equals(card.getNumber()));
    }
}
