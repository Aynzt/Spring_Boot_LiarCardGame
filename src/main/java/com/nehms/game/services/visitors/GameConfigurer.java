package com.nehms.game.services.visitors;

import com.nehms.game.model.Room;

public interface GameConfigurer {

    void createCards(Room room);

    void mixCards(Room room);

    void distribute(Room room);

    void sortPlayers(Room room);

}
