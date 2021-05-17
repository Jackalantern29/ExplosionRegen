package com.jackalantern29.explosionregen.api.events;

import com.jackalantern29.explosionregen.api.Explosion;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;
import org.bukkit.event.Cancellable;

public class ExplosionTriggerEvent extends ExplosionRegenEvent implements Cancellable {
	private boolean cancel = false;

	public ExplosionTriggerEvent(Explosion explosion) {
		super(explosion, ExplosionPhase.ON_EXPLODE);
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


