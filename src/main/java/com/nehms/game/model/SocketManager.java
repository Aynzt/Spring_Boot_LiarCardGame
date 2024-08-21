package com.nehms.game.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SocketManager {

	private List<Room> socketRooms;
	private int currentRoomIndex;
	private WebSocketSession currentSession;
	private String currentMessage;

	public SocketManager() {
		this.setSocketRooms(new ArrayList<>());
	}
	
}
