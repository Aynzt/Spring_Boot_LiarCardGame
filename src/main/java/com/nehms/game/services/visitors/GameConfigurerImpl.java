package com.nehms.game.services.visitors;

import com.nehms.game.model.Card;
import com.nehms.game.model.Player;
import com.nehms.game.model.Room;
import com.nehms.game.valueobjets.Pattern;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class GameConfigurerImpl implements GameConfigurer {

    @Override
    public void createCards(Room room) {
        final String[] numberCards = {"A", "7", "8", "9", "10", "J", "Q", "K"};

        for (String numberCard : numberCards) {
            room.getCards().add(new Card(Pattern.CARREAU, numberCard));
            room.getCards().add(new Card(Pattern.TREFLE, numberCard));
            room.getCards().add(new Card(Pattern.COEUR, numberCard));
            room.getCards().add(new Card(Pattern.PIQUE, numberCard));
        }
    }

    @Override
    public void mixCards(Room room) {
        Collections.shuffle(room.getCards());
    }

    @Override
    public void distribute(Room room) {
        int handSize = room.getCards().size() / room.getPlayers().size();

        room.getPlayers().values()
                .forEach(player -> {
                    for (int i = 0; i < handSize; i++) {
                        int lastIndex = room.getCards().size() - 1;
                        player.getHand().add(room.getCards().get(lastIndex));
                        removeCards(player.getHand().get(i), room.getCards());
                    }
                });
    }

    @Override
    public void sortPlayers(Room room) {
        room.setPlayerSorted(room.getPlayers()
                .values()
                .stream()
                .map(Player::getSessionId)
                .toList());

        Collections.shuffle(room.getPlayerSorted());
    }

    private void removeCards(Card card, List<Card> cards) {
        cards.removeIf(car -> car.getPattern().equals(card.getPattern()) && car.getNumber().equals(card.getNumber()));
    }
}
