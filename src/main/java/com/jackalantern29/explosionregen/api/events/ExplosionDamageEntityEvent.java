package com.jackalantern29.explosionregen.api.events;

import com.jackalantern29.explosionregen.api.ExplosionSettings;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class ExplosionDamageEntityEvent extends ExplosionEvent {
	private static final HandlerList handlers = new HandlerList();

	private double entityDamage;
	public ExplosionDamageEntityEvent(ExplosionSettings settings, Object what, Location location, double entityDamage) {
		super(settings, what, location);
		this.entityDamage = entityDamage;
	}

	public double getEntityDamage() {
		return entityDamage;
	}

	public void setEntityDamage(double entityDamage) {
		this.entityDamage = entityDamage;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}


}


