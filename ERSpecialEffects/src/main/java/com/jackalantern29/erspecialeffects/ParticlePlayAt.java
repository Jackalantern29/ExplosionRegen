package com.jackalantern29.erspecialeffects;

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
