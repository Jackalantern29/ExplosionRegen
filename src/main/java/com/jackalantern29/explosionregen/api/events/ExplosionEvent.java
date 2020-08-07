package com.jackalantern29.explosionregen.api.events;

import com.jackalantern29.explosionregen.api.ExplosionSettings;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;

public class ExplosionEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();

	private boolean cancel = false;
	private ExplosionSettings settings;
	private Object what;
	private Location location;

	public ExplosionEvent(ExplosionSettings settings, Object what, Location location) {
		this.settings = settings;
		this.what = what;
		this.location = location;
	}
	public ExplosionSettings getSettings() {
		return settings;
	}
	public void setSettings(ExplosionSettings settings) {
		this.settings = settings;
	}

	public Object getSource() {
		return what;
	}

	public Location getLocation() {
		return location;
	}

	@Override
	public boolean isCancelled() {
		return cancel;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancel = cancel;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}


