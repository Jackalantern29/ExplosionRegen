package com.jackalantern29.explosionregen.api.events;

import com.jackalantern29.explosionregen.api.Explosion;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;

public class ExplosionBlockRegeneratingEvent extends ExplosionRegenEvent {
	public ExplosionBlockRegeneratingEvent(Explosion explosion) {
		super(explosion, ExplosionPhase.BLOCK_REGENERATING);
	}
}


