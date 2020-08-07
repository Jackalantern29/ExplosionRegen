package com.jackalantern29.explosionregen.api.events;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.jackalantern29.explosionregen.api.ExplosionSettings;

public class ExplosionTriggerEvent extends ExplosionEvent {
	private static final HandlerList handlers = new HandlerList();
	
	private List<Block> blockList;
	private double blockDamage;

	public ExplosionTriggerEvent(ExplosionSettings settings, Object what, Location location, List<Block> blockList, double blockDamage) {
		super(settings, what, location);
		this.blockList = blockList;
		this.blockDamage = blockDamage;
	}

	public List<Block> getBlockList() {
		return blockList;
	}

	public double getBlockDamage() {
		return blockDamage;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}


