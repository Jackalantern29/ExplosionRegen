package com.jackalantern29.explosionregen.api.events;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.jackalantern29.explosionregen.api.ExplosionSettings;

public class ERExplodeEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	
	private boolean cancel = false;
	private ExplosionSettings settings;
	private Object what;
	private Location location;
	private List<Block> blockList;
	public ERExplodeEvent(ExplosionSettings settings, Object what, Location location, List<Block> blockList) {
		this.settings = settings;
		this.what = what;
		this.location = location;
		this.blockList = blockList;
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
	public List<Block> getBlockList() {
		return blockList;
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
//public class ExplodeEventt extends Event implements Cancellable {
//	private static final HandlerList handlers = new HandlerList();
//	private List<ERBlock> blocks = new ArrayList<>();
//	private boolean cancelled;
//	private Location location;
//	private float yield;
//	private ExplosionSettings settings;
//	@Override
//	public HandlerList getHandlers() {
//		return handlers;
//	}
//	@Override
//	public boolean isCancelled() {
//		return cancelled;
//	}
//	@Override
//	public void setCancelled(boolean cancel) {
//		cancelled = cancel;
//	}
//	public Location getLocation() {
//		return location;
//	}
//	public List<ERBlock> blockList() {
//		return blocks;
//	}
//	public float getYield() {
//		return yield;
//	}
//	public void setYield(float yield) {
//		this.yield = yield;
//	}
//	public ExplosionSettings getSettings() {
//		return settings;
//	}
//	public void setSettings(ExplosionSettings settings) {
//		this.settings = settings;
//	}
//}


