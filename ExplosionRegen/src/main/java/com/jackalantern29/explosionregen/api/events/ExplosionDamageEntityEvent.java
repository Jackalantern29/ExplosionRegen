package com.jackalantern29.explosionregen.api.events;

import com.jackalantern29.explosionregen.api.Explosion;
import com.jackalantern29.explosionregen.api.ExplosionSettings;
import com.jackalantern29.explosionregen.api.enums.DamageCategory;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class ExplosionDamageEntityEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private ExplosionSettings settings;
	private Object source;
	private Location location;
	private double entityDamage;
	private boolean cancel = false;
	public ExplosionDamageEntityEvent(ExplosionSettings settings, Object source, Location location) {
		this.settings = settings;
		this.source = source;
		this.location = location;
		this.entityDamage = settings.getDamageAmount(DamageCategory.ENTITY);
	}

	public double getEntityDamage() {
		return entityDamage;
	}

	public void setEntityDamage(double entityDamage) {
		this.entityDamage = entityDamage;
	}

	public ExplosionSettings getSettings() {
		return settings;
	}

	public void setSettings(ExplosionSettings settings) {
		this.settings = settings;
	}

	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
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


