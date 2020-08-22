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

public class ExplosionDamageEntityEvent extends ExplosionEvent implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private double entityDamage;
	private boolean cancel = false;
	public ExplosionDamageEntityEvent(Explosion explosion) {
		this(explosion, explosion.getSettings().getDamageAmount(DamageCategory.ENTITY));
	}
	public ExplosionDamageEntityEvent(Explosion explosion, double entityDamage) {
		super(explosion);
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


	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}
}


