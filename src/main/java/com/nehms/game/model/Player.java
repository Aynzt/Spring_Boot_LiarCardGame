package com.nehms.game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.nehms.game.valueobjets.GameStep;
import com.nehms.game.valueobjets.PlayerStep;
import lombok.Data;

@Data
public class Player {
	
	private String name;
	private String sessionId;
	private PlayerStep step;
	private int position;
	private List<Card> hand = new ArrayList<>();

	public Player(String name) {
		this.name = name;
	}

	public Player(String name, String sessionId) {
		this.name = name;
		this.sessionId = sessionId;
		this.step = PlayerStep.CREATING;
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Player player)) return false;

        return Objects.equals(sessionId, player.sessionId);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(sessionId);
	}
}