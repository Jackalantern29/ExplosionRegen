package com.jackalantern29.explosionregen.api.events;

import com.jackalantern29.explosionregen.api.Explosion;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class ExplosionRegenEvent extends ExplosionEvent {

	private ExplosionPhase phase;
	public ExplosionRegenEvent(Explosion explosion, ExplosionPhase phase) {
		super(explosion);
		this.phase = phase;
	}
	public ExplosionPhase getPhase() {
		return phase;
	}
}


