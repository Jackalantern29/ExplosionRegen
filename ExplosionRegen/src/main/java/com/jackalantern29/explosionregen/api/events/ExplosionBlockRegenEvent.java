package com.jackalantern29.explosionregen.api.events;

import com.jackalantern29.explosionregen.api.Explosion;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class ExplosionBlockRegenEvent extends ExplosionRegenEvent implements Cancellable {

	private boolean cancel = false;
	public ExplosionBlockRegenEvent(Explosion explosion) {
		super(explosion, ExplosionPhase.ON_BLOCK_REGEN);
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}
}


