package com.jackalantern29.explosionregen.api.events;

import com.jackalantern29.explosionregen.api.Explosion;
import com.jackalantern29.explosionregen.api.ExplosionSettings;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class ExplosionEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private Explosion explosion;

	public ExplosionEvent(Explosion explosion) {
		this.explosion = explosion;
	}

	public Explosion getExplosion() {
		return explosion;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}


