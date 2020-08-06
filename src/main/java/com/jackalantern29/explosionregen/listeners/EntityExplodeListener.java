package com.jackalantern29.explosionregen.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import com.jackalantern29.explosionregen.ExplosionRegen;
import com.jackalantern29.explosionregen.api.ERExplosionSettingsOverride;
import com.jackalantern29.explosionregen.api.ExplosionSettings;
import com.jackalantern29.explosionregen.api.events.ERExplodeEvent;

public class EntityExplodeListener implements Listener {
	
	@EventHandler
	public void onExplode(EntityExplodeEvent event) {
		Entity entity = event.getEntity();
		if(!event.isCancelled()) {
			if(entity != null)
				explode(event, entity, event.getLocation(), event.blockList());
			else
				explode(event, event.getLocation().getBlock().getState(), event.getLocation(), event.blockList());
		}
		//explode(event, explosionType, explosionType.toString().toLowerCase(), event.getLocation(), event.getEntity(), event.blockList());
	}
	public class BlockExplodeListener implements Listener {
		@EventHandler
		public void onExplode(BlockExplodeEvent event) {
			if(!event.isCancelled()) {
				explode(event, event.getBlock().getState(), event.getBlock().getLocation(), event.blockList());
			}
		}
	}
	private ExplosionSettings calculateOverrides(ExplosionSettings settings, Object source) {
		ExplosionSettings newSettings = settings;

		if(source instanceof Entity) {
			for(ERExplosionSettingsOverride override : new ArrayList<ERExplosionSettingsOverride>(settings.getOverrides())) {
				if(override.doMeetConditions((Entity) source)) {
					int conditions = 0;
					if(override.countConditions() > conditions) {
						conditions = override.countConditions();
						newSettings = override.getExplosionSettings();
					}
				}
			}
		}
		return newSettings;
	}
	
	private void explode(Event event, Object what, Location location, List<Block> blockList) {
		ExplosionSettings settings = ExplosionSettings.getSettings("default");
		if(settings.getConditions() != null) {
			if(!settings.getConditions().doMeetConditions(what))
				return;
		}
		settings = calculateOverrides(settings, what);
		if(!settings.getOverrides().isEmpty())
			settings = calculateOverrides(settings, what);
		ERExplodeEvent e = new ERExplodeEvent(settings, what, location, blockList);
		Bukkit.getPluginManager().callEvent(e);
		settings = e.getSettings();
		if(e.isCancelled())
			return;
		if(event instanceof EntityExplodeEvent)
			((EntityExplodeEvent) event).setYield(0.0f);
		else if(event instanceof BlockExplodeEvent)
			((BlockExplodeEvent) event).setYield(0.0f);
		ExplosionRegen.getExplosionMap().addExplosion(settings, location, blockList);
	}
}
