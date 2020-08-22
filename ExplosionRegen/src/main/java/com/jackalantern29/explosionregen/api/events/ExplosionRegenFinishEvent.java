package com.jackalantern29.explosionregen.api.events;

import com.jackalantern29.explosionregen.api.Explosion;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import org.bukkit.event.HandlerList;

public class ExplosionRegenFinishEvent extends ExplosionEvent {
	private static final HandlerList handlers = new HandlerList();

	private ExplosionPhase phase = ExplosionPhase.EXPLOSION_FINISHED_REGEN;

	public ExplosionRegenFinishEvent(Explosion explosion) {
		super(explosion);
	}

	public ExplosionPhase getPhase() {
		return phase;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}


