package com.jackalantern29.explosionregen.api.events;

import com.jackalantern29.explosionregen.api.Explosion;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class ExplosionBlockRegenEvent extends ExplosionEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private boolean cancel = false;
	private ExplosionPhase phase = ExplosionPhase.ON_BLOCK_REGEN;
	public ExplosionBlockRegenEvent(Explosion explosion) {
		super(explosion);
	}

	public ExplosionPhase getPhase() {
		return phase;
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}


