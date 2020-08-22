package com.jackalantern29.explosionregen.api.enums;

public enum ExplosionPhase {
	BLOCK_REGENERATING,
	ON_BLOCK_REGEN,
	ON_EXPLODE,
	EXPLOSION_FINISHED_REGEN;
	
	@Override
	public String toString() {
		return name().toLowerCase().replace("_", "-");
	}
}
