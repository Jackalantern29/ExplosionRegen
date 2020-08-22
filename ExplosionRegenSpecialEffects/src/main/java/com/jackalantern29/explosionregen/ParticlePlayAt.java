package com.jackalantern29.explosionregen;

public enum ParticlePlayAt {
	ANYWHERE,
	EXPLOSION,
	RANDOM,
	NEXT_BLOCK,
	PREVIOUS_BLOCK;
	
	@Override
	public String toString() {
		return name().toLowerCase().replace("_", "-");
	}
}
