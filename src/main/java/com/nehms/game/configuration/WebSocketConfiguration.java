package com.nehms.game.configuration;

import com.nehms.game.socket.Communication;
import com.nehms.game.socket.RoomHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {
	
	private final Communication communication;
	private final RoomHandler roomHandler;

	public WebSocketConfiguration(Communication communication, RoomHandler roomHandler) {
		this.communication = communication;
        this.roomHandler = roomHandler;
    }

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(communication , "/game").setAllowedOrigins("*");
		registry.addHandler(roomHandler , "/rooms/{room}")
				.setAllowedOrigins("*");
	}
	

}
