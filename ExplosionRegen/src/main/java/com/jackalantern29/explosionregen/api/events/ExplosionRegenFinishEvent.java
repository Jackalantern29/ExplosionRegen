package com.jackalantern29.explosionregen.api.events;

import com.jackalantern29.explosionregen.api.Explosion;
import com.jackalantern29.explosionregen.api.enums.ExplosionPhase;

public class ExplosionRegenFinishEvent extends ExplosionRegenEvent {
	public ExplosionRegenFinishEvent(Explosion explosion) {
		super(explosion, ExplosionPhase.EXPLOSION_FINISHED_REGEN);
	}

}


