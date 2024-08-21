package com.nehms.game.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
public class RoomManager {

    private static RoomManager instance;
    private Map<String, Room> rooms;

    private RoomManager(Map<String, Room> rooms) {
        this.rooms = rooms;
    }

    public static RoomManager getInstance() {
        if (instance == null) {
            instance = new RoomManager(new ConcurrentHashMap<>());
        }
        return instance;
    }

}
